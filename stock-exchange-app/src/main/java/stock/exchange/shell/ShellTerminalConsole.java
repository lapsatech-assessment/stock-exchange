package stock.exchange.shell;

import java.io.Console;
import java.io.IOException;

public class ShellTerminalConsole implements ShellTerminal {

  private final Console console;

  public ShellTerminalConsole(Console console) {
    this.console = console;
  }

  @Override
  public String readLine() throws IOException {
    return console.readLine("> ");
  }

  @Override
  public void writeLine(String line) throws IOException {
    console.writer().println(line);
  }

  @Override
  public void onFinish() throws IOException {
  }
}
