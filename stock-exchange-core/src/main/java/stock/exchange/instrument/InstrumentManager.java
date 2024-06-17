package stock.exchange.instrument;

import stock.exchange.domain.CompositeRecord;
import stock.exchange.domain.InstrumentRecord;
import stock.exchange.domain.SecurityRecord;

public interface InstrumentManager {

  InstrumentRecord findInstrumentById(int instrumentId);

  InstrumentRecord findInstrumentBySymbol(String symbol);

  SecurityRecord createSecurity(int instrumentId, String symbol, double initialPrice);

  CompositeRecord createComposite(int instrumentId, String symbol, String[] componentSymbols);

  Iterable<InstrumentRecord> getInstruments();
}