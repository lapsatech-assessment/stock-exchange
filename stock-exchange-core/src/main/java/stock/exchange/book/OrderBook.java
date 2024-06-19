package stock.exchange.book;

import stock.exchange.domain.OrderRecord;
import stock.exchange.domain.SecurityRecord;
import stock.exchange.domain.TraderRecord;

/**
 * Trading book interface for trade management.
 * 
 * This interface facilitates placing trade orders in a queue and cancelling
 * orders.
 * 
 * Additionally, this interface defines a method that initiates a single
 * iteration over the trades currently in the queue
 */
public interface OrderBook {

  SecurityRecord instrument();

  /**
   * Placing a market buy order
   * 
   * @throws OrderValidationException
   */
  OrderRecord addBuy(TraderRecord trader, int quantity);

  /**
   * Placing a market sell order
   * 
   * @throws OrderValidationException
   */
  OrderRecord addSell(TraderRecord trader, int quantity);

  /**
   * Placing a buy limit order at a specified price or lower
   * 
   * @throws OrderValidationException
   */
  OrderRecord addBid(TraderRecord trader, int quantity, double price);

  /**
   * Placing a sell limit order at a specified price or higher
   * 
   * @throws OrderValidationException
   */
  OrderRecord addAsk(TraderRecord trader, int quantity, double price);

  /**
   * Attempting to cancel an order and remove it from the order book queue.
   * 
   * In this implementation of the system, if the order has been partially filled,
   * it cannot be canceled
   * 
   * @throws NoSuchOrderException
   * @throws OrderPartiallyFilledException
   */
  OrderRecord removeOrder(long orderId);

  /**
   * The method returns an collection object containing all unfilled or partially filled
   * orders in the order book
   * 
   * @returns an iterable containing all unfilled or partially filled orders
   */
  Iterable<? extends OrderRecord> getActiveOrders();

  /**
   * The invocation of this method initiates a single iteration over the trades
   * currently in the queue
   */
  void tick();

  /**
   * The invocation of this method initiates the flushing of all existing data in
   * the queue and other critical operational data to the persistence store before
   * the application terminates. It is assumed that this data should be loaded
   * later during application startup
   */
  void flush();
}