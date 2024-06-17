package stock.exchange.domain;

public interface InstrumentRecord {

  int id();

  String symbol();

  DoubleReference marketPrice();

}
