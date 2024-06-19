package stock.exchange.trade;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stock.exchange.domain.OrderRecord;
import stock.exchange.domain.SecurityRecord;
import stock.exchange.domain.TradeRecord;
import stock.exchange.integration.Downstream;
import stock.exchange.integration.RejectedDownstream;

public class TradeGeneratorImpl implements TradeGenerator {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final Downstream<TradeRecord> tradeDownstream;
  private final RejectedDownstream<TradeRecord> tradeDownstreamRejected;

  public TradeGeneratorImpl(
      Downstream<TradeRecord> tradeDownstream,
      RejectedDownstream<TradeRecord> tradeDownstreamRejected) {
    this.tradeDownstream = tradeDownstream;
    this.tradeDownstreamRejected = tradeDownstreamRejected;
  }

  private record Trade(
      long id,
      SecurityRecord security,
      OrderRecord buyingOrder,
      OrderRecord sellingOrder,
      double price,
      int quantity) implements TradeRecord {
  }

  @Override
  public void generateTrade(
      SecurityRecord security,
      OrderRecord buyingOrder,
      double buyingPrice,
      OrderRecord sellingOrder,
      double sellingPrice,
      int quantity) {

    if (buyingPrice <= 0) {
      throw new TradeInvalidPriceException(buyingPrice);
    }

    if (buyingPrice > buyingOrder.price()) {
      throw new TradeAndOrderPriceMismatchException(buyingPrice, "higher", buyingOrder.price());
    }

    if (sellingPrice <= 0) {
      throw new TradeInvalidPriceException(sellingPrice);
    }

    if (sellingPrice < sellingOrder.price()) {
      throw new TradeAndOrderPriceMismatchException(sellingPrice, "lower", sellingOrder.price());
    }

    if (buyingPrice < sellingPrice) {
      throw new TradePriceMistmachValidationException(buyingPrice, sellingPrice);
    }

    if (quantity > buyingOrder.quantity()) {
      throw new TradeAndOrderQuantityMismatchException(quantity, "greater", buyingOrder.quantity());
    }

    if (quantity > sellingOrder.quantity()) {
      throw new TradeAndOrderQuantityMismatchException(quantity, "greater", sellingOrder.quantity());
    }

    double tradePrice = (buyingPrice + sellingPrice) / 2; // avg
    long tradeId = Math.abs(UUID.randomUUID().getMostSignificantBits());

    var trade = new Trade(
        tradeId,
        security,
        buyingOrder,
        sellingOrder,
        tradePrice,
        quantity);

    try {
      tradeDownstream.accept(trade);
    } catch (RuntimeException e) {
      try {
        tradeDownstreamRejected.accept(trade, e);
      } catch (RuntimeException e1) {
        logger.error("Rejected downstream exception", e1);
        logger.error("Downstream exception", e);
      }
    }
  }
}
