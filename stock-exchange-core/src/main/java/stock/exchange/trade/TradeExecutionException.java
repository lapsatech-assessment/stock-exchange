package stock.exchange.trade;

import stock.exchange.common.CommonException;

@SuppressWarnings("serial")
public class TradeExecutionException extends CommonException {

  public TradeExecutionException() {
  }

  public TradeExecutionException(String message, Throwable cause) {
    super(message, cause);
  }

  public TradeExecutionException(String message) {
    super(message);
  }

  public TradeExecutionException(Throwable cause) {
    super(cause);
  }
}
