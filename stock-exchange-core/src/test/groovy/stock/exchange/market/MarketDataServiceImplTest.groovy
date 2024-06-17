package stock.exchange.market

import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Subject
import stock.exchange.instrument.MarketDataWorld
import stock.exchange.instrument.NoSuchInstrumentException

@Ignore
class MarketDataServiceImplTest extends Specification {

  @Subject
  def subject = new MarketDataWorld()

  def 'test get'() {
    expect:
    subject.getPrice(1) == 1d
    subject.getPrice(100) == 2d
    subject.getPrice(1000) == 3d
  }

  def 'test get unknown instrument throws InstrumentIsNotSupported'() {
    when:
    subject.getPrice(10000)

    then:
    thrown(NoSuchInstrumentException)
  }

  def 'test set'() {
    when:
    subject.setPrice(1, 123d)

    then:
    subject.getPrice(1) == 123d
    subject.getPrice(100) == 2d
    subject.getPrice(1000) == 3d

    when:
    subject.setPrice(100, 456d)

    then:
    subject.getPrice(1) == 123d
    subject.getPrice(100) == 456d
    subject.getPrice(1000) == 3d
  }

  def 'test set unknown instrument throws InstrumentIsNotSupported'() {
    when:
    subject.setPrice(10000, 789d)

    then:
    thrown(NoSuchInstrumentException)

    and: 'no price were updated'
    subject.getPrice(1) == 1d
    subject.getPrice(100) == 2d
    subject.getPrice(1000) == 3d
  }
}