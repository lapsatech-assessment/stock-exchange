package stock.exchange;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import stock.exchange.book.DuplicateOrderBookException;
import stock.exchange.book.OrderBook;

public class StockMarketRunner implements AutoCloseable {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private static class OderBookRunner implements Runnable {

    private final Logger logger;
    private final Duration ticker;
    private final OrderBook book;
    private volatile boolean stopped = false;

    private OderBookRunner(OrderBook book, Duration ticker) {
      this.logger = LoggerFactory.getLogger(OderBookRunner.class + "." + book.instrument().symbol());
      this.book = book;
      this.ticker = ticker;
    }

    @Override
    public void run() {
      try {
        logger.info("Started");
        while (!stopped) {
          logger.debug("Tick");
          book.tick();
          try {
            Thread.sleep(ticker.toMillis(), ticker.toNanosPart());
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

  private final Collection<CompletableFuture<?>> processes = new ArrayList<>();
  private final IntSet instrumentsRunning = new IntArraySet();

  public synchronized void run(OrderBook book, Duration tickerDuration) {
    if (threadPools.isShutdown()) {
      throw new IllegalStateException("Market is shut down");
    }
    if (!instrumentsRunning.add(book.instrument().id())) {
      throw new DuplicateOrderBookException();
    }
    OderBookRunner runnable = new OderBookRunner(book, tickerDuration);
    processes.add(CompletableFuture.runAsync(runnable, threadPools));
  }

  public void shutdown() {
    threadPools.shutdownNow();
    CompletableFuture.allOf(processes.toArray(CompletableFuture[]::new)).join();
    try {
      threadPools.awaitTermination(60 * 1000, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      logger.warn("Unable to await termination of threads, got interrupted", e);
    }
  }

  @Override
  public void close() {
    shutdown();
  }
}
