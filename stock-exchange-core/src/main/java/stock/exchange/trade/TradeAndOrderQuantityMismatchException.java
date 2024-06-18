package stock.exchange.trade;

@SuppressWarnings("serial")
public class TradeAndOrderQuantityMismatchException extends TradeValidationException {

  public TradeAndOrderQuantityMismatchException(int tradeQuantity, String word, int orderQuantity) {
    super("trade quantity " + String.valueOf(tradeQuantity) + " is " + word + " than order quantity "
        + String.valueOf(orderQuantity));
  }

}
