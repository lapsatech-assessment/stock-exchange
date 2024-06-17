package stock.exchange.book;

@FunctionalInterface
public interface OrderFulfilledEventListener {

  void onOrderFulfilled(long orderId);
}