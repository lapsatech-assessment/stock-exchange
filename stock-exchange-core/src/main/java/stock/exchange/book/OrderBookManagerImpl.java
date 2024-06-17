package stock.exchange.book;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import stock.exchange.NonblockingNonFailingBiDownstream;
import stock.exchange.NonblockingNonFailingDownstream;
import stock.exchange.domain.InstrumentRecord;
import stock.exchange.domain.OrderRecord;
import stock.exchange.domain.SecurityRecord;
import stock.exchange.domain.TradeRecord;
import stock.exchange.instrument.OrderMatchRecord;
import stock.exchange.matcher.StockMatcher;
import stock.exchange.trade.TradeManager;

public class OrderBookManagerImpl implements OrderBookManager {

  private final Lock reader, writer;

  private final Int2ObjectMap<OrderBook> books;
  private final Supplier<StockMatcher> stockMatcherFactory;
  private final NonblockingNonFailingDownstream<OrderRecord> filledOrderDownstream;
  private final NonblockingNonFailingDownstream<TradeRecord> tradeDownstream;
  private final NonblockingNonFailingBiDownstream<OrderMatchRecord, Throwable> tradeExecutionRejected;

  public OrderBookManagerImpl(
      Supplier<StockMatcher> stockMatcherFactory,
      NonblockingNonFailingDownstream<OrderRecord> filledOrderDownstream,
      NonblockingNonFailingDownstream<TradeRecord> tradeDownstream,
      NonblockingNonFailingBiDownstream<OrderMatchRecord, Throwable> tradeExecutionRejected) {
    this.books = new Int2ObjectOpenHashMap<>();
    this.stockMatcherFactory = stockMatcherFactory;
    this.filledOrderDownstream = filledOrderDownstream;
    this.tradeDownstream = tradeDownstream;
    this.tradeExecutionRejected = tradeExecutionRejected;
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
          tradeDownstream,
          tradeExecutionRejected);
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