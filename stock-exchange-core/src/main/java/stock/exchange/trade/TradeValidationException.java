package stock.exchange.trade;

@SuppressWarnings("serial")
public class TradeValidationException extends TradeGenerationException {

  public TradeValidationException() {
  }

  public TradeValidationException(String message, Throwable cause) {
    super(message, cause);
  }

  public TradeValidationException(String message) {
    super(message);
  }

  public TradeValidationException(Throwable cause) {
    super(cause);
  }
}
