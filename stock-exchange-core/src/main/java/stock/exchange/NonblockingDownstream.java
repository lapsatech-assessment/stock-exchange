package stock.exchange;

public interface NonblockingDownstream<T> {

  void accept(T t);

}
