package util.nogc;

public interface ReusableObjects<T> {

  T capture();

  long createdTotal();

  long cachedTotal();

  void release(T t);
}
