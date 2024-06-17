package stock.exchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stock.exchange.instrument.OrderMatchRecord;

public class TradeExecutionFailedLoggerDownstream
    implements NonblockingNonFailingBiDownstream<OrderMatchRecord, Throwable> {

  private final Logger logger = LoggerFactory.getLogger("RejectedTrades");

  @Override
  public void accept(OrderMatchRecord t, Throwable m) {
    logger.info("TRADE EXECUTION REJECTED {} {} {}", t, m.getClass().getSimpleName(), m.getMessage(), m);
  }

}
