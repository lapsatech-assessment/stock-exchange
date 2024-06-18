package stock.exchange.integration;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Function;

public class AppendToFileDownstream<T> implements Downstream<T>, AutoCloseable {

  private final BufferedWriter bw;
  private final Function<T, String> toStringConverter;
  private final Object sync = new Object();

  public AppendToFileDownstream(Path file, Function<T, String> toStringConverter) throws IOException {
    this.bw = Files.newBufferedWriter(file, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    this.toStringConverter = toStringConverter;
  }

  public AppendToFileDownstream(Path file) throws IOException {
    this(file, String::valueOf);
  }

  @Override
  public void accept(T t) {
    String st = toStringConverter.apply(t);
    try {
      synchronized (sync) {
        bw.write(st);
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
