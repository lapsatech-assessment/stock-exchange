package stock.exchange.book;

import stock.exchange.domain.InstrumentRecord;
import stock.exchange.domain.SecurityRecord;
import stock.exchange.trade.TradeManager;

public interface OrderBookManager {

  OrderBook addOrderBook(SecurityRecord instrument, TradeManager tradeManagerWrites);

  OrderBook findBookByInstrument(InstrumentRecord instrument);
}