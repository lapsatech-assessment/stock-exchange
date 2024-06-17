package stock.exchange.market

import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Subject
import stock.exchange.instrument.DuplicateInstrumentException
import stock.exchange.instrument.MarketDataWorld
import stock.exchange.instrument.NoSuchInstrumentException
import stock.exchange.instrument.NoSuchSecurityException

class MarketDataWorldTest extends Specification {

  @Subject
  def subject = new MarketDataWorld()

  def 'create secuiry : successful'() {
    when:
    def secur1 = subject.createSecurity(1, 'secur1', 10.0d)

    then:
    secur1.id == 1
    secur1.symbol == 'secur1'
    secur1.marketPrice.asDouble == 10.0d

    expect:
    subject.getInstruments().collect() == [secur1]
  }

  def 'create secuiry : failed duplicate'() {
    given:
    def secur1 = subject.createSecurity(1, 'secur1', 10.0d)

    when:
    subject.createSecurity(1, 'secur2', 20.0d)

    then:
    thrown(DuplicateInstrumentException)

    when:
    subject.createSecurity(2, 'secur1', 30.0d)

    then:
    thrown(DuplicateInstrumentException)

    expect:
    subject.getInstruments().collect() == [secur1]
  }

  def 'create composite : succesful'() {
    given:
    def secur1 = subject.createSecurity(1, 'secur1', 10.0d)
    def secur2 = subject.createSecurity(2, 'secur2', 20.0d)

    when:
    def comp1 = subject.createComposite(3, 'comp1', 'secur1', 'secur2')

    then:
    comp1.id == 3
    comp1.symbol == 'comp1'
    comp1.marketPrice.asDouble == 15.0d
    comp1.componenents.collect() == [secur1, secur2]

    expect:
    subject.getInstruments().collect().sort({it.id}) == [secur1, secur2, comp1]
  }

  def 'creat composite : failed duplicate'() {
    given:
    def secur1 = subject.createSecurity(1, 'secur1', 10.0d)
    def secur2 = subject.createSecurity(2, 'secur2', 20.0d)
    
    when:
    subject.createComposite(1, 'comp1', 'secur1', 'secur2')

    then:
    thrown(DuplicateInstrumentException)
    
    when:
    subject.createComposite(2, 'secur2', 'secur1', 'secur2')

    then:
    thrown(DuplicateInstrumentException)

    expect:
    subject.getInstruments().collect().sort({it.id}) == [secur1, secur2]
  }

  def 'creat composite : component not found'() {
    given:
    def secur1 = subject.createSecurity(1, 'secur1', 10.0d)
    def secur2 = subject.createSecurity(2, 'secur2', 20.0d)
    
    when:
    subject.createComposite(3, 'comp1', 'secur1', 'securN')

    then:
    thrown(NoSuchSecurityException)

    expect:
    subject.getInstruments().collect().sort({it.id}) == [secur1, secur2]
  }

  def 'get market price : returns correct values'() {
    given:
    def secur1 = subject.createSecurity(1, 'secur1', 10.0d)
    def secur2 = subject.createSecurity(2, 'secur2', 20.0d)
    def comp1 = subject.createComposite(3, 'comp1', 'secur1', 'secur2')
    
    expect:
    subject.getMarketPrice(1) == 10d
    subject.getMarketPrice(2) == 20d
    subject.getMarketPrice(3) == 15d
    subject.getMarketPriceRef(1).asDouble == 10d
    subject.getMarketPriceRef(2).asDouble == 20d
    subject.getMarketPriceRef(3).asDouble == 15d
  }

  def 'get market price : failures on wrong id'() {
    given:
    def secur1 = subject.createSecurity(1, 'secur1', 10.0d)
    def secur2 = subject.createSecurity(2, 'secur2', 20.0d)
    def comp1 = subject.createComposite(3, 'comp1', 'secur1', 'secur2')
    
    when:
    subject.getMarketPrice(4)

    then:
    thrown(NoSuchInstrumentException)

    when:
    subject.getMarketPriceRef(4)

    then:
    thrown(NoSuchInstrumentException)
  }

  def 'accept last trade price : updates prices accordingly'() {
    given:
    def secur1 = subject.createSecurity(1, 'secur1', 10.0d)
    def secur2 = subject.createSecurity(2, 'secur2', 20.0d)
    def comp1 = subject.createComposite(3, 'comp1', 'secur1', 'secur2')

    when:
    subject.acceptLastTradePrice(1, 30, 1000)

    then:
    secur1.marketPrice.asDouble == 30d
    secur2.marketPrice.asDouble == 20d
    comp1.marketPrice.asDouble == 25d

    and:
    subject.getMarketPrice(1) == 30d
    subject.getMarketPrice(2) == 20d
    subject.getMarketPrice(3) == 25d
    subject.getMarketPriceRef(1).asDouble == 30d
    subject.getMarketPriceRef(2).asDouble == 20d
    subject.getMarketPriceRef(3).asDouble == 25d
  }

  def 'accept last trade price : fails on wrong instrument id'() {
    given:
    def secur1 = subject.createSecurity(1, 'secur1', 10.0d)
    def secur2 = subject.createSecurity(2, 'secur2', 20.0d)
    def comp1 = subject.createComposite(3, 'comp1', 'secur1', 'secur2')

    when:
    subject.acceptLastTradePrice(4, 30, 1000)

    then:
    thrown(NoSuchInstrumentException)
  }

  def 'accept last trade price : fails unable to update composite instrument directly'() {
    given:
    def secur1 = subject.createSecurity(1, 'secur1', 10.0d)
    def secur2 = subject.createSecurity(2, 'secur2', 20.0d)
    def comp1 = subject.createComposite(3, 'comp1', 'secur1', 'secur2')

    when:
    subject.acceptLastTradePrice(3, 30, 1000)

    then:
    thrown(NoSuchSecurityException)
  }
}