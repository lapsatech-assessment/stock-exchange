package stock.exchange.domain;

public record SecurityRecord(int id, String symbol, DoubleReference marketPrice)
    implements InstrumentRecord {
}
