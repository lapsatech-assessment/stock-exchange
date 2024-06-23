package stock.exchange.matcher

import spock.lang.Specification
import spock.lang.Subject
import stock.exchange.book.OrderPartiallyFilledException
import stock.exchange.domain.DoubleReference
import stock.exchange.matcher.StockMatcher.OrderFulfilledEventListener
import stock.exchange.matcher.StockMatcher.OrderMatchedEventListener
import stock.exchange.matcher.StockMatcher.OrderPartiallyFilledEventListener

class StockMatcherImplTest extends Specification {

  def maretPriceRef = {30.00d} as DoubleReference
  def orderMatchListener = Mock(OrderMatchedEventListener)
  def orderPartiallyFilledEventListener = Mock(OrderPartiallyFilledEventListener)
  def orderFulfilledEventListener = Mock(OrderFulfilledEventListener)

  @Subject
  def subject = new StockMatcherImpl()

  def 'market orders only : two orders quantity1 = quantity2 are matched and fulfilled'() {
    given:
    def matched
    subject.addOrderBuy(100001, 100)
    subject.addOrderSell(200001, 100)

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100001, 200001, 100)
    1 * orderFulfilledEventListener.onOrderFulfilled(100001)
    1 * orderFulfilledEventListener.onOrderFulfilled(200001)

    and:
    matched

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._

    and:
    !matched
  }

  def 'market orders only : two orders quantity1 != quantity2 are matched and partial filled'() {
    given:
    def matched
    subject.addOrderBuy(100001, 100)
    subject.addOrderSell(200001, 70)

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100001, 200001, 70)
    1 * orderFulfilledEventListener.onOrderFulfilled(200001)
    1 * orderPartiallyFilledEventListener.onOrderPartialyFilled(100001, 30)

    and:
    matched

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._

    and:
    !matched
  }

  def 'market orders only : three orders are matched and fully filleеd in one tick'() {
    given:
    def matched
    subject.addOrderBuy(100001, 100)
    subject.addOrderSell(200001, 70)
    subject.addOrderSell(200002, 30)

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100001, 200001, 70)
    1 * orderPartiallyFilledEventListener.onOrderPartialyFilled(100001, 30)
    1 * orderFulfilledEventListener.onOrderFulfilled(200001)

    and:
    matched

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100001, 200002, 30)
    1 * orderFulfilledEventListener.onOrderFulfilled(100001)
    1 * orderFulfilledEventListener.onOrderFulfilled(200002)

    and:
    matched

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._

    and:
    !matched
  }

  def 'market orders only : four orders are matched and fully filleеd in one ticks'() {
    given:
    def matched
    subject.addOrderBuy(100001, 100)
    subject.addOrderSell(200001, 70)
    subject.addOrderSell(200002, 40)
    subject.addOrderBuy(100002, 10)

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100001, 200001, 70)
    1 * orderPartiallyFilledEventListener.onOrderPartialyFilled(100001, 30)
    1 * orderFulfilledEventListener.onOrderFulfilled(200001)
    
    and:
    matched

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100001, 200002, 30)
    1 * orderFulfilledEventListener.onOrderFulfilled(100001)
    1 * orderPartiallyFilledEventListener.onOrderPartialyFilled(200002, 10)
    
    and:
    matched

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100002, 200002, 10)
    1 * orderFulfilledEventListener.onOrderFulfilled(100002)
    1 * orderFulfilledEventListener.onOrderFulfilled(200002)
    
    and:
    matched

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._

    and:
    !matched
  }

  def 'market orders only : four orders are matched and fully filleеd in three ticks'() {
    given:
    def matched
    subject.addOrderBuy(100001, 100)
    subject.addOrderSell(200001, 70)

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100001, 200001, 70)
    1 * orderPartiallyFilledEventListener.onOrderPartialyFilled(100001, 30)
    1 * orderFulfilledEventListener.onOrderFulfilled(200001)

    and:
    matched

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._
    
    and:
    !matched

    when:
    subject.addOrderSell(200002, 40)
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100001, 200002, 30)
    1 * orderFulfilledEventListener.onOrderFulfilled(100001)
    1 * orderPartiallyFilledEventListener.onOrderPartialyFilled(200002, 10)
    
    and:
    matched

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._

    and:
    !matched

    when:
    subject.addOrderBuy(100002, 10)
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100002, 200002, 10)
    1 * orderFulfilledEventListener.onOrderFulfilled(100002)
    1 * orderFulfilledEventListener.onOrderFulfilled(200002)

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    and:
    matched

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._

    and:
    !matched
  }

  def 'limit orders only : two orders with bid = ask and quantity1 = quantity2 are matched and fulfilled'() {
    given:
    def matched
    subject.addOrderBid(100001, 20.00, 100)
    subject.addOrderBid(100002, 30.00, 100) // <-- only match
    subject.addOrderBid(100003, 10.00, 100)

    subject.addOrderAsk(200001, 40.00, 100)
    subject.addOrderAsk(200002, 30.00, 100) // <-- only match
    subject.addOrderAsk(200003, 50.00, 100)

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100002, 200002, 100)
    1 * orderFulfilledEventListener.onOrderFulfilled(100002)
    1 * orderFulfilledEventListener.onOrderFulfilled(200002)
    
    and:
    matched

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._
    
    and:
    !matched
  }

  def 'limit orders only : one of two orders with bid = ask and quantity1 = quantity2 is canceled and wil not fulfiled'() {
    given:
    def matched
    subject.addOrderBid(100001, 20.00, 100)
    subject.addOrderBid(100002, 30.00, 100) // <-- the only match, but is going to canceled
    subject.addOrderBid(100003, 10.00, 100)

    subject.addOrderAsk(200001, 40.00, 100)
    subject.addOrderAsk(200002, 30.00, 100) // <-- only match
    subject.addOrderAsk(200003, 50.00, 100)

    when:
    subject.removeOrder(100002)
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._

    and:
    !matched
  }

  def 'limit orders only : two orders with bid > ask and quantity1 = quantity2 are matched and fulfilled'() {
    given:
    def matched
    subject.addOrderBid(100001, 25.00, 100)
    subject.addOrderBid(100002, 35.00, 100) // <-- only match
    subject.addOrderBid(100003, 15.00, 100)

    subject.addOrderAsk(200001, 40.00, 100)
    subject.addOrderAsk(200002, 30.00, 100) // <-- only match
    subject.addOrderAsk(200003, 50.00, 100)

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100002, 200002, 100)
    1 * orderFulfilledEventListener.onOrderFulfilled(100002)
    1 * orderFulfilledEventListener.onOrderFulfilled(200002)
    
    and:
    matched

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._

    and:
    !matched
  }

  def 'limit orders only : one of two orders with bid > ask and quantity1 = quantity2 is canceled and wil not fulfiled'() {
    given:
    def matched
    subject.addOrderBid(100001, 25.00, 100)
    subject.addOrderBid(100002, 35.00, 100) // <-- the only match, but is going to canceled
    subject.addOrderBid(100003, 15.00, 100)

    subject.addOrderAsk(200001, 40.00, 100)
    subject.addOrderAsk(200002, 30.00, 100) // <-- only match
    subject.addOrderAsk(200003, 50.00, 100)

    when:
    subject.removeOrder(100002)
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._

    and:
    !matched
  }

  def 'limit orders only : two orders with bid = ask and quantity1 != quantity2 are matched and partial filled'() {
    given:
    def matched
    subject.addOrderBid(100001, 20.00, 70)
    subject.addOrderBid(100002, 30.00, 70) // <-- match
    subject.addOrderBid(100003, 10.00, 70)

    subject.addOrderAsk(200001, 40.00, 100)
    subject.addOrderAsk(200002, 30.00, 100) // <-- match
    subject.addOrderAsk(200003, 50.00, 100)

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100002, 200002, 70)
    1 * orderFulfilledEventListener.onOrderFulfilled(100002)
    1 * orderPartiallyFilledEventListener.onOrderPartialyFilled(200002, 30)

    and:
    matched

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._

    and:
    !matched
  }

  def 'limit orders only : one of two orders with bid = ask and quantity1 != quantity2 can not be canceled'() {
    given:
    subject.addOrderBid(100001, 20.00, 70)
    subject.addOrderBid(100002, 30.00, 70) // <-- match
    subject.addOrderBid(100003, 10.00, 70)

    subject.addOrderAsk(200001, 40.00, 100)
    subject.addOrderAsk(200002, 30.00, 100) // <-- match, cancel attempt to fail
    subject.addOrderAsk(200003, 50.00, 100)

    subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

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
    def matched
    subject.addOrderBid(100001, 25.00, 70)
    subject.addOrderBid(100002, 35.00, 70) // <-- only match
    subject.addOrderBid(100003, 15.00, 70)

    subject.addOrderAsk(200001, 40.00, 100)
    subject.addOrderAsk(200002, 30.00, 100) // <-- only match
    subject.addOrderAsk(200003, 50.00, 100)

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100002, 200002, 70)
    1 * orderFulfilledEventListener.onOrderFulfilled(100002)
    1 * orderPartiallyFilledEventListener.onOrderPartialyFilled(200002, 30)

    and:
    matched

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._

    and:
    !matched
  }

  def 'limit orders only : orders with bid < ask are NOT matched'() {
    given:
    def matched
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
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._

    and:
    !matched
  }

  def 'limit orders only : multiple bid orders with various price are matched to a single ask order in the order of price from highest to lowest'() {
    given:
    def matched
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
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then: '1st'
    1 * orderMatchListener.onOrderMatched(100006, 200002, 60)
    1 * orderFulfilledEventListener.onOrderFulfilled(100006)
    1 * orderPartiallyFilledEventListener.onOrderPartialyFilled(200002, 240)

    and:
    matched

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then: '2nd'
    1 * orderMatchListener.onOrderMatched(100004, 200002, 40)
    1 * orderFulfilledEventListener.onOrderFulfilled(100004)
    1 * orderPartiallyFilledEventListener.onOrderPartialyFilled(200002, 200)
    
    and:
    matched

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then: '3rd'
    1 * orderMatchListener.onOrderMatched(100002, 200002, 30)
    1 * orderFulfilledEventListener.onOrderFulfilled(100002)
    1 * orderPartiallyFilledEventListener.onOrderPartialyFilled(200002, 170)
    
    and:
    matched

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then: 'none'
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._

    and:
    !matched
  }

  def 'limit orders only : multiple bid orders with same price are matched to a single ask order in the order of adding'() {
    given:
    def matched
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
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then: '1st'
    1 * orderMatchListener.onOrderMatched(100002, 200002, 10)
    1 * orderFulfilledEventListener.onOrderFulfilled(100002)
    1 * orderPartiallyFilledEventListener.onOrderPartialyFilled(200002, 290)

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then: '2nd'
    1 * orderMatchListener.onOrderMatched(100004, 200002, 40)
    1 * orderFulfilledEventListener.onOrderFulfilled(100004)
    1 * orderPartiallyFilledEventListener.onOrderPartialyFilled(200002, 250)
    
    and:
    matched

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then: '3rd'
    1 * orderMatchListener.onOrderMatched(100006, 200002, 60)
    1 * orderFulfilledEventListener.onOrderFulfilled(100006)
    1 * orderPartiallyFilledEventListener.onOrderPartialyFilled(200002, 190)
    
    and:
    matched

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then: 'none'
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._
    
    and:
    !matched
  }

  def 'mixed orders : limit order with bid price > market price is matched and fulfiled with market sell order'() {
    given:
    def matched
    subject.addOrderBid(100001, 30.00, 100)
    subject.addOrderSell(200001, 100)

    when:
    matched = subject.match({20.00d}, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100001, 200001, 100)
    1 * orderFulfilledEventListener.onOrderFulfilled(100001)
    1 * orderFulfilledEventListener.onOrderFulfilled(200001)

    and:
    matched

    when:
    matched = subject.match({20.00d}, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then: 'none'
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._

    and:
    !matched
  }

  def 'mixed orders : limit order with ask price < market price is matched and fulfiled with market buy order'() {
    given:
    def matched
    subject.addOrderBuy(100001, 100)
    subject.addOrderAsk(200001, 10.00, 100)

    when:
    matched = subject.match({20.00d}, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100001, 200001, 100)
    1 * orderFulfilledEventListener.onOrderFulfilled(100001)
    1 * orderFulfilledEventListener.onOrderFulfilled(200001)
    
    and:
    matched

    when:
    matched = subject.match({20.00d}, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then: 'none'
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._

    and:
    !matched
  }

  def 'mixed orders : limit order with bid price < market price is not matched'() {
    given:
    def matched
    subject.addOrderBid(100001, 20.00, 100)
    subject.addOrderSell(200001, 100)

    when:
    matched = subject.match({30.00d}, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._

    and:
    !matched
  }

  def 'mixed orders : limit order with ask price > market price is not matched'() {
    given:
    def matched
    subject.addOrderBuy(100001, 100)
    subject.addOrderAsk(200001, 20.00, 100)

    when:
    matched = subject.match({10.00d}, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._

    and:
    !matched
  }

  def 'remove order : succesful, no match'() {
    given:
    def matched
    subject.addOrderBuy(100001, 100)
    subject.addOrderSell(200001, 100)

    when:
    subject.removeOrder(200001)
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._

    and:
    !matched
  }

  def 'remove order : attempt to remove partially filled order causes to exception, still match'() {
    given:
    def matched
    subject.addOrderBuy(100001, 100)
    subject.addOrderSell(200001, 60)
    subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    when:
    subject.removeOrder(100001)

    then:
    thrown(OrderPartiallyFilledException)

    when:
    subject.addOrderSell(200002, 40)
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then:
    1 * orderMatchListener.onOrderMatched(100001, 200002, 40)
    1 * orderFulfilledEventListener.onOrderFulfilled(100001)
    1 * orderFulfilledEventListener.onOrderFulfilled(200002)

    and:
    matched

    when:
    matched = subject.match(maretPriceRef, orderMatchListener, orderPartiallyFilledEventListener, orderFulfilledEventListener)

    then: 'none'
    0 * orderMatchListener._
    0 * orderFulfilledEventListener._
    0 * orderPartiallyFilledEventListener._
    
    and:
    !matched
  }
}