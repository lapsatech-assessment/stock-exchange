package stock.exchange.book;

@FunctionalInterface
public interface OrderPartiallyFilledEventListener {

  void onOrderPartialyFilled(long orderId, int quantityLeft);
}