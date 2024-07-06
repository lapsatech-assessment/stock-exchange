package stock.exchange.engine;

import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.runAsync;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import stock.exchange.book.DuplicateOrderBookException;
import stock.exchange.book.OrderBook;

public class StockMarketEngine implements AutoCloseable, OrderBookRunner {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final Object sync = new Object();

  private static class Runner implements Runnable {

    private final Logger logger;
    private final Duration tickerInterval;
    private final OrderBook book;
    private volatile boolean stopped = false;

    private Runner(OrderBook book, Duration tickerInterval) {
      this.logger = LoggerFactory.getLogger(Runner.class + "." + book.instrument().symbol());
      this.book = book;
      this.tickerInterval = tickerInterval;
    }

    @Override
    public void run() {
      try {
        logger.info("Started");
        while (!stopped) {
          logger.debug("Tick");
          try {
            book.tick();
          } catch (RuntimeException e) {
            logger.error("Unable to tick", e);
          }
          try {
            Thread.sleep(tickerInterval.toMillis(), tickerInterval.toNanosPart());
          } catch (InterruptedException e) {
            this.stopped = true;
          }
        }
      } finally {
        logger.info("Stopping");
        book.flush();
      }
    }
  }

  private final ExecutorService threadPools = Executors.newCachedThreadPool();

  private final ObjectList<CompletableFuture<?>> allProcesses = new ObjectArrayList<>();
  private final Int2ObjectMap<Runner> runners = new Int2ObjectArrayMap<>();

  @Override
  public void runOrderBook(OrderBook book, Duration tickerInterval) {
    synchronized (sync) {
      if (threadPools.isShutdown()) {
        throw new StockMarketIsShutdownException();
      }
      if (runners.containsKey(book.instrument().id())) {
        throw new DuplicateOrderBookException();
      }
      Runner runner = new Runner(book, tickerInterval);
      allProcesses.add(runAsync(runner, threadPools));
      runners.put(book.instrument().id(), runner);
    }
  }

  public boolean shutdown() {
    synchronized (sync) {
      threadPools.shutdownNow();
    }
    allOf(allProcesses.toArray(new CompletableFuture[allProcesses.size()])).join();
    try {
      return threadPools.awaitTermination(60 * 1000, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      logger.warn("Unable to await termination of threads, got interrupted", e);
      return false;
    }
  }

  @Override
  public void close() {
    shutdown();
  }
}
