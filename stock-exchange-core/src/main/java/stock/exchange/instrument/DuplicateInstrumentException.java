package stock.exchange.instrument;

import stock.exchange.common.CommonException;

@SuppressWarnings("serial")
public class DuplicateInstrumentException extends CommonException {

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