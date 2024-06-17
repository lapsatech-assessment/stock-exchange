package stock.exchange.trade;

import stock.exchange.domain.OrderRecord;
import stock.exchange.domain.SecurityRecord;

public interface TradeGenerator {

  void generateTrade(
      SecurityRecord instrument,
      OrderRecord buyOrder,
      OrderRecord sellOrder,
      int quantity,
      double buyerPrice,
      double sellerPrice);
}
