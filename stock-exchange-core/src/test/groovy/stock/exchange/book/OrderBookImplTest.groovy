package stock.exchange.book

import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.TimeUnit

import spock.lang.Specification
import spock.lang.Timeout
import stock.exchange.domain.SecurityRecord
import stock.exchange.domain.TraderRecord
import stock.exchange.integration.Downstream
import stock.exchange.integration.RejectedDownstream
import stock.exchange.matcher.StockMatcher

class OrderBookImplTest extends Specification {

  def ExecutorService executor

  def setup() {
    executor = Executors.newSingleThreadExecutor()
  }

  def cleanup() {
    executor.shutdownNow()
    executor.awaitTermination(1000 * 5, TimeUnit.MILLISECONDS)
  }
  def trader1 = Stub(TraderRecord)
  def trader2 = Stub(TraderRecord)

  def secur1 = Stub(SecurityRecord)

  @Timeout(5)
  def 'new order submission is not blocked by the running tick'() {
    given:
    def BlockingQueue tickStartedEvent = new SynchronousQueue()
    def BlockingQueue tickPoisonPillEvent = new SynchronousQueue()

    def stockMatcher = Stub(StockMatcher) {
      match(_, _, _, _) >> {
        tickStartedEvent.put(true) // notify the tick has started
        tickPoisonPillEvent.take() // wait for complete event
      }
    }

    def OrderBookImpl subject = new OrderBookImpl(
        stockMatcher,
        secur1,
        Stub(Downstream),
        Stub(RejectedDownstream),
        Stub(Downstream),
        Stub(RejectedDownstream))

    def order1 = subject.addBuy(trader1, 100)
    def order2 = subject.addSell(trader2, 200)
    executor.submit({ subject.tick() })

    when:
    tickStartedEvent.take() // wait the tick is started
    def order3 = subject.addBid(trader1, 300, 10.0)
    tickPoisonPillEvent.put(true) // notify the tick to complete

    then:
    order3

    expect:
    subject.getActiveOrders().collect({it.quantity}).sort() == [100, 200, 300]
  }
}