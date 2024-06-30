package stock.exchange;

import java.time.Duration;

import stock.exchange.book.OrderBook;

public interface OrderBookRunManager {

  void runBook(OrderBook book, Duration tickerInterval);
}