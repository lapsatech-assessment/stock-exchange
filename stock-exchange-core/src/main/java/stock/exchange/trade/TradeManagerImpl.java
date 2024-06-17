package stock.exchange.trade;

import java.util.UUID;

import stock.exchange.NonblockingNonFailingJunkDownstream;
import stock.exchange.domain.OrderRecord;
import stock.exchange.domain.SecurityRecord;
import stock.exchange.domain.TradeRecord;
import stock.exchange.instrument.MarketDataException;
import stock.exchange.instrument.MarketDataWrites;

public class TradeManagerImpl implements TradeManager {

  private final MarketDataWrites marketDataWrites;
  private final NonblockingNonFailingJunkDownstream<TradeRecord> tradeExecutionRejected;

  public TradeManagerImpl(
      MarketDataWrites marketDataWrites,
      NonblockingNonFailingJunkDownstream<TradeRecord> tradeExecutionRejected) {
    this.marketDataWrites = marketDataWrites;
    this.tradeExecutionRejected = tradeExecutionRejected;
  }

  @Override
  public TradeRecord executeTrade(
      SecurityRecord instrument,
      OrderRecord buyOrder,
      OrderRecord sellOrder,
      int quantity,
      double buyerPrice,
      double sellerPrice) {
    if (buyerPrice <= 0) {
      throw new TradeInvalidPriceValidationException(buyerPrice);
    }
    if (sellerPrice <= 0) {
      throw new TradeInvalidPriceValidationException(sellerPrice);
    }
    if (buyerPrice < sellerPrice) {
      throw new TradePriceMistmachValidationException(buyerPrice, sellerPrice);
    }
    try {
      double tradePrice = (buyerPrice + sellerPrice) / 2; // avg
      long tradeId = Math.abs(UUID.randomUUID().getMostSignificantBits());

      var trade = new TradeRecord(
          tradeId,
          instrument,
          buyOrder.trader(),
          sellOrder.trader(),
          quantity,
          tradePrice);

      try {
        marketDataWrites.acceptLastTradePrice(trade.instrument().id(), trade.price(), trade.quantity());
      } catch (MarketDataException e) {
        tradeExecutionRejected.accept(trade, e);
      }
      return trade;
    } catch (RuntimeException e) {
      throw new TradeExecutionException(e);
    }
  }

}
