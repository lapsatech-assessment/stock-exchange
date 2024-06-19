package util.nogc;

import java.util.function.Consumer;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.objects.ObjectBigArrayBigList;
import it.unimi.dsi.fastutil.objects.ObjectBigList;

public class SimpleReusableObjects<T> implements ReusableObjects<T> {

  private final ObjectBigList<T> array;
  private final Supplier<T> ctor;
  private final Consumer<T> resetOp;
  private long createdCount = 0;

  private long pointer = -1;
  private final long increaseSizeBy;

  public SimpleReusableObjects(
      long initalCapacity,
      long increaseSizeBy,
      Supplier<T> ctor,
      Consumer<T> reset) {
    this.array = new ObjectBigArrayBigList<>(initalCapacity);
    this.array.size(initalCapacity);
    this.increaseSizeBy = increaseSizeBy;
    this.ctor = ctor;
    this.resetOp = reset;
  }

  public SimpleReusableObjects(
      long initalCapacity,
      long increaseSizeBy,
      Supplier<T> ctor) {
    this(initalCapacity, increaseSizeBy, ctor, null);
  }

  @Override
  public T capture() {
    if (pointer < 0) {
      T t = ctor.get();
      createdCount++;
      return t;
    } else {
      return array.remove(pointer--);
    }
  }

  public long cachedTotal() {
    return pointer + 1;
  }

  @Override
  public void release(T t) {
    if (resetOp != null) {
      resetOp.accept(t);
    }
    pointer++;
    if (pointer >= array.size64()) {
      array.size(array.size64() + increaseSizeBy);
    }
    array.set(pointer, t);
  }

  public long createdTotal() {
    return createdCount;
  }

  @Override
  public String toString() {
    return cachedTotal() + "/" + createdTotal();
  }

}