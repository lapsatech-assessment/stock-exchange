package stock.exchange.trade;

import java.util.UUID;

import stock.exchange.domain.OrderRecord;
import stock.exchange.domain.SecurityRecord;
import stock.exchange.domain.TradeRecord;

public class TradeManagerImpl implements TradeManager {

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
      return new TradeRecord(
          tradeId,
          instrument,
          buyOrder.trader(),
          sellOrder.trader(),
          quantity,
          tradePrice);
    } catch (RuntimeException e) {
      throw new TradeExecutionException(e);
    }
  }

}
