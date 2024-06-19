package stock.exchange.book;

import stock.exchange.common.CommonException;

@SuppressWarnings("serial")
public class NoSuchOrderException extends CommonException {

  public NoSuchOrderException(long orderId) {
    super("No such order id " + orderId);
  }
}