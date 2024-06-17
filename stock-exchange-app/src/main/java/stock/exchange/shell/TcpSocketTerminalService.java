package stock.exchange.shell;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ServerSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stock.exchange.cmd.ShellCommandParser;

public class TcpSocketTerminalService implements Runnable {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final ShellCommandParser shellCommandParser;
  private final ExecutorService pool;
  private volatile boolean stopped = false;
  private final int port;

  public TcpSocketTerminalService(
      ShellCommandParser shellCommandParser,
      int port) {
    this.pool = Executors.newCachedThreadPool();
    this.shellCommandParser = shellCommandParser;
    this.port = port;
  }

  @Override
  public void run() {
    ServerSocketFactory ssf = ServerSocketFactory.getDefault();
    try (ServerSocket ss = ssf.createServerSocket(port)) {
      ss.setSoTimeout(500);
      while (!Thread.interrupted() && !stopped) {
        Socket socket;
        try {
          socket = ss.accept();
        } catch (java.net.SocketTimeoutException e) {
          continue;
        }
        pool.submit(new StockExchangeShellRunner(shellCommandParser, new ShellTerminalTcpSocket(socket)));
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } finally {
      pool.shutdownNow();
      try {
        pool.awaitTermination(10000, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        logger.warn("Unable to await termination of threads, got interrupted", e);
      }
    }
  }

}
