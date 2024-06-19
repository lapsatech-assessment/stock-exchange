package stock.exchange.instrument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import stock.exchange.domain.CompositeRecord;
import stock.exchange.domain.DoubleReference;
import stock.exchange.domain.InstrumentRecord;
import stock.exchange.domain.SecurityRecord;

/**
 * A simple implementation of a market data service includes functions for
 * managing instruments, processing incoming trade data, and retrieving current
 * quote information.
 * 
 * In a production enterprise system, these implementations would likely be
 * distributed across different components. However, for a prototype project,
 * combining all functions into one may be sufficient.
 */
public class MarketDataWorld implements MarketDataReads, MarketDataWrites, InstrumentManager {

  private final Lock reader, writer;
  private final Int2ObjectMap<DoubleReference> marketPrices;
  private final Int2ObjectMap<InstrumentRecord> instrumentsById;
  private final Object2ObjectMap<String, InstrumentRecord> instrumentsBySymbol;

  public MarketDataWorld() {
    this.marketPrices = new Int2ObjectOpenHashMap<>();
    this.instrumentsById = new Int2ObjectOpenHashMap<>();
    this.instrumentsBySymbol = new Object2ObjectOpenHashMap<>();
    ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
    this.reader = rw.readLock();
    this.writer = rw.writeLock();
  }

  @Override
  public Iterable<? extends InstrumentRecord> getAllInstruments() {
    reader.lock();
    try {
      List<InstrumentRecord> records = new ArrayList<>(instrumentsById.size());
      records.addAll(instrumentsById.values());
      return Collections.unmodifiableList(records);
    } finally {
      reader.unlock();
    }
  }

  @Override
  public InstrumentRecord findInstrumentById(int instrumentId) {
    reader.lock();
    try {
      InstrumentRecord rec = instrumentsById.get(instrumentId);
      if (rec == null) {
        throw new NoSuchInstrumentException(instrumentId);
      }
      return rec;
    } finally {
      reader.unlock();
    }
  }

  @Override
  public InstrumentRecord findInstrumentBySymbol(String symbol) {
    reader.lock();
    try {
      InstrumentRecord rec = instrumentsBySymbol.get(symbol);
      if (rec == null) {
        throw new NoSuchInstrumentException(symbol);
      }
      return rec;
    } finally {
      reader.unlock();
    }
  }

  @Override
  public DoubleReference getMarketPriceRef(int instrumentId) {
    reader.lock();
    try {
      DoubleReference ref = marketPrices.get(instrumentId);
      if (ref == null) {
        throw new NoSuchInstrumentException(instrumentId);
      }
      return ref;
    } finally {
      reader.unlock();
    }
  }

  private record Security(
      int id,
      String symbol,
      DoubleReference marketPrice) implements SecurityRecord {
  }

  @Override
  public SecurityRecord createSecurity(
      int instrumentId,
      String symbol,
      double initialPrice) {
    writer.lock();
    try {
      if (instrumentsById.containsKey(instrumentId)) {
        throw new DuplicateInstrumentException();
      }
      if (instrumentsBySymbol.containsKey(symbol)) {
        throw new DuplicateInstrumentException();
      }
      Mutable marketPrice = new Mutable(initialPrice);
      SecurityRecord rec = new Security(instrumentId, symbol, marketPrice);

      marketPrices.put(instrumentId, marketPrice);
      instrumentsById.put(instrumentId, rec);
      instrumentsBySymbol.put(symbol, rec);

      return rec;
    } finally {
      writer.unlock();
    }
  }

  private record Composite(
      int id,
      String symbol,
      DoubleReference marketPrice,
      Iterable<SecurityRecord> componenents) implements CompositeRecord {
  }

  @Override
  public Composite createComposite(int compositeId, String symbol, String[] componentSymbols) {
    writer.lock();
    try {
      if (instrumentsById.containsKey(compositeId)) {
        throw new DuplicateInstrumentException();
      }
      if (instrumentsBySymbol.containsKey(symbol)) {
        throw new DuplicateInstrumentException();
      }

      SecurityRecord[] securities = new SecurityRecord[componentSymbols.length];
      DoubleReference[] securityPrices = new DoubleReference[componentSymbols.length];
      for (int i = 0; i < componentSymbols.length; i++) {
        if (instrumentsBySymbol.get(componentSymbols[i]) instanceof SecurityRecord sr) {
          securities[i] = sr;
          securityPrices[i] = sr.marketPrice();
        } else {
          throw new NoSuchSecurityException(componentSymbols[i]);
        }
      }

      DynamicAverage marketPrice = new DynamicAverage(securityPrices);
      Composite rec = new Composite(
          compositeId,
          symbol,
          marketPrice,
          Collections.unmodifiableList(Arrays.asList(securities)));

      marketPrices.put(compositeId, marketPrice);
      instrumentsById.put(compositeId, rec);
      instrumentsBySymbol.put(symbol, rec);

      return rec;
    } finally {
      writer.unlock();
    }
  }

  @Override
  public void acceptLastTradePrice(int securityId, double price, int quantity) {
    DoubleReference ref = getMarketPriceRef(securityId);
    if (ref instanceof Mutable mdr) {
      mdr.value = price;
    } else {
      throw new NoSuchSecurityException(securityId);
    }
  }

  private static class DynamicAverage implements DoubleReference {

    private final DoubleReference[] components;

    private DynamicAverage(DoubleReference[] components) {
      this.components = components;
    }

    @Override
    public double getAsDouble() {
      double d = 0d;
      for (int i = 0; i < components.length; i++) {
        d += components[i].getAsDouble();
      }
      return d / components.length;
    }

    @Override
    public String toString() {
      return String.valueOf(getAsDouble());
    }

  }

  private static class Mutable implements DoubleReference {

    private volatile double value;

    private Mutable(double value) {
      this.value = value;
    }

    @Override
    public double getAsDouble() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }
}