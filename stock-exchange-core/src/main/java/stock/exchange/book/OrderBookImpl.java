package stock.exchange.book;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import stock.exchange.domain.OrderMatchRecord;
import stock.exchange.domain.OrderRecord;
import stock.exchange.domain.OrderType;
import stock.exchange.domain.SecurityRecord;
import stock.exchange.domain.TraderRecord;
import stock.exchange.integration.Downstream;
import stock.exchange.integration.RejectedDownstream;
import stock.exchange.matcher.StockMatcher;

public class OrderBookImpl implements OrderBook {

  private final Logger logger;

  private final SecurityRecord security;

  private final StockMatcher stockMatcher = StockMatcher.newInstance();

  private final Long2ObjectMap<Order> orders = new Long2ObjectOpenHashMap<>();

  private final Downstream<? super OrderMatchRecord> orderMatchDownstream;
  private final RejectedDownstream<? super OrderMatchRecord> orderMatchDownstreamRejected;

  private final Downstream<? super OrderRecord> filledOrderDownstream;
  private final RejectedDownstream<? super OrderRecord> filledOrderDownstreamRejected;

  private final Object sync = new Object();

  public OrderBookImpl(
      SecurityRecord security,
      Downstream<? super OrderMatchRecord> orderMatchDownstream,
      RejectedDownstream<? super OrderMatchRecord> orderMatchDownstreamRejected,
      Downstream<? super OrderRecord> filledOrderDownstream,
      RejectedDownstream<? super OrderRecord> filledOrderDownstreamRejected) {
    this.logger = LoggerFactory.getLogger("ORDER_BOOK_" + security.symbol());
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
    synchronized (sync) {
      stockMatcher.match(
          security.marketPrice(),
          (long buyingOrderId, long sellingOrderId, int quantity, double buyingPrice, double sellingPrice) -> {
            Order buyingOrder = orders.get(buyingOrderId);
            Order sellingOrder = orders.get(sellingOrderId);
            OrderMatchRecord match = new OrderMatch(
                security,
                buyingOrder,
                sellingOrder,
                quantity,
                buyingPrice,
                sellingPrice);
            try {
              orderMatchDownstream.accept(match);
            } catch (RuntimeException e) {
              try {
                orderMatchDownstreamRejected.accept(match, e);
              } catch (RuntimeException e1) {
                logger.error("Rejected downstream exception", e1);
                logger.error("Downstream exception", e);
              }
            }

          },
          (orderId, quantityLeft) -> {
            // update book for partially filled order values if needed
          },
          orderId -> {
            Order order = orders.remove(orderId);
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
    synchronized (sync) {
      long orderId = Math.abs(UUID.randomUUID().getMostSignificantBits());
      stockMatcher.addOrderBuy(orderId, quantity); // this checks constraints
      var order = new Order(orderId, security, OrderType.BUY, trader, quantity, Double.NaN);
      orders.put(order.id(), order);
      return order;
    }
  }

  @Override
  public Order addSell(TraderRecord trader, int quantity) {
    synchronized (sync) {
      var orderId = Math.abs(UUID.randomUUID().getMostSignificantBits());
      stockMatcher.addOrderSell(orderId, quantity); // this checks constraints
      var order = new Order(orderId, security, OrderType.SELL, trader, quantity, Double.NaN);
      orders.put(order.id(), order);
      return order;
    }
  }

  @Override
  public Order addBid(TraderRecord trader, int quantity, double price) {
    synchronized (sync) {
      var orderId = Math.abs(UUID.randomUUID().getMostSignificantBits());
      stockMatcher.addOrderBid(orderId, price, quantity); // this checks constraints
      var order = new Order(orderId, security, OrderType.BID, trader, quantity, price);
      orders.put(order.id(), order);
      return order;
    }
  }

  @Override
  public Order addAsk(TraderRecord trader, int quantity, double price) {
    synchronized (sync) {
      var orderId = Math.abs(UUID.randomUUID().getMostSignificantBits());
      stockMatcher.addOrderAsk(orderId, price, quantity); // this checks constraints
      Order order = new Order(orderId, security, OrderType.ASK, trader, quantity, price);
      orders.put(order.id(), order);
      return order;
    }
  }

  @Override
  public Order removeOrder(long orderId) {
    synchronized (sync) {
      stockMatcher.removeOrder(orderId); // this checks constraints
      return orders.remove(orderId);
    }
  }

  @Override
  public Iterable<Order> getActiveOrders() {
    synchronized (sync) {
      List<Order> records = new ArrayList<>(orders.size());
      records.addAll(orders.values());
      return Collections.unmodifiableList(records);
    }
  }
}
