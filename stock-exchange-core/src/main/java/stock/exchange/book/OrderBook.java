package stock.exchange.book;

import java.util.concurrent.TimeUnit;

import stock.exchange.domain.OrderRecord;
import stock.exchange.domain.SecurityRecord;
import stock.exchange.domain.TraderRecord;

public interface OrderBook {

  SecurityRecord instrument();

  OrderRecord addBuy(TraderRecord trader, int quantity);

  OrderRecord addSell(TraderRecord trader, int quantity);

  OrderRecord addBid(TraderRecord trader, int quantity, double price);

  OrderRecord addAsk(TraderRecord trader, int quantity, double price);

  OrderRecord removeOrder(long orderId, long timeout, TimeUnit timeuit) throws InterruptedException;

  OrderRecord removeOrder(long orderId);

  Iterable<? extends OrderRecord> getActiveOrders();

  void tick();

  void flush();

}