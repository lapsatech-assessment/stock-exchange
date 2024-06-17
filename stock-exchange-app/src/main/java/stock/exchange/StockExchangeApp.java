package stock.exchange;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import stock.exchange.book.OrderBookImpl;
import stock.exchange.book.OrderBookManager;
import stock.exchange.book.OrderBookManagerImpl;
import stock.exchange.cmd.ShellCommandParser;
import stock.exchange.cmd.ShellCommandParserImpl;
import stock.exchange.domain.OrderMatchRecord;
import stock.exchange.domain.OrderRecord;
import stock.exchange.domain.TradeRecord;
import stock.exchange.instrument.InstrumentManager;
import stock.exchange.instrument.MarketDataWorld;
import stock.exchange.instrument.MarketDataWrites;
import stock.exchange.integration.FanOutDownstream;
import stock.exchange.integration.NonblockingNonFailingDownstream;
import stock.exchange.integration.NonblockingNonFailingJunkDownstream;
import stock.exchange.integration.TradeDataToMarketDataDownstream;
import stock.exchange.matcher.StockMatcherImpl;
import stock.exchange.shell.ShellTerminalConsole;
import stock.exchange.shell.StockExchangeShellRunner;
import stock.exchange.shell.TcpSocketTerminalService;
import stock.exchange.trade.TradeGenerator;
import stock.exchange.trade.TradeManagerImpl;
import stock.exchange.trader.TraderManager;
import stock.exchange.trader.TraderManagerImpl;

public class StockExchangeApp {

  public static void main(String[] args) throws InterruptedException, ExecutionException {

    ExecutorService pool = Executors.newFixedThreadPool(2);

    try (StockMarketRunner stockMarketRunner = new StockMarketRunner()) {

      MarketDataWorld marketDataWorld = new MarketDataWorld();
      InstrumentManager instrumentManager = marketDataWorld;
      MarketDataWrites marketDataWrites = marketDataWorld;

      NonblockingNonFailingDownstream<TradeRecord> tradeDownstream = new FanOutDownstream<>(
          new TradeDataToMarketDataDownstream(marketDataWrites),
          new TradeHappenLoggerDownstream());
      NonblockingNonFailingJunkDownstream<TradeRecord> tradeRejectedDownstream = new RejectedMarketDataLoggerDownstream();
      TradeGenerator tradeGenerator = new TradeManagerImpl(tradeDownstream, tradeRejectedDownstream);

      NonblockingNonFailingDownstream<OrderRecord> filledOrderDownstream = new OrderFulfilledLoggerDownstream();
      NonblockingNonFailingJunkDownstream<OrderMatchRecord> tradeExecutionRejected = new TradeExecutionFailedLoggerDownstream();

      OrderBookManager orderBookManager = new OrderBookManagerImpl(
          security -> new OrderBookImpl(
              security,
              new StockMatcherImpl(),
              tradeGenerator,
              filledOrderDownstream,
              tradeExecutionRejected));

      TraderManager traderManager = new TraderManagerImpl();

      StockExchangeFacade stockExchangeFacade = new StockExchangeFacadeImpl(
          instrumentManager,
          traderManager,
          stockMarketRunner,
          orderBookManager);

      ShellCommandParser shellCommandParser = new ShellCommandParserImpl(stockExchangeFacade);

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
        System.err.println("Unable to complete termination of threads, got interrupted");
      }
    }
  }
}