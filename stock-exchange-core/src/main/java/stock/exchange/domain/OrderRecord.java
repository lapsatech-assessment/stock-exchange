package stock.exchange.domain;

public interface OrderRecord {
  long id();

  SecurityRecord security();

  OrderType type();

  TraderRecord trader();

  int quantity();

  double price();
}
