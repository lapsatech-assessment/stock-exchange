package stock.exchange.instrument;

@SuppressWarnings("serial")
public class NoSuchInstrumentException extends MarketDataException {

  public NoSuchInstrumentException(int instrumentId) {
    super("No such instrument id " + String.valueOf(instrumentId));
  }

  public NoSuchInstrumentException(String symbol) {
    super("No such instrument symbol " + symbol);
  }
}