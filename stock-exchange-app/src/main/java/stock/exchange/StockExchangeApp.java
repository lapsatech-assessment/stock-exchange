package stock.exchange;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stock.exchange.book.OrderBookManager;
import stock.exchange.book.OrderBookManagerImpl;
import stock.exchange.cmd.ShellCommandParser;
import stock.exchange.cmd.ShellCommandParserImpl;
import stock.exchange.domain.OrderRecord;
import stock.exchange.domain.TradeRecord;
import stock.exchange.instrument.InstrumentManager;
import stock.exchange.instrument.MarketDataWorld;
import stock.exchange.instrument.MarketDataWrites;
import stock.exchange.integration.AppendToFileDownstream;
import stock.exchange.integration.Downstream;
import stock.exchange.integration.FanOutDownstream;
import stock.exchange.shell.ShellTerminalConsole;
import stock.exchange.shell.StockExchangeShellRunner;
import stock.exchange.shell.TcpSocketTerminalService;
import stock.exchange.trade.TradeGeneratorImpl;
import stock.exchange.trader.TraderManager;
import stock.exchange.trader.TraderManagerImpl;

public class StockExchangeApp {

  public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
    Logger logger = LoggerFactory.getLogger(StockExchangeApp.class);

    ExecutorService pool = Executors.newFixedThreadPool(2);

    try (StockMarketRunner stockMarketRunner = new StockMarketRunner()) {

      MarketDataWorld marketDataWorld = new MarketDataWorld();
      InstrumentManager instrumentManager = marketDataWorld;
      MarketDataWrites marketDataWrites = marketDataWorld;

      Downstream<TradeRecord> tradesPostingToFileDownstream = new AppendToFileDownstream<>(
          Paths.get("trades.txt"),
          t -> {
            StringBuilder sb = new StringBuilder();
            sb.append("id:");
            sb.append(t.id());
            sb.append(",buyingOrder:");
            sb.append(t.buyingOrder().id());
            sb.append(",sellingOrder:");
            sb.append(t.sellingOrder().id());
            sb.append(",symbol:");
            sb.append(t.security().symbol());
            sb.append(",quantity:");
            sb.append(t.quantity());
            sb.append(",price:");
            sb.append(t.price());
            return sb.toString();
          });

      Downstream<OrderRecord> ordersPostingToFileDownstream = new AppendToFileDownstream<>(
          Paths.get("orders.txt"),
          t -> {
            StringBuilder sb = new StringBuilder();
            sb.append("id:");
            sb.append(t.id());
            sb.append(",trader:");
            sb.append(t.trader().name());
            sb.append(",type:");
            sb.append(t.type().name());
            sb.append(",instrument:");
            sb.append(t.security().symbol());
            sb.append(",quantity:");
            sb.append(t.quantity());
            sb.append(",price:");
            sb.append(t.price());
            return sb.toString();
          });

      OrderBookManager orderBookManager = new OrderBookManagerImpl(

          new TradeGeneratorImpl(

              new FanOutDownstream<>(
                  t -> logger.info("Trade executed {}", t),
                  marketDataWrites,
                  tradesPostingToFileDownstream),

              (t, e) -> logger.error("Trade event rejected by downstream {}", t, e)),

          (t, e) -> logger.error("Order match event rejected by downstream {}", t, e),

          new FanOutDownstream<>(
              t -> logger.info("Order fulfilled {}", t),
              ordersPostingToFileDownstream),

          (t, e) -> logger.error("Order fulfilled event rejected by downstream {}", t, e)

      );

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

    } finally

    {
      pool.shutdownNow();
      try {
        pool.awaitTermination(60 * 1000, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        System.err.println("Unable to complete termination of threads, got interrupted");
      }
    }
  }
}