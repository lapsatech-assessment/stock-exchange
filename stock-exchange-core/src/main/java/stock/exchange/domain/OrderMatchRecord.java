package stock.exchange.domain;

public interface OrderMatchRecord {

  double marketPrice();

  SecurityRecord security();

  OrderRecord buyerOrder();

  OrderRecord sellerOrder();

  int quantity();
}
