package stock.exchange.book;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import stock.exchange.NonblockingDownstream;
import stock.exchange.domain.InstrumentRecord;
import stock.exchange.domain.OrderRecord;
import stock.exchange.domain.SecurityRecord;
import stock.exchange.domain.TradeRecord;
import stock.exchange.trade.TradeManager;

public class OrderBookManagerImpl implements OrderBookManager {

  private final Lock reader, writer;

  private final Int2ObjectMap<OrderBook> books;
  private final Supplier<StockMatcher> stockMatcherFactory;
  private final NonblockingDownstream<OrderRecord> filledOrderDownstream;
  private final NonblockingDownstream<TradeRecord> tradeDownstream;

  public OrderBookManagerImpl(
      Supplier<StockMatcher> stockMatcherFactory,
      NonblockingDownstream<OrderRecord> filledOrderDownstream,
      NonblockingDownstream<TradeRecord> tradeDownstream) {
    this.books = new Int2ObjectOpenHashMap<>();
    this.stockMatcherFactory = stockMatcherFactory;
    this.filledOrderDownstream = filledOrderDownstream;
    this.tradeDownstream = tradeDownstream;
    ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
    this.reader = rw.readLock();
    this.writer = rw.writeLock();
  }

  @Override
  public OrderBook addOrderBook(SecurityRecord instrument, TradeManager tradeManagerWrites) {
    writer.lock();
    try {
      if (books.containsKey(instrument.id())) {
        throw new DuplicateOrderBookException();
      }
      OrderBook book = new OrderBookImpl(
          instrument,
          stockMatcherFactory.get(),
          tradeManagerWrites,
          filledOrderDownstream,
          tradeDownstream);
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