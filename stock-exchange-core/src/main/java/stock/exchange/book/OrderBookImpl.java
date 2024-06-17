package stock.exchange.book;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import stock.exchange.NonblockingDownstream;
import stock.exchange.domain.OrderRecord;
import stock.exchange.domain.OrderType;
import stock.exchange.domain.SecurityRecord;
import stock.exchange.domain.TradeRecord;
import stock.exchange.domain.TraderRecord;
import stock.exchange.trade.TradeManager;

public class OrderBookImpl implements OrderBook {

  private final Logger logger;

  private final SecurityRecord instrument;

  private final StockMatcher stockMatcher;
  private final TradeManager tradeManager;

  private final Long2ObjectMap<OrderRecord> orders = new Long2ObjectArrayMap<>();

  private final NonblockingDownstream<OrderRecord> filledOrderDownstream;
  private final NonblockingDownstream<TradeRecord> tradeDownstream;

  private final Object sync = new Object();

  public OrderBookImpl(
      SecurityRecord instrument,
      StockMatcher stockMatcher,
      TradeManager tradeManager,
      NonblockingDownstream<OrderRecord> filledOrderDownstream,
      NonblockingDownstream<TradeRecord> tradeDownstream) {
    this.logger = LoggerFactory.getLogger("BOOK." + instrument.symbol());
    this.instrument = instrument;
    this.stockMatcher = stockMatcher;
    this.tradeManager = tradeManager;
    this.filledOrderDownstream = filledOrderDownstream;
    this.tradeDownstream = tradeDownstream;
  }

  @Override
  public void tick() {
    synchronized (sync) {
      stockMatcher.match(
          instrument.marketPrice(),
          (long buyersOrderId, long sellersOrderId, int quantity, double buyerPrice, double sellerPrice) -> {
            tradeDownstream.accept(
                tradeManager.createTrade(
                    instrument,
                    orders.get(buyersOrderId),
                    orders.get(sellersOrderId),
                    quantity,
                    buyerPrice,
                    sellerPrice));
          },
          (orderId, quantityLeft) -> {
            // update book for partially filled order values
          },
          orderId -> filledOrderDownstream.accept(orders.remove(orderId)));
    }
  }

  @Override
  public SecurityRecord instrument() {
    return instrument;
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
      OrderRecord order = new OrderRecord(orderId, instrument, OrderType.BUY, trader, quantity, -1d);
      orders.put(order.id(), order);
      return order;
    }
  }

  @Override
  public OrderRecord addSell(TraderRecord trader, int quantity) {
    synchronized (sync) {
      long orderId = Math.abs(UUID.randomUUID().getMostSignificantBits());
      stockMatcher.addOrderSell(orderId, quantity); // this checks constraints
      OrderRecord order = new OrderRecord(orderId, instrument, OrderType.SELL, trader, quantity, -1d);
      orders.put(order.id(), order);
      return order;
    }
  }

  @Override
  public OrderRecord addBid(TraderRecord trader, int quantity, double price) {
    synchronized (sync) {
      long orderId = Math.abs(UUID.randomUUID().getMostSignificantBits());
      stockMatcher.addOrderBid(orderId, price, quantity); // this checks constraints
      OrderRecord order = new OrderRecord(orderId, instrument, OrderType.BID, trader, quantity, price);
      orders.put(order.id(), order);
      return order;
    }
  }

  @Override
  public OrderRecord addAsk(TraderRecord trader, int quantity, double price) {
    synchronized (sync) {
      long orderId = Math.abs(UUID.randomUUID().getMostSignificantBits());
      stockMatcher.addOrderAsk(orderId, price, quantity); // this checks constraints
      OrderRecord order = new OrderRecord(orderId, instrument, OrderType.ASK, trader, quantity, price);
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
  public Iterable<OrderRecord> getOrders() {
    synchronized (sync) {
      List<OrderRecord> list = new ArrayList<>(orders.size());
      list.addAll(orders.values());
      return list;
    }
  }
}
