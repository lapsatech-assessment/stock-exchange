package stock.exchange.trade;

@SuppressWarnings("serial")
public class TradeExecutionPriceMistmachValidationException extends TradeExecutionValidationException {

  public TradeExecutionPriceMistmachValidationException() {
  }

  public TradeExecutionPriceMistmachValidationException(double buyPrice, double sellPrice) {
    super("Price mistmach buy: " + buyPrice + " sell " + sellPrice);
  }
}
