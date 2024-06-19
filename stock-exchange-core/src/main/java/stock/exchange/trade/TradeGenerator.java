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
      SecurityRecord instrument,
      OrderRecord buyingOrder,
      double buyingPrice,
      OrderRecord sellingOrder,
      double sellingPrice,
      int quantity);

  @Override
  default void accept(OrderMatchRecord t) {
    generateTrade(t.security(), t.buyingOrder(), t.buyingPrice(), t.sellingOrder(), t.sellingPrice(), t.quantity());
  }
}
