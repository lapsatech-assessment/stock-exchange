package stock.exchange.trade;

@SuppressWarnings("serial")
public class TradeExecutionInvalidPriceValidationException extends TradeExecutionValidationException {

  public TradeExecutionInvalidPriceValidationException(double price) {
    super(String.valueOf(price));
  }
}
