package stock.exchange.trade;

import stock.exchange.domain.OrderMatchRecord;
import stock.exchange.domain.OrderRecord;
import stock.exchange.domain.SecurityRecord;
import stock.exchange.integration.Downstream;

public interface TradeGenerator extends Downstream<OrderMatchRecord> {

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
