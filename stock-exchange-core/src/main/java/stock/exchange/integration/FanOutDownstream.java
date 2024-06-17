package stock.exchange.integration;

import java.util.Arrays;

/**
 * Dummy implementation of the fan-out pattern. Sufficient functionality for the
 * test app
 */
public class FanOutDownstream<T> implements NonblockingNonFailingDownstream<T> {

  private final Iterable<NonblockingNonFailingDownstream<T>> downstreams;

  @SafeVarargs
  public FanOutDownstream(NonblockingNonFailingDownstream<T>... downstreams) {
    this.downstreams = Arrays.asList(downstreams);
  }

  @Override
  public void accept(T t) {
    downstreams.forEach(d -> d.accept(t));
  }

}
