package stock.exchange.domain;

public interface TradeRecord {

  long id();

  SecurityRecord security();

  OrderRecord buyingOrder();

  OrderRecord sellingOrder();

  double price();

  int quantity();
}
