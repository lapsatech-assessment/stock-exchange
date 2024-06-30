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

public class TcpSocketTerminalService implements Runnable {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @FunctionalInterface
  public static interface SocketRunnerFactory {
    Runnable createRunner(Socket socket) throws IOException;
  }

  private final SocketRunnerFactory taskFactory;
  private final ExecutorService pool;
  private final int port;

  public TcpSocketTerminalService(
      SocketRunnerFactory taskFactory,
      int port) {
    this.pool = Executors.newCachedThreadPool();
    this.taskFactory = taskFactory;
    this.port = port;
  }

  @Override
  public void run() {
    ServerSocketFactory ssf = ServerSocketFactory.getDefault();
    try (ServerSocket ss = ssf.createServerSocket(port)) {
      ss.setSoTimeout(500);
      while (!Thread.interrupted()) {
        Socket socket;
        try {
          socket = ss.accept();
        } catch (java.net.SocketTimeoutException e) {
          continue;
        }
        pool.submit(taskFactory.createRunner(socket));
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
