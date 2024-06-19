package stock.exchange.instrument;

import stock.exchange.domain.DoubleReference;

/**
 * Interface for accessing market data
 */
public interface MarketDataReads {

  default double getMarketPrice(int instrumentId) {
    return getMarketPriceRef(instrumentId).getAsDouble();
  }

  DoubleReference getMarketPriceRef(int instrumentId);
}