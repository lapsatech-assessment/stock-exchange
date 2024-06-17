package stock.exchange.trade;

import stock.exchange.common.CommonException;

@SuppressWarnings("serial")
public class TradeGenerationException extends CommonException {

  public TradeGenerationException() {
  }

  public TradeGenerationException(String message, Throwable cause) {
    super(message, cause);
  }

  public TradeGenerationException(String message) {
    super(message);
  }

  public TradeGenerationException(Throwable cause) {
    super(cause);
  }
}
