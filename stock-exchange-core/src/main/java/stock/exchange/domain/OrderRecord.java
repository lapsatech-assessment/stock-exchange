package stock.exchange.domain;

import java.time.Instant;

public interface OrderRecord {
  long id();

  SecurityRecord security();

  OrderType type();

  TraderRecord trader();

  int quantity();

  double price();
  
  Instant timestamp();
}
