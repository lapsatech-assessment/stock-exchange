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
      double marketPrice,
      SecurityRecord security,
      OrderRecord buyerOrder,
      OrderRecord sellerOrder,
      int quantity) {

    double buyerPrice = Double.isNaN(buyerOrder.price()) ? marketPrice : buyerOrder.price();
    double sellerPrice = Double.isNaN(sellerOrder.price()) ? marketPrice : sellerOrder.price();

    if (buyerPrice <= 0) {
      throw new TradeInvalidPriceException(buyerPrice);
    }

    if (sellerPrice <= 0) {
      throw new TradeInvalidPriceException(sellerPrice);
    }

    if (buyerPrice < sellerPrice) {
      throw new TradePriceMistmachValidationException(buyerPrice, sellerPrice);
    }

    if (quantity > buyerOrder.quantity()) {
      throw new TradeAndOrderQuantityMismatchException(quantity, "greater", buyerOrder.quantity());
    }

    if (quantity > sellerOrder.quantity()) {
      throw new TradeAndOrderQuantityMismatchException(quantity, "greater", sellerOrder.quantity());
    }

    final double tradePrice;
    if (buyerPrice == sellerPrice) {
      tradePrice = buyerPrice;
    } else if (sellerOrder.timestamp().isAfter(buyerOrder.timestamp())) {
      // seller first
      tradePrice = sellerPrice;
    } else {
      // buyer first
      tradePrice = buyerPrice;
    }

    long tradeId = Math.abs(UUID.randomUUID().getMostSignificantBits());

    var trade = new Trade(
        tradeId,
        security,
        buyerOrder,
        sellerOrder,
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
