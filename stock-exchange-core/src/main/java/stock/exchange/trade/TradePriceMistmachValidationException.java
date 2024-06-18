package stock.exchange.trade;

@SuppressWarnings("serial")
public class TradePriceMistmachValidationException extends TradeValidationException {

  public TradePriceMistmachValidationException() {
  }

  public TradePriceMistmachValidationException(double buyPrice, double sellPrice) {
    super("buy price " + String.valueOf(buyPrice) + " is higher than sell price " + String.valueOf(sellPrice));
  }
}
