package stock.exchange.instrument;

import stock.exchange.domain.DoubleReference;

public interface MarketDataReads {

  DoubleReference getMarketPriceRef(int instrumentId);
}