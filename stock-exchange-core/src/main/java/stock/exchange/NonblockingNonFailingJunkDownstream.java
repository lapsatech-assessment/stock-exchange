package stock.exchange;

public interface NonblockingNonFailingJunkDownstream<T> {

  void accept(T t, Throwable e);

}
