package stock.exchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stock.exchange.domain.TradeRecord;
import stock.exchange.integration.NonblockingNonFailingJunkDownstream;

public class RejectedMarketDataLoggerDownstream
    implements NonblockingNonFailingJunkDownstream<TradeRecord> {

  private final Logger logger = LoggerFactory.getLogger(RejectedMarketDataLoggerDownstream.class);

  @Override
  public void accept(TradeRecord t, Throwable m) {
    logger.info("MARKET DATA REJECTED {} {} {}", t, m.getClass().getSimpleName(), m.getMessage(), m);
  }

}
