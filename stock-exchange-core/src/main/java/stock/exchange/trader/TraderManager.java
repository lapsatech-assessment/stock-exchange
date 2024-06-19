package stock.exchange.trader;

import stock.exchange.domain.TraderRecord;

public interface TraderManager {

  TraderRecord createTrader(int traderId, String name);

  TraderRecord findTraderById(int traderId);

  Iterable<? extends TraderRecord> getAllTraders();
}