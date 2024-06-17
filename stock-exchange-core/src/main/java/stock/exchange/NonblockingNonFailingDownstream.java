package stock.exchange;

public interface NonblockingNonFailingDownstream<T> {

  void accept(T t);

}
