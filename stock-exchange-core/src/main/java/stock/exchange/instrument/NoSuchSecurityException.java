package stock.exchange.instrument;

import stock.exchange.common.CommonException;

@SuppressWarnings("serial")
public class NoSuchSecurityException extends CommonException {

  public NoSuchSecurityException() {
  }

  public NoSuchSecurityException(String symbol) {
    super(symbol);
  }

  public NoSuchSecurityException(int securityId) {
    super(String.valueOf(securityId));
  }

}
