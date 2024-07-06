package stock.exchange.engine;

import stock.exchange.common.CommonException;

@SuppressWarnings("serial")
public class StockMarketException extends CommonException {

  public StockMarketException() {
    super();
  }

  public StockMarketException(String message, Throwable cause) {
    super(message, cause);
  }

  public StockMarketException(String message) {
    super(message);
  }

  public StockMarketException(Throwable cause) {
    super(cause);
  }

}