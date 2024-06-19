package stock.exchange.trader;

import stock.exchange.common.CommonException;

@SuppressWarnings("serial")
public class NoSuchTraderTraderException extends CommonException {

  public NoSuchTraderTraderException(int traderId) {
    super("No such trader id " + traderId);
  }
}
