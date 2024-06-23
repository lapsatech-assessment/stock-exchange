package stock.exchange.trade

import static java.lang.Double.NaN

import java.time.Instant

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject
import stock.exchange.domain.OrderRecord
import stock.exchange.domain.SecurityRecord
import stock.exchange.domain.TradeRecord
import stock.exchange.integration.Downstream
import stock.exchange.integration.RejectedDownstream

class TradeGeneratorImplTest extends Specification {

  def tradeDownstream = Mock(Downstream)
  def tradeDownstreamRejected = Mock(RejectedDownstream)

  @Shared
  def firstTs = Instant.now()

  @Shared
  def secondTs = Instant.now().plusMillis(1)
  
  def 'successful trade generation when #scenario' (
    def scenario,
    def marketPrice,
    def buyPrice, 
    def buyQuantity, 
    def buyTs, 
    def sellPrice, 
    def sellQuantity, 
    def sellTs, 
    def tradeQuantity, 
    def expectTradePrice) {
    given:
    def secur1 = Stub(SecurityRecord)
    def buyingOrder = Stub(OrderRecord) {
      price() >> buyPrice
      quantity() >> buyQuantity
      timestamp() >> buyTs
    }
    def sellingOrder = Stub(OrderRecord) {
      price() >> sellPrice
      quantity() >> sellQuantity
      timestamp() >> sellTs
    }
    def subject = new TradeGeneratorImpl(tradeDownstream, tradeDownstreamRejected)

    when:
    subject.generateTrade(
        marketPrice,
        secur1,
        buyingOrder,
        sellingOrder,
        tradeQuantity)

    then:
    1 * tradeDownstream.accept({
      def tr = it as TradeRecord
      tr.id() > 0
      tr.security() == secur1
      tr.buyingOrder() == buyingOrder
      tr.sellingOrder() == sellingOrder
      tr.price() == expectTradePrice
      tr.quantity() == tradeQuantity
    })
    0 * tradeDownstreamRejected._

    where:
    scenario                                                                                    | marketPrice | buyPrice | buyQuantity | buyTs    | sellPrice | sellQuantity | sellTs   | tradeQuantity | expectTradePrice
    'prices are set and equal and buyer was first'                                              |     150.00d |  100.00d |          10 | firstTs  |   100.00d |           10 | secondTs |            10 |          100.00d
    'prices are set are equal and seller was first'                                             |     150.00d |  100.00d |          10 | secondTs |   100.00d |           10 | firstTs  |            10 |          100.00d

    'prices are set buy > sell and buyer first -> seller\'s price priority'                     |     150.00d |  200.00d |          10 | firstTs  |   100.00d |           10 | secondTs |            10 |          100.00d
    'prices are set buy > sell and seller first -> buyer\'s price priority'                     |     150.00d |  200.00d |          10 | secondTs |   100.00d |           10 | firstTs  |            10 |          200.00d

    'buy price not set and sell price not set -> market price'                                  |     150.00d |      NaN |          10 | firstTs  |       NaN |           10 | secondTs |            10 |          150.00d

    'buy price not set and sell price set and buyer first -> seller\'s price priority'          |     150.00d |      NaN |          10 | firstTs  |   100.00d |           10 | secondTs |            10 |          100.00d
    'buy price not set and sell price set and seller first -> buyer\'s (market) price priority' |     150.00d |      NaN |          10 | secondTs |   100.00d |           10 | firstTs  |            10 |          150.00d

    'buy price set and sell not price set and buyer first -> seller\'s price priority'          |     150.00d |  200.00d |          10 | firstTs  |       NaN |           10 | secondTs |            10 |          150.00d
    'buy price set and sell not price set and seller first -> buyer\'s (market) price priority' |     150.00d |  200.00d |          10 | secondTs |       NaN |           10 | firstTs  |            10 |          200.00d
  }

  def '#scenario causes to exception' (
    def scenario, 
    def marketPrice,
    def buyPrice,
    def buyQuantity,
    def sellPrice,
    def sellQuantity,
    def quantity, 
    def expectException) {

    given:
    def secur1 = Stub(SecurityRecord)
    def buyingOrder = Stub(OrderRecord) {
      price() >> buyPrice
      quantity() >> buyQuantity
    }
    def sellingOrder = Stub(OrderRecord) {
      price() >> sellPrice
      quantity() >> sellQuantity
    }
    def subject = new TradeGeneratorImpl(tradeDownstream, tradeDownstreamRejected)
    
    when:
    subject.generateTrade(
      marketPrice,
      secur1,
      buyingOrder,
      sellingOrder,
      quantity)

    then:
    thrown(expectException)
    0 * tradeDownstream._
    0 * tradeDownstreamRejected._

    where:
    scenario                                      | marketPrice | buyPrice | buyQuantity | sellPrice | sellQuantity | quantity | expectException
    'buying price below zero'                     |     150.00d | -100.00d |          75 |   100.00d |           75 |       75 | TradeInvalidPriceException
    'sell price below zero'                       |     150.00d |  100.00d |          75 |  -100.00d |           75 |       75 | TradeInvalidPriceException
    'buying price lower than selling price'       |     150.00d |   90.00d |          75 |   100.00d |           75 |       75 | TradePriceMistmachValidationException
    'quntity greater than buying order quantity'  |     150.00d |  100.00d |          50 |   100.00d |           75 |       75 | TradeAndOrderQuantityMismatchException
    'quntity greater than selling order quantity' |     150.00d |  100.00d |          75 |   100.00d |           50 |       75 | TradeAndOrderQuantityMismatchException
   }
}
