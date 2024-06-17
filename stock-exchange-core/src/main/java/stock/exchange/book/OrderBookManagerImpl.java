package stock.exchange.book;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import stock.exchange.NonblockingNonFailingDownstream;
import stock.exchange.NonblockingNonFailingJunkDownstream;
import stock.exchange.domain.InstrumentRecord;
import stock.exchange.domain.OrderMatchRecord;
import stock.exchange.domain.OrderRecord;
import stock.exchange.domain.SecurityRecord;
import stock.exchange.domain.TradeRecord;
import stock.exchange.instrument.MarketDataWrites;
import stock.exchange.matcher.StockMatcher;
import stock.exchange.trade.TradeManager;

public class OrderBookManagerImpl implements OrderBookManager {

  private final Lock reader, writer;

  private final Int2ObjectMap<OrderBook> books = new Int2ObjectOpenHashMap<>();
  private final Supplier<StockMatcher> stockMatcherFactory;

  private final TradeManager tradeManager;
  private final MarketDataWrites marketDataWrites;

  private final NonblockingNonFailingDownstream<OrderRecord> filledOrderDownstream;
  private final NonblockingNonFailingDownstream<TradeRecord> tradeDownstream;
  private final NonblockingNonFailingJunkDownstream<OrderMatchRecord> tradeExecutionRejected;
  private final NonblockingNonFailingJunkDownstream<TradeRecord> marketDataAcceptRejected;

  public OrderBookManagerImpl(
      Supplier<StockMatcher> stockMatcherFactory,
      TradeManager tradeManager,
      MarketDataWrites marketDataWrites,
      NonblockingNonFailingDownstream<OrderRecord> filledOrderDownstream,
      NonblockingNonFailingDownstream<TradeRecord> tradeDownstream,
      NonblockingNonFailingJunkDownstream<OrderMatchRecord> tradeExecutionRejected,
      NonblockingNonFailingJunkDownstream<TradeRecord> marketDataAcceptRejected) {
    this.stockMatcherFactory = stockMatcherFactory;
    this.tradeManager = tradeManager;
    this.marketDataWrites = marketDataWrites;
    this.filledOrderDownstream = filledOrderDownstream;
    this.tradeDownstream = tradeDownstream;
    this.tradeExecutionRejected = tradeExecutionRejected;
    this.marketDataAcceptRejected = marketDataAcceptRejected;
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
          stockMatcherFactory.get(),
          tradeManager,
          marketDataWrites,
          filledOrderDownstream,
          tradeDownstream,
          tradeExecutionRejected,
          marketDataAcceptRejected);
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