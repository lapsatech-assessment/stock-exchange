package stock.exchange.shell;

import java.io.IOException;

public interface ShellTerminal {

  String readLine() throws IOException;

  void writeLine(String line) throws IOException;

  void onFinish() throws IOException;

}
