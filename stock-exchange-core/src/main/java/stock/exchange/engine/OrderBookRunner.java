package stock.exchange.engine;

import java.time.Duration;

import stock.exchange.book.OrderBook;

public interface OrderBookRunner {

  void runOrderBook(OrderBook book, Duration tickerInterval);
}