package stock.exchange;

public interface StockMarketEngine extends AutoCloseable, OrderBookRunManager {

  void shutdown();

  @Override
  default void close() {
    shutdown();
  }
}