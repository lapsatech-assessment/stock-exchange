package stock.exchange.matcher;

import stock.exchange.book.DuplicateOrderException;
import stock.exchange.book.OrderPartiallyFilledException;
import stock.exchange.domain.DoubleReference;

/**
 * StockMatcher is an abstraction of the order matching strategy in the trading
 * system.
 * 
 * The implementation is typically stateful, keeping all unfilled orders in
 * internal storage.
 * 
 * Implementations should guarantee the consistency and correctness of the trade
 * execution order.
 */
public interface StockMatcher {

  @FunctionalInterface
  interface OrderMatchedEventListener {

    /**
     * The method is called each time the order matching mechanism identifies a pair
     * of suitable orders for a trade
     * 
     * @param buyerOrderId     Buyer order ID
     * @param sellerOrderId    Seller order ID
     * @param quantity         Trade volume
     * @param buyerOrderPrice  Buy order price
     * @param sellerOrderPrice Sell order price
     */
    void onOrderMatched(long buyerOrderId, long sellerOrderId, int quantity, double buyerOrderPrice,
        double sellerOrderPrice);
  }

  @FunctionalInterface
  interface OrderPartiallyFilledEventListener {

    /**
     * The method is called each time the order matching mechanism determines that a
     * specific order in the queue is only partially executed and an unfilled volume
     * remains.
     * 
     * Calling this method typically means that the order still remains in the
     * internal queue of the TradeMatcher and will continue to participate in
     * matching for the remaining volume.
     * 
     * @param orderId      The order ID
     * @param volumeRemain Unfilled order volume
     */

    void onOrderPartialyFilled(long orderId, int volumeRemain);
  }

  @FunctionalInterface
  public interface OrderFulfilledEventListener {

    /**
     * The method is called each time the order matching mechanism determines that a
     * specific order in the queue is fully executed.
     * 
     * Calling this method typically means that the order will be removed from the
     * internal queue of the TradeMatcher and will no longer participate in matching
     * 
     * @param orderId The order ID
     */
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
   * The method executes the matching algorithm over all orders that are in the
   * internal queue of the TradeMatcher sends matching callback events if a match
   * occurs.
   * 
   * The method also updates its internal state by removing fully filled orders
   * from the queue and/or updating the remaining quantity of partially filled
   * orders.
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
