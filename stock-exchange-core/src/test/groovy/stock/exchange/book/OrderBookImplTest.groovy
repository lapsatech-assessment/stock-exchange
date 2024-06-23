package stock.exchange.book

import java.util.concurrent.Executors
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.TimeUnit

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout
import spock.lang.Unroll
import stock.exchange.domain.DoubleReference
import stock.exchange.domain.OrderMatchRecord
import stock.exchange.domain.OrderType
import stock.exchange.domain.SecurityRecord
import stock.exchange.domain.TraderRecord
import stock.exchange.integration.Downstream
import stock.exchange.integration.RejectedDownstream
import stock.exchange.matcher.StockMatcher
import stock.exchange.matcher.StockMatcher.OrderFulfilledEventListener
import stock.exchange.matcher.StockMatcher.OrderMatchedEventListener

class OrderBookImplTest extends Specification {

  @Shared
  def trader1 = Stub(TraderRecord)

  @Shared
  def trader2 = Stub(TraderRecord)

  @Shared
  def trader3 = Stub(TraderRecord)

  def stockMatcher = Mock(StockMatcher)
  def security = Stub(SecurityRecord) {
    marketPrice() >> Stub(DoubleReference) {
      getAsDouble() >> 666.00d
    }
  }
  def orderMatchDownstream = Mock(Downstream)
  def orderMatchRejectedDownstream = Mock(RejectedDownstream)
  def filledOrderDownstream = Mock(Downstream)
  def filledOrderDownstreamRejected = Mock(RejectedDownstream)

  def OrderBookImpl subject = new OrderBookImpl(
  stockMatcher,
  security,
  orderMatchDownstream,
  orderMatchRejectedDownstream,
  filledOrderDownstream,
  filledOrderDownstreamRejected)

  @Unroll
  def 'successful add new #type order'(def type, def Closure addClosure, def typeExpected, def priceEpxected) {
    when:
    def order1 = addClosure.call(subject)

    then:
    order1.security() == security
    order1.type() == typeExpected
    order1.trader() == trader1
    order1.quantity() == 100
    order1.price() == priceEpxected

    expect:
    subject.getActiveOrders().collect() == [order1]

    where:
    type   | addClosure                       | typeExpected   | priceEpxected
    'buy'  | { it.addBuy(trader1, 100) }      | OrderType.BUY  | Double.NaN
    'sell' | { it.addSell(trader1, 100) }     | OrderType.SELL | Double.NaN
    'bid'  | { it.addBid(trader1, 100, 10d) } | OrderType.BID  | 10d
    'ask'  | { it.addAsk(trader1, 100, 10d) } | OrderType.ASK  | 10d
  }

  @Unroll
  def 'failed to add new #type order due to #cause'(def type, def Closure addClosure, def cause, def expectException) {
    when:
    def order1 = addClosure.call(subject)

    then:
    thrown(expectException)

    expect:
    subject.getActiveOrders().collect() == []

    where:
    type   | addClosure                        | cause                 | expectException
    'buy'  | { it.addBuy(trader1, 0) }         | 'zero quantity'       | OrderQuantityValidationException
    'sell' | { it.addSell(trader1, 0) }        | 'zero quantity'       | OrderQuantityValidationException
    'bid'  | { it.addBid(trader1, 0, 10d) }    | 'zero quantity'       | OrderQuantityValidationException
    'ask'  | { it.addAsk(trader1, 0, 10d) }    | 'zero quantity'       | OrderQuantityValidationException
    'buy'  | { it.addBuy(trader1, -10) }       | 'below zero quantity' | OrderQuantityValidationException
    'sell' | { it.addSell(trader1, -10) }      | 'below zero quantity' | OrderQuantityValidationException
    'bid'  | { it.addBid(trader1, -10, 10d) }  | 'below zero quantity' | OrderQuantityValidationException
    'ask'  | { it.addAsk(trader1, -10, 10d) }  | 'below zero quantity' | OrderQuantityValidationException
    'bid'  | { it.addBid(trader1, 100, 0d) }   | 'zero price'          | OrderPriceValidationException
    'ask'  | { it.addAsk(trader1, 100, 0d) }   | 'zero price'          | OrderPriceValidationException
    'bid'  | { it.addBid(trader1, 100, -10d) } | 'below zero price'    | OrderPriceValidationException
    'ask'  | { it.addAsk(trader1, 100, -10d) } | 'below zero price'    | OrderPriceValidationException
    'buy'  | { it.addBuy(null, 100) }          | 'trader is null'      | OrderTraderValidationException
    'sell' | { it.addSell(null, 100) }         | 'trader is null'      | OrderTraderValidationException
    'bid'  | { it.addBid(null, 100, 10d) }     | 'trader is null'      | OrderTraderValidationException
    'ask'  | { it.addAsk(null, 100, 10d) }     | 'trader is null'      | OrderTraderValidationException
  }

  def 'added orders are passed to the underlying trade matcher in the order of submission'() {
    given:
    def order1 = subject.addBuy(trader1, 100)
    def order2 = subject.addSell(trader1, 200)
    def order3 = subject.addBid(trader1, 300, 10d)
    def order4 = subject.addAsk(trader1, 400, 20d)

    when:
    subject.tick()

    then:
    1 * stockMatcher.addOrderBuy(order1.id(), 100)

    then:
    1 * stockMatcher.addOrderSell(order2.id(), 200)

    then:
    1 * stockMatcher.addOrderBid(order3.id(), 10d, 300)

    then:
    1 * stockMatcher.addOrderAsk(order4.id(), 20d, 400)

    then:
    1 * stockMatcher.match(_, _, _, _)

    when:
    subject.tick()

    then:
    1 * stockMatcher.match(_, _, _, _)

    then:
    0 * stockMatcher._
  }

  def 'succesful remove order'() {
    given:
    def order1 = subject.addBuy(trader1, 100)
    def order2 = subject.addSell(trader1, 200)
    def order3 = subject.addBid(trader1, 300, 10d)
    def order4 = subject.addAsk(trader1, 400, 20d)

    when:
    def removed3 = subject.removeOrder(order3.id())

    then:
    1 * stockMatcher.removeOrder(order3.id())

    and:
    order3 == removed3

    expect:
    subject.getActiveOrders().collect().sort() == [order1, order2, order4].sort()

    when:
    def removed2 = subject.removeOrder(order2.id())

    then:
    1 * stockMatcher.removeOrder(order2.id())

    and:
    order2 == removed2

    expect:
    subject.getActiveOrders().collect().sort() == [order1, order4].sort()
  }

  def 'removed order is not passed to the underlying trade matcher on next tick'() {
    given:
    def order1 = subject.addBuy(trader1, 100)
    def order2 = subject.addSell(trader1, 200)
    def order3 = subject.addBid(trader1, 300, 10d)
    def order4 = subject.addAsk(trader1, 400, 20d)
    subject.removeOrder(order3.id())

    when:
    subject.tick()

    then:
    1 * stockMatcher.addOrderBuy(order1.id(), _)
    1 * stockMatcher.addOrderSell(order2.id(), _)
    1 * stockMatcher.addOrderAsk(order4.id(), _, _)
    1 * stockMatcher.match(_, _, _, _)
    
    then:
    0 * stockMatcher._

    expect:
    subject.getActiveOrders().collect().sort() == [order1, order2, order4].sort()
  }

  def 'order match event is passed to the listener'() {
    given:
    def order1 = subject.addBuy(trader1, 100)
    def order2 = subject.addSell(trader1, 200)
    def order3 = subject.addBuy(trader1, 100)
    def order4 = subject.addSell(trader1, 200)
    stockMatcher.match(_, _, _, _) >> {
      with (it[1] as OrderMatchedEventListener) {
        onOrderMatched(order1.id(), order2.id(), 100)
        onOrderMatched(order3.id(), order4.id(), 200)
      }
    }

    when:
    subject.tick()

    then:
    1 * orderMatchDownstream.accept({
      with (it as OrderMatchRecord) {
        marketPrice() == 666.00d
        buyerOrder() == order1
        sellerOrder() == order2
        quantity() == 100
      }
    })
    1 * orderMatchDownstream.accept({
      with (it as OrderMatchRecord) {
        marketPrice() == 666.00d
        buyerOrder() == order3
        sellerOrder() == order4
        quantity() == 200
      }
    })

    then:
    0 * orderMatchDownstream._
    0 * orderMatchRejectedDownstream._
    0 * filledOrderDownstream._
    0 * filledOrderDownstreamRejected._
  }

  def 'order match event event is passed to reject event listener'() {
    given:
    def order1 = subject.addBuy(trader1, 100)
    def order2 = subject.addSell(trader1, 200)
    def order3 = subject.addBuy(trader1, 100)
    def order4 = subject.addSell(trader1, 200)

    def e1 = new RuntimeException('e1')
    def e2 = new RuntimeException('e2')
    def e3 = new RuntimeException('e3')

    stockMatcher.match(_, _, _, _) >> {
      with (it[1] as OrderMatchedEventListener) {
        onOrderMatched(order1.id(), order2.id(), 100)
        onOrderMatched(order3.id(), order4.id(), 200)
      }
    }

    when:
    subject.tick()

    then:
    1 * orderMatchDownstream.accept({ it.buyerOrder() == order1 }) >> { throw e1 }
    1 * orderMatchRejectedDownstream.accept({ it.buyerOrder() == order1 }, e1)

    then:
    1 * orderMatchDownstream.accept({ it.buyerOrder() == order3 }) >> { throw e2 }
    1 * orderMatchRejectedDownstream.accept({ it.buyerOrder() == order3 }, e2)

    then:
    0 * orderMatchDownstream._
    0 * orderMatchRejectedDownstream._
    0 * filledOrderDownstream._
    0 * filledOrderDownstreamRejected._
  }

  def 'order fully filled event is passed to the listener'() {
    given:
    def order1 = subject.addBuy(trader1, 100)
    def order2 = subject.addSell(trader1, 200)
    def order3 = subject.addBuy(trader1, 100)
    def order4 = subject.addSell(trader1, 200)

    stockMatcher.match(_, _, _, _) >> {
      with (it[3] as OrderFulfilledEventListener) {
        onOrderFulfilled(order1.id())
        onOrderFulfilled(order2.id())
        onOrderFulfilled(order3.id())
        onOrderFulfilled(order4.id())
      }
    }

    when:
    subject.tick()

    then:
    1 * filledOrderDownstream.accept(order1)
    1 * filledOrderDownstream.accept(order2)
    1 * filledOrderDownstream.accept(order3)
    1 * filledOrderDownstream.accept(order4)

    then:
    0 * orderMatchDownstream._
    0 * orderMatchRejectedDownstream._
    0 * filledOrderDownstream._
    0 * filledOrderDownstreamRejected._
  }


  def 'order fully filled event is passed to reject event listener'() {
    given:
    def order1 = subject.addBuy(trader1, 100)
    def order2 = subject.addSell(trader1, 200)
    def order3 = subject.addBuy(trader1, 300)

    def e1 = new RuntimeException('e1')
    def e2 = new RuntimeException('e2')
    def e3 = new RuntimeException('e3')

    stockMatcher.match(_, _, _, _) >> {
      with (it[3] as OrderFulfilledEventListener) {
        onOrderFulfilled(order1.id())
        onOrderFulfilled(order2.id())
        onOrderFulfilled(order3.id())
      }
    }

    when:
    subject.tick()

    then:
    1 * filledOrderDownstream.accept(order1) >> { throw e1 }
    1 * filledOrderDownstreamRejected.accept(order1, e1)

    then:
    1 * filledOrderDownstream.accept(order2) >> { throw e2 }
    1 * filledOrderDownstreamRejected.accept(order2, e2)

    then:
    1 * filledOrderDownstream.accept(order3) >> { throw e3 }
    1 * filledOrderDownstreamRejected.accept(order3, e3)

    then:
    0 * orderMatchDownstream._
    0 * orderMatchRejectedDownstream._
    0 * filledOrderDownstream._
    0 * filledOrderDownstreamRejected._
  }

  @Timeout(5)
  def 'new order submission is not blocked by the running tick'() {
    given:
    def executor = Executors.newSingleThreadExecutor()
    def tickStartedEvent = new SynchronousQueue()
    def tickPoisonPillEvent = new SynchronousQueue()

    def stockMatcher = Stub(StockMatcher) {
      match(_, _, _, _) >> {
        tickStartedEvent.put(true) // notify the tick has started
        tickPoisonPillEvent.take() // wait for complete event
      }
    }

    def OrderBookImpl subject = new OrderBookImpl(
        stockMatcher,
        Stub(SecurityRecord),
        Stub(Downstream),
        Stub(RejectedDownstream),
        Stub(Downstream),
        Stub(RejectedDownstream))

    def order1 = subject.addBuy(trader1, 100)
    def order2 = subject.addSell(trader2, 200)
    executor.submit({ subject.tick() })

    when:
    tickStartedEvent.take() // wait the tick is started
    def order3 = subject.addBid(trader3, 300, 10.0)
    tickPoisonPillEvent.put(true) // notify the tick to complete

    then:
    order3

    expect:
    subject.getActiveOrders().collect().sort() == [order1, order2, order3].sort()

    cleanup:
    executor.shutdownNow()
    executor.awaitTermination(1000 * 5, TimeUnit.MILLISECONDS)
  }
}