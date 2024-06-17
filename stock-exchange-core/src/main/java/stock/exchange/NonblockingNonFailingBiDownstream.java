package stock.exchange;

public interface NonblockingNonFailingBiDownstream<T, M> {

  void accept(T t, M m);

}
