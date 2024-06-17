package stock.exchange;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import stock.exchange.book.OrderBookManagerImpl;
import stock.exchange.cmd.ShellCommandParser;
import stock.exchange.cmd.ShellCommandParserImpl;
import stock.exchange.instrument.InstrumentManager;
import stock.exchange.instrument.MarketDataWorld;
import stock.exchange.instrument.MarketDataWrites;
import stock.exchange.matcher.StockMatcherImpl;
import stock.exchange.shell.ShellTerminalConsole;
import stock.exchange.shell.StockExchangeShellRunner;
import stock.exchange.shell.TcpSocketTerminalService;
import stock.exchange.trade.TradeManagerImpl;
import stock.exchange.trader.TraderManagerImpl;

public class StockExchangeApp {

  public static void main(String[] args) throws InterruptedException, ExecutionException {

    MarketDataWorld marketDataWorld = new MarketDataWorld();
    InstrumentManager instrumentManager = marketDataWorld;
    MarketDataWrites marketDataWrites = marketDataWorld;

    ExecutorService pool = Executors.newFixedThreadPool(2);

    try (StockExchangeWorld stockExchangeWorld = new StockExchangeWorldImpl(
        instrumentManager,
        new TraderManagerImpl(),
        new StockMarketRunnerImpl(),
        new OrderBookManagerImpl(
            StockMatcherImpl::new,
            new TradeManagerImpl(),
            marketDataWrites,
            new OrderFulfilledLoggerDownstream(),
            new TradeHappenLoggerDownstream(),
            new TradeExecutionFailedLoggerDownstream(),
            new RejectedMarketDataLoggerDownstream()))) {

      ShellCommandParser shellCommandParser = new ShellCommandParserImpl(stockExchangeWorld);

      CompletableFuture.runAsync(
          new TcpSocketTerminalService(shellCommandParser, 7070),
          pool);

      CompletableFuture.runAsync(
          new StockExchangeShellRunner(
              shellCommandParser,
              new ShellTerminalConsole(System.console())),
          pool)
          .join();

    } finally {
      pool.shutdownNow();
      try {
        pool.awaitTermination(60 * 1000, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        System.err.println("Unable to await termination of threads, got interrupted");
      }
    }
  }

}