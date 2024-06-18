package stock.exchange.integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Dummy implementation of the fan-out pattern
 * 
 * Provides sufficient functionality for the test application
 */
public class FanOutRejectedDownstream<T> implements RejectedDownstream<T> {

  private final Iterable<RejectedDownstream<T>> downstreams;

  @SafeVarargs
  public FanOutRejectedDownstream(RejectedDownstream<T>... downstreams) {
    this.downstreams = Arrays.asList(downstreams);
  }

  @Override
  public void accept(T t, Throwable cause) {
    List<RuntimeException> exceptions = new ArrayList<>();
    downstreams.forEach(d -> {
      try {
        d.accept(t, cause);
      } catch (RuntimeException e) {
        exceptions.add(e);
      }
    });
    if (exceptions.isEmpty()) {
      return;
    }
    RuntimeException first = new RuntimeException("Downstream exceptions");
    Iterator<RuntimeException> i = exceptions.iterator();
    while (i.hasNext()) {
      first.addSuppressed(i.next());
    }
    throw first;
  }

}
