package stock.exchange.matcher

import spock.lang.Specification
import spock.lang.Subject
import stock.exchange.book.OrderPartiallyFilledException
import stock.exchange.domain.DoubleReference
import stock.exchange.matcher.StockMatcher.OrderFulfilledEventListener
import stock.exchange.matcher.StockMatcher.OrderMatchedEventListener
import stock.exchange.matcher.StockMatcher.OrderPartiallyFilledEventListener

class StockMatcherImplTest extends Specification {

  def marketPriceRef = Stub(DoubleReference)
  def orderMatchListener = Mock(OrderMatchedEventListener)
  def orderPartiallyFilledEventListener = Mock(OrderPartiallyFilledEventListener)
  def orderFulfilledEventListener = Mock(OrderFulfilledEventListener)

  @Subject
  def subject = new StockMatcherImpl()

  def 'market orders only : two orders quantity1 = quantity2 are matched and fulfilled'() {

    given:
    marketPriceRef.getAsDouble() >>> [666.00d]
    subject.addOrderBuy(100001, 100)
    subject.addOrderSell(200001, 100)

    when:
    subject.match(marketPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100001, 200001, 100, 666.00d, 666.00d)
    1 * orderFulfilledEventListener.onOrderFulfilled(100001)
    1 * orderFulfilledEventListener.onOrderFulfilled(200001)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._
  }

  def 'market orders only : two orders quantity1 != quantity2 are matched and partial filled'() {
    given:
    marketPriceRef.getAsDouble() >>> [666.00d]
    subject.addOrderBuy(100001, 100)
    subject.addOrderSell(200001, 70)

    when:
    subject.match(marketPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100001, 200001, 70, 666.00d, 666.00d)
    1 * orderFulfilledEventListener.onOrderFulfilled(200001)
    1 * orderPartiallyFilledEventListener.onOrderPartialyFilled(100001, 30)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._
  }

  def 'market orders only : three orders are matched and fully filleеd in one tick'() {
    given:
    marketPriceRef.getAsDouble() >>> [666.00d]
    subject.addOrderBuy(100001, 100)
    subject.addOrderSell(200001, 70)
    subject.addOrderSell(200002, 30)
    
    when:
    subject.match(marketPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100001, 200001, 70, 666.00d, 666.00d)
    1 * orderPartiallyFilledEventListener.onOrderPartialyFilled(100001, 30)
    1 * orderFulfilledEventListener.onOrderFulfilled(200001)

    then:
    1 * orderMatchListener.onOrderMatched(100001, 200002, 30, 666.00d, 666.00d)
    1 * orderFulfilledEventListener.onOrderFulfilled(100001)
    1 * orderFulfilledEventListener.onOrderFulfilled(200002)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._
  }

  def 'market orders only : four orders are matched and fully filleеd in one ticks'() {
    given:
    marketPriceRef.getAsDouble() >>> [666.00d, 777.00d, 888.00d]
    subject.addOrderBuy(100001, 100)
    subject.addOrderSell(200001, 70)
    subject.addOrderSell(200002, 40)
    subject.addOrderBuy(100002, 10)

    when:
    subject.match(marketPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100001, 200001, 70, 666.00d, 666.00d)
    1 * orderPartiallyFilledEventListener.onOrderPartialyFilled(100001, 30)
    1 * orderFulfilledEventListener.onOrderFulfilled(200001)

    then:
    1 * orderMatchListener.onOrderMatched(100001, 200002, 30, 666.00d, 666.00d)
    1 * orderFulfilledEventListener.onOrderFulfilled(100001)
    1 * orderPartiallyFilledEventListener.onOrderPartialyFilled(200002, 10)

    then:
    1 * orderMatchListener.onOrderMatched(100002, 200002, 10, 666.00d, 666.00d)
    1 * orderFulfilledEventListener.onOrderFulfilled(100002)
    1 * orderFulfilledEventListener.onOrderFulfilled(200002)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._
  }

  def 'market orders only : four orders are matched and fully filleеd in three ticks'() {
    given:
    marketPriceRef.getAsDouble() >>> [666.00d, 777.00d, 888.00d]
    subject.addOrderBuy(100001, 100)
    subject.addOrderSell(200001, 70)
    
    when:
    subject.match(marketPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100001, 200001, 70, 666.00d, 666.00d)
    1 * orderPartiallyFilledEventListener.onOrderPartialyFilled(100001, 30)
    1 * orderFulfilledEventListener.onOrderFulfilled(200001)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._

    when:
    subject.addOrderSell(200002, 40)
    subject.match(marketPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100001, 200002, 30, 777.00d, 777.00d)
    1 * orderFulfilledEventListener.onOrderFulfilled(100001)
    1 * orderPartiallyFilledEventListener.onOrderPartialyFilled(200002, 10)
    
    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._

    when:
    subject.addOrderBuy(100002, 10)
    subject.match(marketPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100002, 200002, 10, 888.00d, 888.00d)
    1 * orderFulfilledEventListener.onOrderFulfilled(100002)
    1 * orderFulfilledEventListener.onOrderFulfilled(200002)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._
  }

  def 'limit orders only : two orders with bid = ask and quantity1 = quantity2 are matched and fulfilled'() {
    given:
    subject.addOrderBid(100001, 20.00, 100)
    subject.addOrderBid(100002, 30.00, 100) // <-- only match
    subject.addOrderBid(100003, 10.00, 100)

    subject.addOrderAsk(200001, 40.00, 100)
    subject.addOrderAsk(200002, 30.00, 100) // <-- only match
    subject.addOrderAsk(200003, 50.00, 100)

    when:
    subject.match(marketPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100002, 200002, 100, 30.00, 30.00)
    1 * orderFulfilledEventListener.onOrderFulfilled(100002)
    1 * orderFulfilledEventListener.onOrderFulfilled(200002)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._
  }

  def 'limit orders only : one of two orders with bid = ask and quantity1 = quantity2 is canceled and wil not fulfiled'() {
    given:
    subject.addOrderBid(100001, 20.00, 100)
    subject.addOrderBid(100002, 30.00, 100) // <-- the only match, but is going to canceled
    subject.addOrderBid(100003, 10.00, 100)

    subject.addOrderAsk(200001, 40.00, 100)
    subject.addOrderAsk(200002, 30.00, 100) // <-- only match
    subject.addOrderAsk(200003, 50.00, 100)

    when:
    subject.removeOrder(100002)
    subject.match(marketPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._
  }

  def 'limit orders only : two orders with bid > ask and quantity1 = quantity2 are matched and fulfilled'() {
    given:
    subject.addOrderBid(100001, 25.00, 100)
    subject.addOrderBid(100002, 35.00, 100) // <-- only match
    subject.addOrderBid(100003, 15.00, 100)

    subject.addOrderAsk(200001, 40.00, 100)
    subject.addOrderAsk(200002, 30.00, 100) // <-- only match
    subject.addOrderAsk(200003, 50.00, 100)

    when:
    subject.match(marketPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100002, 200002, 100, 35.00, 30.00)
    1 * orderFulfilledEventListener.onOrderFulfilled(100002)
    1 * orderFulfilledEventListener.onOrderFulfilled(200002)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._
  }

  def 'limit orders only : one of two orders with bid > ask and quantity1 = quantity2 is canceled and wil not fulfiled'() {
    given:
    subject.addOrderBid(100001, 25.00, 100)
    subject.addOrderBid(100002, 35.00, 100) // <-- the only match, but is going to canceled
    subject.addOrderBid(100003, 15.00, 100)

    subject.addOrderAsk(200001, 40.00, 100)
    subject.addOrderAsk(200002, 30.00, 100) // <-- only match
    subject.addOrderAsk(200003, 50.00, 100)

    when:
    subject.removeOrder(100002)
    subject.match(marketPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._
  }

  def 'limit orders only : two orders with bid = ask and quantity1 != quantity2 are matched and partial filled'() {
    given:
    subject.addOrderBid(100001, 20.00, 70)
    subject.addOrderBid(100002, 30.00, 70) // <-- match
    subject.addOrderBid(100003, 10.00, 70)

    subject.addOrderAsk(200001, 40.00, 100)
    subject.addOrderAsk(200002, 30.00, 100) // <-- match
    subject.addOrderAsk(200003, 50.00, 100)

    when:
    subject.match(marketPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100002, 200002, 70, 30.00, 30.00)
    1 * orderFulfilledEventListener.onOrderFulfilled(100002)
    1 * orderPartiallyFilledEventListener.onOrderPartialyFilled(200002, 30)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._
  }

  def 'limit orders only : one of two orders with bid = ask and quantity1 != quantity2 can not be canceled'() {
    given:
    subject.addOrderBid(100001, 20.00, 70)
    subject.addOrderBid(100002, 30.00, 70) // <-- match
    subject.addOrderBid(100003, 10.00, 70)

    subject.addOrderAsk(200001, 40.00, 100)
    subject.addOrderAsk(200002, 30.00, 100) // <-- match, cancel attempt to fail
    subject.addOrderAsk(200003, 50.00, 100)

    subject.match(marketPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    when:
    subject.removeOrder(200002)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._

    then:
    thrown(OrderPartiallyFilledException)
  }

  def 'limit orders only : two orders with bid > ask and quantity1 != quantity2 are matched and partial filled'() {
    given:
    subject.addOrderBid(100001, 25.00, 70)
    subject.addOrderBid(100002, 35.00, 70) // <-- only match
    subject.addOrderBid(100003, 15.00, 70)

    subject.addOrderAsk(200001, 40.00, 100)
    subject.addOrderAsk(200002, 30.00, 100) // <-- only match
    subject.addOrderAsk(200003, 50.00, 100)

    when:
    subject.match(marketPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100002, 200002, 70, 35.00, 30.00)
    1 * orderFulfilledEventListener.onOrderFulfilled(100002)
    1 * orderPartiallyFilledEventListener.onOrderPartialyFilled(200002, 30)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._
  }

  def 'limit orders only : orders with bid < ask are NOT matched'() {
    given:
    subject.addOrderBid(100001, 20.00, 70)
    subject.addOrderBid(100002, 30.00, 70)
    subject.addOrderBid(100003, 10.00, 70)
    subject.addOrderBid(100004, 20.00, 100)
    subject.addOrderBid(100005, 30.00, 100)
    subject.addOrderBid(100006, 10.00, 100)

    subject.addOrderAsk(200001, 40.50, 100)
    subject.addOrderAsk(200002, 30.50, 100)
    subject.addOrderAsk(200003, 50.50, 100)

    when:
    subject.match(marketPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._
  }

  def 'limit orders only : multiple bid orders with various price are matched to a single ask order in the order of price from highest to lowest'() {
    given:
    subject.addOrderBid(100001, 29.00, 1)
    subject.addOrderBid(100002, 30.00, 30) // <-- 3rd highest bid price match
    subject.addOrderBid(100003, 28.00, 1)
    subject.addOrderBid(100004, 31.00, 40) // <-- 2nd highest bid price match
    subject.addOrderBid(100005, 27.00, 1)
    subject.addOrderBid(100006, 32.00, 60) // <-- 1st highest bid price match

    subject.addOrderAsk(200001, 40.00, 400)
    subject.addOrderAsk(200002, 30.00, 300) // <-- match 1st, 2nd, 3rd
    subject.addOrderAsk(200003, 50.00, 500)

    when:
    subject.match(marketPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then: '1st'
    1 * orderMatchListener.onOrderMatched(100006, 200002, 60, 32.00, 30.00)
    1 * orderFulfilledEventListener.onOrderFulfilled(100006)
    1 * orderPartiallyFilledEventListener.onOrderPartialyFilled(200002, 240)

    then: '2nd'
    1 * orderMatchListener.onOrderMatched(100004, 200002, 40, 31.00, 30.00)
    1 * orderFulfilledEventListener.onOrderFulfilled(100004)
    1 * orderPartiallyFilledEventListener.onOrderPartialyFilled(200002, 200)

    then: '3rd'
    1 * orderMatchListener.onOrderMatched(100002, 200002, 30, 30.00, 30.00)
    1 * orderFulfilledEventListener.onOrderFulfilled(100002)
    1 * orderPartiallyFilledEventListener.onOrderPartialyFilled(200002, 170)

    then: 'none'
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._
  }

  def 'limit orders only : multiple bid orders with same price are matched to a single ask order in the order of adding'() {
    given:
    subject.addOrderBid(100001, 28.00, 1)
    subject.addOrderBid(100002, 30.00, 10) // <-- 1st earliest added
    subject.addOrderBid(100003, 28.00, 1)
    subject.addOrderBid(100004, 30.00, 40) // <-- 2nd earliest added
    subject.addOrderBid(100005, 27.00, 1)
    subject.addOrderBid(100006, 30.00, 60) // <-- 3rd earliest added

    subject.addOrderAsk(200001, 40.00, 400)
    subject.addOrderAsk(200002, 30.00, 300) // <-- match 1st, 2nd
    subject.addOrderAsk(200003, 50.00, 500)

    when:
    subject.match(marketPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then: '1st'
    1 * orderMatchListener.onOrderMatched(100002, 200002, 10, 30.00, 30.00)
    1 * orderFulfilledEventListener.onOrderFulfilled(100002)
    1 * orderPartiallyFilledEventListener.onOrderPartialyFilled(200002, 290)

    then: '2nd'
    1 * orderMatchListener.onOrderMatched(100004, 200002, 40, 30.00, 30.00)
    1 * orderFulfilledEventListener.onOrderFulfilled(100004)
    1 * orderPartiallyFilledEventListener.onOrderPartialyFilled(200002, 250)

    then: '3rd'
    1 * orderMatchListener.onOrderMatched(100006, 200002, 60, 30.00, 30.00)
    1 * orderFulfilledEventListener.onOrderFulfilled(100006)
    1 * orderPartiallyFilledEventListener.onOrderPartialyFilled(200002, 190)

    then: 'none'
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._
  }

  def 'mixed orders : limit order with bid price > market price is matched and fulfiled with market sell order'() {
    given:
    marketPriceRef.getAsDouble() >>> [20.00d]
    subject.addOrderBid(100001, 30.00, 100)
    subject.addOrderSell(200001, 100)

    when:
    subject.match(marketPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100001, 200001, 100, 30.00, 20.00)
    1 * orderFulfilledEventListener.onOrderFulfilled(100001)
    1 * orderFulfilledEventListener.onOrderFulfilled(200001)

    then: 'none'
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._
  }

  def 'mixed orders : limit order with ask price < market price is matched and fulfiled with market buy order'() {
    given:
    marketPriceRef.getAsDouble() >>> [20.00d]
    subject.addOrderBuy(100001, 100)
    subject.addOrderAsk(200001, 10.00, 100)
    
    when:
    subject.match(marketPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100001, 200001, 100, 20.00, 10.00)
    1 * orderFulfilledEventListener.onOrderFulfilled(100001)
    1 * orderFulfilledEventListener.onOrderFulfilled(200001)

    then: 'none'
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._
  }

  def 'mixed orders : limit order with bid price < market price is not matched'() {
    given:
    marketPriceRef.getAsDouble() >>> [30.00d]
    subject.addOrderBid(100001, 20.00, 100)
    subject.addOrderSell(200001, 100)

    when:
    subject.match(marketPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._
  }

  def 'mixed orders : limit order with ask price > market price is not matched'() {
    given:
    marketPriceRef.getAsDouble() >>> [10.00d]
    subject.addOrderBuy(100001, 100)
    subject.addOrderAsk(200001, 20.00, 100)
    
    when:
    subject.match(marketPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._
  }

}