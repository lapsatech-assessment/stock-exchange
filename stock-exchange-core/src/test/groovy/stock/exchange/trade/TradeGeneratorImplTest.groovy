package stock.exchange.trade

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

  def 'successful trade generation when #scenario' (def scenario, def buyOrderPrice, def buyOrderQuantity, def sellOrderPrice, def sellOrderQuantity, def buyPrice, def sellPrice, def tradeQuantity, def expectTradePrice) {
    given:
    def secur1 = Stub(SecurityRecord)
    def buyingOrder = Stub(OrderRecord) {
      price() >> buyOrderPrice
      quantity() >> buyOrderQuantity
    }
    def sellingOrder = Stub(OrderRecord) {
      price() >> sellOrderPrice
      quantity() >> sellOrderQuantity
    }
    def subject = new TradeGeneratorImpl(tradeDownstream, tradeDownstreamRejected)

    when:
    subject.generateTrade(
        secur1,
        buyingOrder,
        buyPrice,
        sellingOrder,
        sellPrice,
        tradeQuantity
        )

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
    scenario                              | buyOrderPrice | buyOrderQuantity | sellOrderPrice | sellOrderQuantity | buyPrice | sellPrice | tradeQuantity | expectTradePrice
    'prices are equal'                    |       100.00d |              100 |        100.00d |               100 |  100.00d |   100.00d |            75 |          100.00d
    'buy price is higher than sell price' |       110.00d |              100 |        100.00d |               100 |  110.00d |   100.00d |            75 |          105.00d
    'buy price is higher than sell price' |       110.00d |              100 |        100.00d |               100 |  110.00d |   100.00d |            75 |          105.00d
  }

  def '#scenario causes to exception' (
    def scenario, 
    def buyOrderPrice,
    def buyOrderQuantity,
    def sellOrderPrice,
    def sellOrderQuantity,
    def buyPrice, 
    def sellPrice, 
    def quantity, 
    def expectException) {

    given:
    def secur1 = Stub(SecurityRecord)
    def buyingOrder = Stub(OrderRecord) {
      price() >> buyOrderPrice
      quantity() >> buyOrderQuantity
    }
    def sellingOrder = Stub(OrderRecord) {
      price() >> sellOrderPrice
      quantity() >> sellOrderQuantity
    }
    def subject = new TradeGeneratorImpl(tradeDownstream, tradeDownstreamRejected)

    when:
    subject.generateTrade(
      secur1,
      buyingOrder,
      buyPrice,
      sellingOrder,
      sellPrice,
      quantity)

    then:
    thrown(expectException)
    0 * tradeDownstream._
    0 * tradeDownstreamRejected._

    where:
    scenario                                      | buyOrderPrice | buyOrderQuantity | sellOrderPrice | sellOrderQuantity | buyPrice |  sellPrice | quantity | expectException
    'buying price below zero'                     |       100.00d |               75 |        100.00d |                75 | -100.00d |  100.00d   |       75 | TradeInvalidPriceException
    'sell price below zero'                       |       100.00d |               75 |        100.00d |                75 |  100.00d | -100.00d   |       75 | TradeInvalidPriceException
    'buying price greater than order price'       |       100.00d |               75 |        100.00d |                75 |  120.00d |  100.00d   |       75 | TradeAndOrderPriceMismatchException
    'selling price lower than order price'        |       100.00d |               75 |        100.00d |                75 |  100.00d |   80.00d   |       75 | TradeAndOrderPriceMismatchException
    'buying price lower than selling price'       |       100.00d |               75 |        100.00d |                75 |   90.00d |  110.00d   |       75 | TradePriceMistmachValidationException
    'quntity greater than buying order quantity'  |       100.00d |               50 |        100.00d |                75 |  100.00d |  100.00d   |       75 | TradeAndOrderQuantityMismatchException
    'quntity greater than selling order quantity' |       100.00d |               75 |        100.00d |                50 |  100.00d |  100.00d   |       75 | TradeAndOrderQuantityMismatchException
   }
}
