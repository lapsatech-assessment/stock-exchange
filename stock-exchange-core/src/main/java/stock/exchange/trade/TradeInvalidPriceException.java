package stock.exchange.trade;

@SuppressWarnings("serial")
public class TradeInvalidPriceException extends TradeValidationException {

  public TradeInvalidPriceException(double price) {
    super(String.valueOf(price));
  }
}
