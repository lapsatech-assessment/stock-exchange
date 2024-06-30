package stock.exchange.shell;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ShellTerminalTcpSocket implements ShellTerminal {

  private final BufferedReader br;
  private final BufferedWriter pw;
  private final Socket socket;

  public ShellTerminalTcpSocket(Socket socket) throws IOException {
    this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    this.pw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    this.socket = socket;
    socket.setSoTimeout(1000);
  }

  @Override
  public String readLine() throws IOException {
    try {
      return br.readLine();
    } catch (InterruptedIOException e) {
      return null;
    }
  }

  @Override
  public void writeLine(String line) throws IOException {
    pw.write(line);
    pw.write(System.lineSeparator());
    pw.flush();
  }

  @Override
  public void onFinish() throws IOException {
    socket.close();
  }

}
