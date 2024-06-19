package stock.exchange.book;

@SuppressWarnings("serial")
public class BookTickerFatalErrorException extends BookTickerException {

  public BookTickerFatalErrorException() {
    super();
  }

  public BookTickerFatalErrorException(String message, Throwable cause) {
    super(message, cause);
  }

  public BookTickerFatalErrorException(String message) {
    super(message);
  }

  public BookTickerFatalErrorException(Throwable cause) {
    super(cause);
  }

}
