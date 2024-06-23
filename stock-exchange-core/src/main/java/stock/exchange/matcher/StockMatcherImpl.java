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
 * This trade matching algorithm implementation of the order book queue provides
 * FIFO algorithms for market orders and time-price priority for limit orders.
 * 
 * The implementation is not thread-safe, so access to this object should be
 * synchronized externally.
 */
public class StockMatcherImpl implements StockMatcher {

  private static class QE {

    private double price;
    private long orderId;
    private int quantity;
    private int volumeRemain;

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
    e.quantity = quantity;
    e.volumeRemain = quantity;
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
    if (qe.volumeRemain != qe.quantity) {
      throw new OrderPartiallyFilledException();
    }
    qeCache.release(index.remove(orderId));
    return true;
  }

  @Override
  public boolean match(
      DoubleReference marketPrice,
      OrderMatchedEventListener orderMatchedEventListener,
      OrderPartiallyFilledEventListener orderPartiallyFilledEventListener,
      OrderFulfilledEventListener orderFulfilledEventListener) {
    ToDoubleFunction<QE> marketPriceFunction = x -> marketPrice.getAsDouble();
    // match orders bid <-> ask
    if (matchQueues(
        orderMatchedEventListener,
        orderPartiallyFilledEventListener,
        orderFulfilledEventListener,
        bidQueue,
        askQueue,
        QE::price,
        QE::price)) {
      return true;
    }
    // match orders bid <-> sell(marketPrice)
    if (matchQueues(
        orderMatchedEventListener,
        orderPartiallyFilledEventListener,
        orderFulfilledEventListener,
        bidQueue,
        sellQueue,
        QE::price,
        marketPriceFunction)) {
      return true;
    }
    // match orders buy(marketPrice) <-> ask
    if (matchQueues(
        orderMatchedEventListener,
        orderPartiallyFilledEventListener,
        orderFulfilledEventListener,
        buyQueue,
        askQueue,
        marketPriceFunction,
        QE::price)) {
      return true;
    }
    // match orders buy(marketPrice) <-> sell(marketPrice)
    if (matchQueues(
        orderMatchedEventListener,
        orderPartiallyFilledEventListener,
        orderFulfilledEventListener,
        buyQueue,
        sellQueue,
        marketPriceFunction,
        marketPriceFunction)) {
      return true;
    }
    return false;
  }

  private boolean matchQueues(
      OrderMatchedEventListener orderMatchedEventListener,
      OrderPartiallyFilledEventListener orderPartiallyFilledEventListener,
      OrderFulfilledEventListener orderFulfilledEventListener,
      PriorityQueue<QE> buyerQueue,
      PriorityQueue<QE> sellerQueue,
      ToDoubleFunction<QE> buyerPriceFunction,
      ToDoubleFunction<QE> sellerPriceFunction) {

    QE buyer, seller;
    double buyerPrice, sellerPrice;

    buyer = firstDequeueRemoved(buyerQueue);
    if (buyer == null) {
      return false;
    }

    seller = firstDequeueRemoved(sellerQueue);
    if (seller == null) {
      return false;
    }

    buyerPrice = buyerPriceFunction.applyAsDouble(buyer);
    sellerPrice = sellerPriceFunction.applyAsDouble(seller);

    if (buyerPrice < sellerPrice) {
      return false;
    }

    final int quantity = Math.min(buyer.volumeRemain, seller.volumeRemain);
    orderMatchedEventListener.onOrderMatched(buyer.orderId, seller.orderId, quantity);

    if (buyer.volumeRemain == quantity) {
      buyerQueue.dequeue();
      qeCache.release(index.remove(buyer.orderId));
      orderFulfilledEventListener.onOrderFulfilled(buyer.orderId);
    } else {
      buyer.volumeRemain -= quantity;
      orderPartiallyFilledEventListener.onOrderPartialyFilled(buyer.orderId, buyer.volumeRemain);
    }

    if (seller.volumeRemain == quantity) {
      sellerQueue.dequeue();
      qeCache.release(index.remove(seller.orderId));
      orderFulfilledEventListener.onOrderFulfilled(seller.orderId);
    } else {
      seller.volumeRemain -= quantity;
      orderPartiallyFilledEventListener.onOrderPartialyFilled(seller.orderId, seller.volumeRemain);
    }
    return true;
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
