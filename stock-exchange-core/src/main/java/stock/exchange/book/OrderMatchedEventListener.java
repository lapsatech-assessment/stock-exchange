package stock.exchange.book;

@FunctionalInterface
public interface OrderMatchedEventListener {

  void onOrderMatched(long buyersOrderId, long sellersOrderId, int quantity, double buyerPrice, double sellerPrice);
}