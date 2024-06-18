package stock.exchange.domain;

public interface CompositeRecord extends InstrumentRecord {

  Iterable<? extends SecurityRecord> componenents();

}
