package stock.exchange.trade;

@SuppressWarnings("serial")
public class TradePriceMistmachValidationException extends TradeValidationException {

  public TradePriceMistmachValidationException() {
  }

  public TradePriceMistmachValidationException(double buyPrice, double sellPrice) {
    super("Price mistmach buy: " + buyPrice + " sell " + sellPrice);
  }
}
