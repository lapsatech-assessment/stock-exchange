package stock.exchange.integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Dummy implementation of the fan-out pattern.
 * 
 * Provides sufficient functionality for the test application
 */
public class FanOutDownstream<T> implements Downstream<T> {

  private final Iterable<Downstream<T>> downstreams;

  @SafeVarargs
  public FanOutDownstream(Downstream<T>... downstreams) {
    this.downstreams = Arrays.asList(downstreams);
  }

  @Override
  public void accept(T t) {
    List<RuntimeException> exceptions = new ArrayList<>();
    downstreams.forEach(d -> {
      try {
        d.accept(t);
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
