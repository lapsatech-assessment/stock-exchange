package stock.exchange;

import java.time.Duration;

import stock.exchange.book.OrderBook;

public interface StockMarketRunner extends AutoCloseable {

  void run(OrderBook book, Duration tickerDuration);

  void shutdown();

  @Override
  default void close() {
    shutdown();
  }
}
