package stock.exchange;

import stock.exchange.domain.CompositeRecord;
import stock.exchange.domain.InstrumentRecord;
import stock.exchange.domain.OrderRecord;
import stock.exchange.domain.SecurityRecord;
import stock.exchange.domain.TraderRecord;

/**
 * Facade for accessing core functions in this trading system
 */
public interface StockExchangeFacade {

  SecurityRecord createSecurity(int instrumentId, String symbol, double initialPrice);

  CompositeRecord createComposite(int instrumentId, String symbol, String[] componentSymbols);

  InstrumentRecord getInstrument(String symbol);

  TraderRecord getTrader(int traderId);

  TraderRecord createTrader(int traderId, String name);

  OrderRecord sell(int traderId, String symbol, int quantity);

  OrderRecord buy(int traderId, String symbol, int quantity);

  OrderRecord bid(int traderId, String symbol, int quantity, double price);

  OrderRecord ask(int traderId, String symbol, int quantity, double price);

  Iterable<? extends OrderRecord> listOrders(String symbol);

  OrderRecord cancelOrder(String symbol, long orderId);

  Iterable<? extends InstrumentRecord> listInstruments();
}