package stock.exchange.domain;

public record CompositeRecord(int id, String symbol, DoubleReference marketPrice, Iterable<SecurityRecord> componenents)
    implements InstrumentRecord {
}
