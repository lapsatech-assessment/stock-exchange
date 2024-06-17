package stock.exchange.trader;

import stock.exchange.domain.TraderRecord;

public interface TraderManager {

  TraderRecord createTrader(int id, String name);

  TraderRecord findTraderById(int id);
}