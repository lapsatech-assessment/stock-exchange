package stock.exchange.trade;

@SuppressWarnings("serial")
public class TradeInvalidPriceValidationException extends TradeValidationException {

  public TradeInvalidPriceValidationException(double price) {
    super(String.valueOf(price));
  }
}
