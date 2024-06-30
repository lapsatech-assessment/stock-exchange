package stock.exchange.shell;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stock.exchange.cmd.ShellCommandExecutor;

public class StockExchangeShellTerminalRunner implements Runnable {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final ShellCommandExecutor shellCommandExecutor;
  private final ShellTerminal shellTerminal;

  private volatile boolean stopped = false;

  public StockExchangeShellTerminalRunner(ShellCommandExecutor shellCommandExecutor, ShellTerminal shellTerminal) {
    this.shellCommandExecutor = shellCommandExecutor;
    this.shellTerminal = shellTerminal;
  }

  @Override
  public void run() {
    try {
      while (!Thread.interrupted() && !stopped) {
        String line = shellTerminal.readLine();
        if (line == null) {
          continue;
        }
        String message = shellCommandExecutor.execute(line);
        if (ShellCommandExecutor.BYE_STRING.equals(message)) {
          shellTerminal.writeLine("See ya!");
          stopped = true;
        } else {
          shellTerminal.writeLine(message);
        }
      }
    } catch (IOException e1) {
      logger.debug(e1.getMessage());
    } finally {
      try {
        shellTerminal.onFinish();
      } catch (IOException e) {
        logger.debug(e.getMessage());
      }
    }
  }

}
