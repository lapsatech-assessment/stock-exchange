package stock.exchange.matcher;

import stock.exchange.book.DuplicateOrderException;
import stock.exchange.book.OrderPartiallyFilledException;
import stock.exchange.domain.DoubleReference;

/**
 * SotckMatcher is an abstract implementation of orders matching algorithm in
 * the trade system
 * 
 * The implementation is usually supposed to be stateful keeping all not filled
 * orders in the internal storage
 * 
 * 
 * Implementations should guarantee consistency and correctness of trades
 * execution order as well as it should guarantee it's internal state
 */
public interface StockMatcher {

  public static StockMatcher newInstance() {
    return new StockMatcherImpl();
  }

  @FunctionalInterface
  interface OrderMatchedEventListener {

    /**
     * The method should be exceptions free
     */
    void onOrderMatched(long buyingOrderId, long sellingOrderId, int quantity, double buyingPrice, double sellingPrice);
  }

  @FunctionalInterface
  interface OrderPartiallyFilledEventListener {

    void onOrderPartialyFilled(long orderId, int quantityLeft);
  }

  @FunctionalInterface
  public interface OrderFulfilledEventListener {

    void onOrderFulfilled(long orderId);
  }

  /**
   * The method adds 'Buy' Limit Order to the queue
   * 
   * @param orderId  unique order id
   * @param price    buy price
   * @param quantity buy quantity
   * @throws DuplicateOrderException if order with the given id is in the queue
   *                                 already
   */
  void addOrderBid(long orderId, double price, int quantity);

  /**
   * The method adds 'Sell' Limit Order to the queue
   * 
   * @param orderId  unique order id
   * @param price    sell price
   * @param quantity sell quantity
   * @throws DuplicateOrderException if order with the given id is in the queue
   *                                 already
   */
  void addOrderAsk(long orderId, double price, int quantity);

  /**
   * The method adds 'Buy' Market Order to the queue
   * 
   * @param orderId  unique order id
   * @param quantity buy quantity
   * @throws DuplicateOrderException if order with the given id is in the queue
   *                                 already
   */
  void addOrderBuy(long orderId, int quantity);

  /**
   * The method adds 'Sell' Market Order to the queue
   * 
   * @param orderId  unique order id
   * @param quantity sell quantity
   * @throws DuplicateOrderException if order with the given id is in the queue
   *                                 already
   */
  void addOrderSell(long orderId, int quantity);

  /**
   * The method attemps to remove (cancel) a given order from the matcher queue If
   * any of the exceptions below thrown means no order is removed (canceled) from
   * the queue
   * 
   * @param orderId the order id of the order being removed (canceled)
   * @throws OrderPartiallyFilledException if the order is already partially
   *                                       executed and can'not be rmoved
   *                                       (canceled)
   * @return true if the requested order actually removed from the matcher or
   *         false if the order was not in the matcher queue befure the call
   */
  boolean removeOrder(long orderId);

  /**
   * Methods executes matching algorithm over all orders added to the matcher
   * previously and sends matching callback events if match occurs
   * 
   * The method also updates its internal state by removing fully filled orders
   * from the queue and/or updates the remaining quantity of partially filled
   * queues
   * 
   * @param marketPriceRef                    the reference to the current market
   *                                          price for the instrument
   * @param orderMatchedEventListener         the event listener receives event on
   *                                          every trade match event
   * @param orderPartiallyFilledEventListener the event listener receives event on
   *                                          partially filled order event
   * @param orderFulfilledEventListener       the event listener receives event on
   *                                          fuly filled order event
   */
  void match(
      DoubleReference marketPriceRef,
      OrderMatchedEventListener orderMatchedEventListener,
      OrderPartiallyFilledEventListener orderPartiallyFilledEventListener,
      OrderFulfilledEventListener orderFulfilledEventListener);
}
