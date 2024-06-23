package stock.exchange.trade;

import stock.exchange.domain.OrderMatchRecord;
import stock.exchange.domain.OrderRecord;
import stock.exchange.domain.SecurityRecord;
import stock.exchange.integration.Downstream;

/**
 * Provides validation of trade parameters and executes the trade
 */
public interface TradeGenerator extends Downstream<OrderMatchRecord> {

  /**
   * Calling the method leads to validation and execution of the trade
   * 
   * @throws TradeValidationException
   */
  void generateTrade(
      double marketPrice,
      SecurityRecord instrument,
      OrderRecord buyerOrder,
      OrderRecord sellerOrder,
      int quantity);

  @Override
  default void accept(OrderMatchRecord t) {
    generateTrade(
        t.marketPrice(),
        t.security(),
        t.buyerOrder(),
        t.sellerOrder(),
        t.quantity());
  }
}
