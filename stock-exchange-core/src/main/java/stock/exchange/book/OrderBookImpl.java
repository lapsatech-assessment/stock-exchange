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
import stock.exchange.integration.NonblockingNonFailingDownstream;
import stock.exchange.integration.NonblockingNonFailingJunkDownstream;
import stock.exchange.matcher.StockMatcher;
import stock.exchange.trade.TradeGenerationException;
import stock.exchange.trade.TradeGenerator;

public class OrderBookImpl implements OrderBook {

  private final Logger logger;

  private final SecurityRecord security;

  private final StockMatcher stockMatcher;
  private final TradeGenerator tradeGenerator;

  private final Long2ObjectMap<OrderRecord> orders = new Long2ObjectOpenHashMap<>();

  private final NonblockingNonFailingDownstream<OrderRecord> filledOrderDownstream;
  private final NonblockingNonFailingJunkDownstream<OrderMatchRecord> tradeGenerationRejected;

  private final Object sync = new Object();

  public OrderBookImpl(
      SecurityRecord security,
      StockMatcher stockMatcher,
      TradeGenerator tradeGenerator,
      NonblockingNonFailingDownstream<OrderRecord> filledOrderDownstream,
      NonblockingNonFailingJunkDownstream<OrderMatchRecord> tradeExecutionRejected) {
    this.logger = LoggerFactory.getLogger("BOOK." + security.symbol());
    this.security = security;
    this.stockMatcher = stockMatcher;
    this.tradeGenerator = tradeGenerator;
    this.filledOrderDownstream = filledOrderDownstream;
    this.tradeGenerationRejected = tradeExecutionRejected;
  }

  @Override
  public void tick() {
    synchronized (sync) {
      stockMatcher.match(
          security.marketPrice(),
          (long buyersOrderId, long sellersOrderId, int quantity, double buyerPrice, double sellerPrice) -> {
            try {

              try {
                tradeGenerator.generateTrade(
                    security,
                    orders.get(buyersOrderId),
                    orders.get(sellersOrderId),
                    quantity,
                    buyerPrice,
                    sellerPrice);
              } catch (TradeGenerationException e) {
                tradeGenerationRejected
                    .accept(new OrderMatchRecord(buyersOrderId, sellersOrderId, quantity, buyerPrice, sellerPrice), e);
              }

            } catch (RuntimeException e) {
              logger.error("Failed to process order match event", e);
            }

          },
          (orderId, quantityLeft) -> {
            // update book for partially filled order values if needed
          },
          orderId -> {
            filledOrderDownstream.accept(orders.remove(orderId));
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

  @Override
  public OrderRecord addBuy(TraderRecord trader, int quantity) {
    synchronized (sync) {
      long orderId = Math.abs(UUID.randomUUID().getMostSignificantBits());
      stockMatcher.addOrderBuy(orderId, quantity); // this checks constraints
      OrderRecord order = new OrderRecord(orderId, security, OrderType.BUY, trader, quantity, -1d);
      orders.put(order.id(), order);
      return order;
    }
  }

  @Override
  public OrderRecord addSell(TraderRecord trader, int quantity) {
    synchronized (sync) {
      long orderId = Math.abs(UUID.randomUUID().getMostSignificantBits());
      stockMatcher.addOrderSell(orderId, quantity); // this checks constraints
      OrderRecord order = new OrderRecord(orderId, security, OrderType.SELL, trader, quantity, -1d);
      orders.put(order.id(), order);
      return order;
    }
  }

  @Override
  public OrderRecord addBid(TraderRecord trader, int quantity, double price) {
    synchronized (sync) {
      long orderId = Math.abs(UUID.randomUUID().getMostSignificantBits());
      stockMatcher.addOrderBid(orderId, price, quantity); // this checks constraints
      OrderRecord order = new OrderRecord(orderId, security, OrderType.BID, trader, quantity, price);
      orders.put(order.id(), order);
      return order;
    }
  }

  @Override
  public OrderRecord addAsk(TraderRecord trader, int quantity, double price) {
    synchronized (sync) {
      long orderId = Math.abs(UUID.randomUUID().getMostSignificantBits());
      stockMatcher.addOrderAsk(orderId, price, quantity); // this checks constraints
      OrderRecord order = new OrderRecord(orderId, security, OrderType.ASK, trader, quantity, price);
      orders.put(order.id(), order);
      return order;
    }
  }

  @Override
  public OrderRecord removeOrder(long orderId) {
    synchronized (sync) {
      stockMatcher.removeOrder(orderId); // this checks constraints
      return orders.remove(orderId);
    }
  }

  @Override
  public Iterable<OrderRecord> getActiveOrders() {
    synchronized (sync) {
      List<OrderRecord> records = new ArrayList<>(orders.size());
      records.addAll(orders.values());
      return Collections.unmodifiableList(records);
    }
  }
}
