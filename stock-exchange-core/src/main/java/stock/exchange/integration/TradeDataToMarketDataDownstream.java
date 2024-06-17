package stock.exchange.integration;

import stock.exchange.domain.TradeRecord;
import stock.exchange.instrument.MarketDataWrites;

public class TradeDataToMarketDataDownstream implements NonblockingNonFailingDownstream<TradeRecord> {

  private final MarketDataWrites marketDataWrites;

  public TradeDataToMarketDataDownstream(MarketDataWrites marketDataWrites) {
    this.marketDataWrites = marketDataWrites;
  }

  @Override
  public void accept(TradeRecord t) {
    marketDataWrites.acceptLastTradePrice(t.instrument().id(), t.price(), t.quantity());
  }

}
