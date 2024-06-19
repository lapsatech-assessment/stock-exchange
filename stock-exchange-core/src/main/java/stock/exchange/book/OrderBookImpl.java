package stock.exchange.book;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import stock.exchange.domain.OrderMatchRecord;
import stock.exchange.domain.OrderRecord;
import stock.exchange.domain.OrderType;
import stock.exchange.domain.SecurityRecord;
import stock.exchange.domain.TraderRecord;
import stock.exchange.integration.Downstream;
import stock.exchange.integration.RejectedDownstream;
import stock.exchange.matcher.StockMatcher;
import stock.exchange.matcher.StockMatcherImpl;

public class OrderBookImpl implements OrderBook {

  private final Logger logger;

  private final SecurityRecord security;

  private final StockMatcher stockMatcher;

  private final Lock stockMatcherLock = new ReentrantLock();
  private final Lock orderCollectionsRead;
  private final Lock orderCollectionsWrite;

  {
    ReadWriteLock rw = new ReentrantReadWriteLock();
    orderCollectionsRead = rw.readLock();
    orderCollectionsWrite = rw.writeLock();
  }

  private final Long2ObjectMap<Order> ordersIndex = new Long2ObjectOpenHashMap<>();
  private final PriorityQueue<Order> ordersStagingQueue = new ObjectArrayFIFOQueue<>();

  private final Downstream<? super OrderMatchRecord> orderMatchDownstream;
  private final RejectedDownstream<? super OrderMatchRecord> orderMatchDownstreamRejected;

  private final Downstream<? super OrderRecord> filledOrderDownstream;
  private final RejectedDownstream<? super OrderRecord> filledOrderDownstreamRejected;

  public OrderBookImpl(
      SecurityRecord security,
      Downstream<? super OrderMatchRecord> orderMatchDownstream,
      RejectedDownstream<? super OrderMatchRecord> orderMatchDownstreamRejected,
      Downstream<? super OrderRecord> filledOrderDownstream,
      RejectedDownstream<? super OrderRecord> filledOrderDownstreamRejected) {
    this(
        new StockMatcherImpl(),
        security,
        orderMatchDownstream,
        orderMatchDownstreamRejected,
        filledOrderDownstream,
        filledOrderDownstreamRejected);
  }

  private OrderBookImpl(
      StockMatcher stockMatcher,
      SecurityRecord security,
      Downstream<? super OrderMatchRecord> orderMatchDownstream,
      RejectedDownstream<? super OrderMatchRecord> orderMatchDownstreamRejected,
      Downstream<? super OrderRecord> filledOrderDownstream,
      RejectedDownstream<? super OrderRecord> filledOrderDownstreamRejected) {
    this.logger = LoggerFactory.getLogger("ORDER_BOOK_" + security.symbol());
    this.stockMatcher = stockMatcher;
    this.security = security;
    this.orderMatchDownstream = orderMatchDownstream;
    this.orderMatchDownstreamRejected = orderMatchDownstreamRejected;
    this.filledOrderDownstream = filledOrderDownstream;
    this.filledOrderDownstreamRejected = filledOrderDownstreamRejected;
  }

  private record OrderMatch(
      SecurityRecord security,
      OrderRecord buyingOrder,
      OrderRecord sellingOrder,
      int quantity,
      double buyingPrice,
      double sellingPrice) implements OrderMatchRecord {
  }

  @Override
  public void tick() {

    try {
      stockMatcherLock.lockInterruptibly();
    } catch (InterruptedException e) {
      throw new OrderBookUnavailableException(e);
    }

    try {
      Order o;

      while (true) {

        orderCollectionsWrite.lock();
        try {
          try {
            o = ordersStagingQueue.dequeue();
          } catch (NoSuchElementException e) {
            break;
          }
          if (!ordersIndex.containsKey(o.id)) {
            continue; // it's removed
          }
        } finally {
          orderCollectionsWrite.unlock();
        }

        switch (o.type) {
          case ASK:
            stockMatcher.addOrderAsk(o.id, o.price, o.quantity);
            break;
          case BID:
            stockMatcher.addOrderBid(o.id, o.price, o.quantity);
            break;
          case BUY:
            stockMatcher.addOrderBuy(o.id, o.quantity);
            break;
          case SELL:
            stockMatcher.addOrderSell(o.id, o.quantity);
            break;
        }
      }

      stockMatcher.match(
          security.marketPrice(),
          (long buyerOrderId, long sellerOrderId, int quantity, double buyerOrderPrice, double sellerOrderPrice) -> {
            Order buyingOrder = ordersIndex.get(buyerOrderId);
            if (buyingOrder == null) {
              throw new BookTickerFatalErrorException(new NoSuchOrderException(buyerOrderId));
            }
            Order sellingOrder = ordersIndex.get(sellerOrderId);
            if (sellingOrder == null) {
              throw new BookTickerFatalErrorException(new NoSuchOrderException(sellerOrderId));
            }
            OrderMatch orderMatch = new OrderMatch(
                security,
                buyingOrder,
                sellingOrder,
                quantity,
                buyerOrderPrice,
                sellerOrderPrice);

            try {
              orderMatchDownstream.accept(orderMatch);
            } catch (RuntimeException e) {
              try {
                orderMatchDownstreamRejected.accept(orderMatch, e);
              } catch (RuntimeException e1) {
                logger.error("Rejected downstream exception", e1);
                logger.error("Downstream exception", e);
              }
            }
          },
          (orderId, volumeRemain) -> {
            // update book for partially filled order values if needed
          },
          orderId -> {
            Order order = ordersIndex.remove(orderId);
            if (order == null) {
              throw new BookTickerFatalErrorException(new NoSuchOrderException(orderId));
            }
            try {
              filledOrderDownstream.accept(order);
            } catch (RuntimeException e) {
              try {
                filledOrderDownstreamRejected.accept(order, e);
              } catch (RuntimeException e1) {
                logger.error("Rejected downstream exception", e1);
                logger.error("Downstream exception", e);
              }
            }
          });
    } finally {
      stockMatcherLock.unlock();
    }
  }

  @Override
  public SecurityRecord instrument() {
    return security;
  }

  @Override
  public void flush() {
    logger.info("Persisting unfilled orders before shutdown");
  }

  private record Order(
      long id,
      SecurityRecord security,
      OrderType type,
      TraderRecord trader,
      int quantity,
      double price) implements OrderRecord {
  }

  @Override
  public Order addBuy(TraderRecord trader, int quantity) {
    return addOrder(trader, OrderType.BUY, quantity, Double.NaN);
  }

  @Override
  public Order addSell(TraderRecord trader, int quantity) {
    return addOrder(trader, OrderType.SELL, quantity, Double.NaN);
  }

  @Override
  public Order addBid(TraderRecord trader, int quantity, double price) {
    return addOrder(trader, OrderType.BID, quantity, price);
  }

  @Override
  public Order addAsk(TraderRecord trader, int quantity, double price) {
    return addOrder(trader, OrderType.ASK, quantity, price);
  }

  private Order addOrder(TraderRecord trader, OrderType type, int quantity, double price) {
    var orderId = Math.abs(UUID.randomUUID().getMostSignificantBits());
    if (trader == null) {
      throw new OrderTraderValidationException();
    }
    if (quantity <= 0) {
      throw new OrderQuantityValidationException();
    }
    if (!Double.isNaN(price) && price <= 0) {
      throw new OrderPriceValidationException();
    }
    var order = new Order(orderId, security, type, trader, quantity, price);
    orderCollectionsWrite.lock();
    try {
      ordersIndex.merge(order.id, order, (o1, o2) -> {
        throw new DuplicateOrderBookException();// Unlikely to happen as UUID guarantees the distinction
      });
      ordersStagingQueue.enqueue(order);
    } finally {
      orderCollectionsWrite.unlock();
    }
    return order;
  }

  @Override
  public Order removeOrder(long orderId) {
    try {
      stockMatcherLock.lockInterruptibly();
    } catch (InterruptedException e) {
      throw new OrderBookUnavailableException(e);
    }
    try {
      orderCollectionsWrite.lock();
      try {
        if (!ordersIndex.containsKey(orderId)) {
          throw new NoSuchOrderException(orderId);
        }
        stockMatcher.removeOrder(orderId); // this checks constraints
        return ordersIndex.remove(orderId);
      } finally {
        orderCollectionsWrite.unlock();
      }
    } finally {
      stockMatcherLock.unlock();
    }
  }

  @Override
  public Iterable<Order> getActiveOrders() {
    try {
      orderCollectionsRead.lockInterruptibly();
    } catch (InterruptedException e) {
      throw new OrderBookUnavailableException(e);
    }
    try {
      if (ordersIndex.isEmpty()) {
        return Collections.emptyList();
      }
      List<Order> records = new ArrayList<>(ordersIndex.size());
      records.addAll(ordersIndex.values());
      return Collections.unmodifiableList(records);
    } finally {
      orderCollectionsRead.unlock();
    }
  }
}
