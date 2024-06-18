package stock.exchange.domain;

public interface OrderMatchRecord {

  SecurityRecord security();

  OrderRecord buyingOrder();

  OrderRecord sellingOrder();

  int quantity();

  double buyingPrice();

  double sellingPrice();

}
