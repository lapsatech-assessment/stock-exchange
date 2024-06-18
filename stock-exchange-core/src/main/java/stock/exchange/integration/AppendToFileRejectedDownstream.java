package stock.exchange.integration;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Function;

public class AppendToFileRejectedDownstream<T> implements RejectedDownstream<T>, AutoCloseable {

  private final BufferedWriter bw;
  private final Function<T, String> toStringConverter;
  private final Function<Throwable, String> exceptionToStringConverter;
  private final Object sync = new Object();

  public AppendToFileRejectedDownstream(Path file, Function<T, String> toStringConverter,
      Function<Throwable, String> exceptionToStringConverter) throws IOException {
    this.bw = Files.newBufferedWriter(file, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    this.toStringConverter = toStringConverter;
    this.exceptionToStringConverter = exceptionToStringConverter;
  }

  public AppendToFileRejectedDownstream(Path file) throws IOException {
    this(file, String::valueOf, Throwable::getMessage);
  }

  @Override
  public void accept(T t, Throwable cause) {
    String st = toStringConverter.apply(t);
    String scause = exceptionToStringConverter.apply(cause);
    try {
      synchronized (sync) {
        bw.write(st);
        bw.write(' ');
        bw.write(scause);
        bw.newLine();
        bw.flush();
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void close() throws IOException {
    bw.close();
  }

}
