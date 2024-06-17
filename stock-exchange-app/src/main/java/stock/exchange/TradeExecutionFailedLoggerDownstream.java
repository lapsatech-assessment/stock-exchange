package stock.exchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stock.exchange.domain.OrderMatchRecord;

public class TradeExecutionFailedLoggerDownstream
    implements NonblockingNonFailingJunkDownstream<OrderMatchRecord> {

  private final Logger logger = LoggerFactory.getLogger(TradeExecutionFailedLoggerDownstream.class);

  @Override
  public void accept(OrderMatchRecord t, Throwable m) {
    logger.info("TRADE EXECUTION REJECTED {} {} {}", t, m.getClass().getSimpleName(), m.getMessage(), m);
  }

}
