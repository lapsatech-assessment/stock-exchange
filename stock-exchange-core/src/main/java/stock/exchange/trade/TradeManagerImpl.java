package stock.exchange.trade;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stock.exchange.domain.OrderRecord;
import stock.exchange.domain.SecurityRecord;
import stock.exchange.domain.TradeRecord;
import stock.exchange.integration.NonblockingNonFailingDownstream;
import stock.exchange.integration.NonblockingNonFailingJunkDownstream;

public class TradeManagerImpl implements TradeGenerator {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final NonblockingNonFailingJunkDownstream<TradeRecord> tradeRejectedDownstream;
  private final NonblockingNonFailingDownstream<TradeRecord> tradeDownstream;

  public TradeManagerImpl(
      NonblockingNonFailingDownstream<TradeRecord> tradeDownstream,
      NonblockingNonFailingJunkDownstream<TradeRecord> tradeRejectedDownstream) {
    this.tradeDownstream = tradeDownstream;
    this.tradeRejectedDownstream = tradeRejectedDownstream;
  }

  @Override
  public void generateTrade(
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
      tradeDownstream.accept(trade);
    } catch (RuntimeException e) {
      try {
        tradeRejectedDownstream.accept(trade, e);
      } catch (RuntimeException e1) {
        logger.error("Rejected downstream exception", e1);
        logger.error("Original issue with downstream exception", e);
      }
    }
  }

}
