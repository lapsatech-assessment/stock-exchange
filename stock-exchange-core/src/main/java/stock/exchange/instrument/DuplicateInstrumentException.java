package stock.exchange.instrument;

@SuppressWarnings("serial")
public class DuplicateInstrumentException extends MarketDataException {

  public DuplicateInstrumentException() {
  }

  public DuplicateInstrumentException(String message, Throwable cause) {
    super(message, cause);
  }

  public DuplicateInstrumentException(String message) {
    super(message);
  }

  public DuplicateInstrumentException(Throwable cause) {
    super(cause);
  }
}