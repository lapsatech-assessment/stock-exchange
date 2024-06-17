package stock.exchange.instrument;

import stock.exchange.common.CommonException;

@SuppressWarnings("serial")
public class MarketDataException extends CommonException {

  public MarketDataException() {
    super();
  }

  public MarketDataException(String message, Throwable cause) {
    super(message, cause);
  }

  public MarketDataException(String message) {
    super(message);
  }

  public MarketDataException(Throwable cause) {
    super(cause);
  }
}
