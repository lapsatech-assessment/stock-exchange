package stock.exchange.trade;

@SuppressWarnings("serial")
public class TradeAndOrderPriceMismatchException extends TradeValidationException {

  public TradeAndOrderPriceMismatchException(double tradePrice, String word, double orderPrice) {
    super("trade price " + String.valueOf(tradePrice) + " is " + word + " than order price "
        + String.valueOf(orderPrice));
  }

}
