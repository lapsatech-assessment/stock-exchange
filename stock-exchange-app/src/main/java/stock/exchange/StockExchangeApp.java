package stock.exchange;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import stock.exchange.cmd.ShellCommandParser;
import stock.exchange.cmd.ShellCommandParserImpl;
import stock.exchange.shell.ShellTerminalConsole;
import stock.exchange.shell.StockExchangeShellRunner;
import stock.exchange.shell.TcpSocketTerminalService;

public class StockExchangeApp {

  public static void main(String[] args) throws InterruptedException, ExecutionException {

    ExecutorService pool = Executors.newFixedThreadPool(2);
    try (StockExchangeWorld stockExchangeWorld = new StockExchangeWorldImpl()) {

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