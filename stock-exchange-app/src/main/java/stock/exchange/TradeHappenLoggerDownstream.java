package stock.exchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stock.exchange.domain.TradeRecord;

public class TradeHappenLoggerDownstream implements NonblockingDownstream<TradeRecord> {

  private final Logger logger = LoggerFactory.getLogger("Trades");

  @Override
  public void accept(TradeRecord t) {
    logger.info(
        "TRADE {} {} buyer:{} seller:{} {} {} ",
        t.id(),
        t.instrument().symbol(),
        t.buyer().name(),
        t.seller().name(),
        t.quantity(),
        t.price());
  }

}
