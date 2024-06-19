package stock.exchange.matcher;

import static java.util.Comparator.comparingDouble;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.function.ToDoubleFunction;

import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import stock.exchange.book.DuplicateOrderException;
import stock.exchange.book.OrderPartiallyFilledException;
import stock.exchange.domain.DoubleReference;
import util.nogc.ReusableObjects;
import util.nogc.SimpleReusableObjects;

/**
 * This trade matching algorithm implementation of the order book queue
 * provides: FIFO algorithms for Market Orders and Time-price priority for Limit
 * Orders
 * 
 * The implementation is not thread safe so the access to that object should be
 * synchronized externally
 */
public class StockMatcherImpl implements StockMatcher {

  private static class QE {

    private double price;
    private long orderId;
    private int quantityRequested;
    private int quantityRemain;

    private double price() {
      return price;
    }
  }

  private final ReusableObjects<QE> qeCache = new SimpleReusableObjects<>(1000, 1000, QE::new);

  private static final Comparator<QE> ORDER_BY_PRICE_DESC = //
      comparingDouble(QE::price).reversed();

  private static final Comparator<QE> ORDER_BY_PRICE_ASC = //
      comparingDouble(QE::price);

  private final Long2ObjectMap<QE> index = new Long2ObjectOpenHashMap<>();
  private final PriorityQueue<QE> buyQueue = new ObjectArrayFIFOQueue<>();
  private final PriorityQueue<QE> sellQueue = new ObjectArrayFIFOQueue<>();
  private final PriorityQueue<QE> bidQueue = new ObjectHeapPriorityQueue<>(ORDER_BY_PRICE_DESC);
  private final PriorityQueue<QE> askQueue = new ObjectHeapPriorityQueue<>(ORDER_BY_PRICE_ASC);

  @Override
  public void addOrderBuy(long orderId, int quantity) {
    addOrderToQueue(orderId, Double.NaN, quantity, buyQueue);
  }

  @Override
  public void addOrderSell(long orderId, int quantity) {
    addOrderToQueue(orderId, Double.NaN, quantity, sellQueue);
  }

  @Override
  public void addOrderBid(long orderId, double price, int quantity) {
    addOrderToQueue(orderId, price, quantity, bidQueue);
  }

  @Override
  public void addOrderAsk(long orderId, double price, int quantity) {
    addOrderToQueue(orderId, price, quantity, askQueue);
  }

  private void addOrderToQueue(long orderId, double price, int quantity, PriorityQueue<QE> queue) {
    if (index.containsKey(orderId)) {
      throw new DuplicateOrderException();
    }
    QE e = qeCache.capture();
    e.orderId = orderId;
    e.quantityRequested = quantity;
    e.quantityRemain = quantity;
    e.price = price;
    index.put(orderId, e);
    queue.enqueue(e);
  }

  @Override
  public boolean removeOrder(long orderId) {
    QE qe = index.get(orderId);
    if (qe == null) {
      return false;
    }
    if (qe.quantityRemain != qe.quantityRequested) {
      throw new OrderPartiallyFilledException();
    }
    qeCache.release(index.remove(orderId));
    return true;
  }

  @Override
  public void match(
      DoubleReference marketPriceRef,
      OrderMatchedEventListener orderMatchedEventListener,
      OrderPartiallyFilledEventListener orderPartiallyFilledEventListener,
      OrderFulfilledEventListener orderFulfilledEventListener) {
    double marketPrice = marketPriceRef.getAsDouble();
    ToDoubleFunction<QE> marketPriceFunction = x -> marketPrice;
    // match orders bid <-> ask
    matchQueues(
        orderMatchedEventListener,
        orderPartiallyFilledEventListener,
        orderFulfilledEventListener,
        bidQueue,
        askQueue,
        QE::price,
        QE::price);
    // match orders bid <-> sell(marketPrice)
    matchQueues(
        orderMatchedEventListener,
        orderPartiallyFilledEventListener,
        orderFulfilledEventListener,
        bidQueue,
        sellQueue,
        QE::price,
        marketPriceFunction);
    // match orders buy(marketPrice) <-> ask
    matchQueues(
        orderMatchedEventListener,
        orderPartiallyFilledEventListener,
        orderFulfilledEventListener,
        buyQueue,
        askQueue,
        marketPriceFunction,
        QE::price);
    // match orders buy(marketPrice) <-> sell(marketPrice)
    matchQueues(
        orderMatchedEventListener,
        orderPartiallyFilledEventListener,
        orderFulfilledEventListener,
        buyQueue,
        sellQueue,
        marketPriceFunction,
        marketPriceFunction);
  }

  private void matchQueues(
      OrderMatchedEventListener orderMatchedEventListener,
      OrderPartiallyFilledEventListener orderPartiallyFilledEventListener,
      OrderFulfilledEventListener orderFulfilledEventListener,
      PriorityQueue<QE> buyerQueue,
      PriorityQueue<QE> sellerQueue,
      ToDoubleFunction<QE> buyerPriceFunction,
      ToDoubleFunction<QE> sellerPriceFunction) {

    QE buyer, seller;
    double buyerPrice, sellerPrice;

    for (;;) {

      buyer = firstDequeueRemoved(buyerQueue);
      if (buyer == null) {
        return;
      }

      seller = firstDequeueRemoved(sellerQueue);
      if (seller == null) {
        return;
      }

      buyerPrice = buyerPriceFunction.applyAsDouble(buyer);
      sellerPrice = sellerPriceFunction.applyAsDouble(seller);

      if (buyerPrice < sellerPrice) {
        return;
      }

      final int quantity = Math.min(buyer.quantityRemain, seller.quantityRemain);
      orderMatchedEventListener.onOrderMatched(buyer.orderId, seller.orderId, quantity, buyerPrice, sellerPrice);

      if (buyer.quantityRemain == quantity) {
        buyerQueue.dequeue();
        qeCache.release(index.remove(buyer.orderId));
        orderFulfilledEventListener.onOrderFulfilled(buyer.orderId);
      } else {
        buyer.quantityRemain -= quantity;
        orderPartiallyFilledEventListener.onOrderPartialyFilled(buyer.orderId, buyer.quantityRemain);
      }

      if (seller.quantityRemain == quantity) {
        sellerQueue.dequeue();
        qeCache.release(index.remove(seller.orderId));
        orderFulfilledEventListener.onOrderFulfilled(seller.orderId);
      } else {
        seller.quantityRemain -= quantity;
        orderPartiallyFilledEventListener.onOrderPartialyFilled(seller.orderId, seller.quantityRemain);
      }
    }
  }

  private QE firstDequeueRemoved(PriorityQueue<QE> queue) {
    QE qe;
    try {
      for (;;) {
        qe = queue.first();
        if (index.containsKey(qe.orderId)) {
          return qe;
        }
        queue.dequeue(); // the order had removed earlier, removing from queue
      }
    } catch (NoSuchElementException e) {
      return null;
    }
  }

}
