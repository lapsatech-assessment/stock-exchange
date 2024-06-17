package stock.exchange.trade;

@SuppressWarnings("serial")
public class TradeExecutionValidationException extends TradeExecutionException {

  public TradeExecutionValidationException() {
  }

  public TradeExecutionValidationException(String message, Throwable cause) {
    super(message, cause);
  }

  public TradeExecutionValidationException(String message) {
    super(message);
  }

  public TradeExecutionValidationException(Throwable cause) {
    super(cause);
  }
}
