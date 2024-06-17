package stock.exchange.trade;

import stock.exchange.domain.OrderRecord;
import stock.exchange.domain.SecurityRecord;
import stock.exchange.domain.TradeRecord;

public interface TradeManager {

  TradeRecord createTrade(
      SecurityRecord instrument,
      OrderRecord buyOrder,
      OrderRecord sellOrder,
      int quantity,
      double buyerPrice,
      double sellerPrice);
}
