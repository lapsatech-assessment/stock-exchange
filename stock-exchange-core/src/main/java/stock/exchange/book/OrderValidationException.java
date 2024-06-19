package stock.exchange.book;

import stock.exchange.common.CommonException;

@SuppressWarnings("serial")
public class OrderValidationException extends CommonException {

  public OrderValidationException() {
    super();
  }

  public OrderValidationException(String message, Throwable cause) {
    super(message, cause);
  }

  public OrderValidationException(String message) {
    super(message);
  }

  public OrderValidationException(Throwable cause) {
    super(cause);
  }

}
