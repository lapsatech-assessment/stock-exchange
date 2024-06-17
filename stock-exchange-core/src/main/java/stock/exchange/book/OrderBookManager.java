package stock.exchange.book;

import stock.exchange.domain.InstrumentRecord;
import stock.exchange.domain.SecurityRecord;

public interface OrderBookManager {

  OrderBook createOrderBook(SecurityRecord instrument);

  OrderBook findBookByInstrument(InstrumentRecord instrument);
}