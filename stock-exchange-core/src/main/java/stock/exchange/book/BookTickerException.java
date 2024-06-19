package stock.exchange.book;

import stock.exchange.common.CommonException;

@SuppressWarnings("serial")
public class BookTickerException extends CommonException {

  public BookTickerException() {
    super();
  }

  public BookTickerException(String message, Throwable cause) {
    super(message, cause);
  }

  public BookTickerException(String message) {
    super(message);
  }

  public BookTickerException(Throwable cause) {
    super(cause);
  }
}
