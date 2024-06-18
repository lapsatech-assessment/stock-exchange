package stock.exchange.instrument;

import stock.exchange.domain.TradeRecord;
import stock.exchange.integration.Downstream;

public interface MarketDataWrites extends Downstream<TradeRecord> {

  void acceptLastTradePrice(int securityId, double price, int quantity);

  @Override
  default void accept(TradeRecord t) {
    acceptLastTradePrice(t.security().id(), t.price(), t.quantity());
  }

}