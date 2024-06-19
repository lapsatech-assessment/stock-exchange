package stock.exchange.instrument;

@SuppressWarnings("serial")
public class NoSuchSecurityException extends MarketDataException {

  public NoSuchSecurityException(String symbol) {
    super(symbol);
  }

  public NoSuchSecurityException(int securityId) {
    super(String.valueOf(securityId));
  }

}
