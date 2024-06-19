package stock.exchange.book;

import stock.exchange.common.CommonException;

@SuppressWarnings("serial")
public class OrderBookUnavailableException extends CommonException {
  public OrderBookUnavailableException() {
  }

  public OrderBookUnavailableException(Throwable cause) {
    super(cause);
  }
}
