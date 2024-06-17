package stock.exchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stock.exchange.domain.OrderRecord;
import stock.exchange.integration.NonblockingNonFailingDownstream;

public class OrderFulfilledLoggerDownstream implements NonblockingNonFailingDownstream<OrderRecord> {

  private final Logger logger = LoggerFactory.getLogger("Orders");

  @Override
  public void accept(OrderRecord t) {
    logger.info(
        "ORDER EXECUTED {} {} {} {} {} {}",
        t.id(),
        t.instrument().symbol(),
        t.type(),
        t.trader().name(),
        t.quantity(),
        t.price());
  }

}
