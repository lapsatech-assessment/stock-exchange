package util.nogc;

public interface ReusableObjects<T> {

  T capture();

  void release(T t);
}
