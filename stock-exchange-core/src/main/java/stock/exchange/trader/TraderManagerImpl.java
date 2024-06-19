package stock.exchange.trader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import stock.exchange.domain.TraderRecord;

public class TraderManagerImpl implements TraderManager {

  private final Lock reader, writer;
  private final Int2ObjectMap<Trader> traders;

  public TraderManagerImpl() {
    this.traders = new Int2ObjectOpenHashMap<>();
    ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
    this.reader = rw.readLock();
    this.writer = rw.writeLock();
  }

  private record Trader(
      int id,
      String name) implements TraderRecord {
  }

  @Override
  public Trader createTrader(int id, String name) {
    writer.lock();
    try {
      if (traders.containsKey(id)) {
        throw new DuplicateTraderException();
      }
      Trader rec = new Trader(id, name);
      traders.put(id, rec);
      return rec;
    } finally {
      writer.unlock();
    }
  }

  @Override
  public Trader findTraderById(int traderId) {
    reader.lock();
    try {
      if (!traders.containsKey(traderId)) {
        throw new NoSuchTraderTraderException(traderId);
      }
      return traders.get(traderId);
    } finally {
      reader.unlock();
    }
  }

  @Override
  public Iterable<Trader> getAllTraders() {
    reader.lock();
    try {
      List<Trader> records = new ArrayList<>(traders.size());
      records.addAll(traders.values());
      return Collections.unmodifiableList(records);
    } finally {
      reader.unlock();
    }
  }
}
