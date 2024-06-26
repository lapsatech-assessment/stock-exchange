package stock.exchange.integration;

/**
 * This interface represents consumers for events such as rejection, failure, or
 * unprocessed events generated by the stock market runners.
 * 
 * The closest implementation analogy is a dead-letter queue.
 */
public interface RejectedDownstream<T> {

  /**
   * This method is invoked by market runners.
   * 
   * There are two important considerations for implementations of this interface:
   * 
   * 1. The implementation should be non-blocking. 2. The implementation should
   * minimize the likelihood of throwing exceptions or errors.
   * 
   * @param t the event object
   * @param e the exception that caused the event to be sent to rejection
   */

  void accept(T t, Throwable e);

}
