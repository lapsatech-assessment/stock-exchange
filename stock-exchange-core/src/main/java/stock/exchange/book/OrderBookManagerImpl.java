package stock.exchange.book;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import stock.exchange.domain.InstrumentRecord;
import stock.exchange.domain.OrderMatchRecord;
import stock.exchange.domain.OrderRecord;
import stock.exchange.domain.SecurityRecord;
import stock.exchange.integration.Downstream;
import stock.exchange.integration.RejectedDownstream;

public class OrderBookManagerImpl implements OrderBookManager {

  private final Lock reader, writer;

  private final Int2ObjectMap<OrderBook> books = new Int2ObjectOpenHashMap<>();

  private final Downstream<OrderMatchRecord> orderMatchDownstream;
  private final RejectedDownstream<OrderMatchRecord> orderMatchDownstreamRejected;
  private final Downstream<OrderRecord> filledOrderDownstream;
  private final RejectedDownstream<OrderRecord> filledOrderDownstreamRejected;

  public OrderBookManagerImpl(
      Downstream<OrderMatchRecord> orderMatchDownstream,
      RejectedDownstream<OrderMatchRecord> orderMatchDownstreamRejected,
      Downstream<OrderRecord> filledOrderDownstream,
      RejectedDownstream<OrderRecord> filledOrderDownstreamRejected) {
    this.orderMatchDownstream = orderMatchDownstream;
    this.orderMatchDownstreamRejected = orderMatchDownstreamRejected;
    this.filledOrderDownstream = filledOrderDownstream;
    this.filledOrderDownstreamRejected = filledOrderDownstreamRejected;
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
      OrderBook book = new OrderBookImpl(
          instrument,
          orderMatchDownstream,
          orderMatchDownstreamRejected,
          filledOrderDownstream,
          filledOrderDownstreamRejected);
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