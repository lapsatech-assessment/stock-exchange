package stock.exchange.book;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import stock.exchange.domain.InstrumentRecord;
import stock.exchange.domain.SecurityRecord;

public class OrderBookManagerImpl implements OrderBookManager {

  private final Lock reader, writer;

  private final Int2ObjectMap<OrderBook> books = new Int2ObjectOpenHashMap<>();
  private final Function<SecurityRecord, OrderBook> orderBookFactory;

  public OrderBookManagerImpl(Function<SecurityRecord, OrderBook> orderBookFactory) {
    this.orderBookFactory = orderBookFactory;
    ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
    this.reader = rw.readLock();
    this.writer = rw.writeLock();
  }

  @Override
  public OrderBook createOrderBook(SecurityRecord instrument) {
    writer.lock();
    try {
      if (books.containsKey(instrument.id())) {
        throw new DuplicateOrderBookException();
      }
      OrderBook book = orderBookFactory.apply(instrument);
      books.put(instrument.id(), book);
      return book;
    } finally {
      writer.unlock();
    }
  }

  @Override
  public OrderBook findBookByInstrument(InstrumentRecord instrument) {
    reader.lock();
    try {
      if (!books.containsKey(instrument.id())) {
        throw new NoSuchBookException();
      }
      return books.get(instrument.id());
    } finally {
      reader.unlock();
    }
  }
}