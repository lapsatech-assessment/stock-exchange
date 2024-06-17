package stock.exchange.book;

import stock.exchange.common.CommonException;

@SuppressWarnings("serial")
public class DuplicateOrderException extends CommonException {

  public DuplicateOrderException() {
  }

  public DuplicateOrderException(String message, Throwable cause) {
    super(message, cause);
  }

  public DuplicateOrderException(String message) {
    super(message);
  }

  public DuplicateOrderException(Throwable cause) {
    super(cause);
  }
}
