package util.nogc

import java.util.function.Supplier

import spock.lang.Specification

class SimpleReusableObjectsTest extends Specification {

  def 'test underlying array size is increased on a given value'() {
    when:
    def subject = new SimpleReusableObjects(2, 100, { new Object() })

    then:
    subject.array.size64() == 2

    when:
    subject.release('1')

    then:
    subject.array.size64() == 2

    when:
    subject.release('2')

    then:
    subject.array.size64() == 2

    when:
    subject.release('3')

    then:
    subject.array.size64() == 102
  }

  def 'test basic function where object is created reused after the release'() {
    given:
    def o1 = 'a'
    def o2 = 'b'
    def o3 = 'c'
    def osorted = [o1, o2, o3].sort()

    def creator = Mock(Supplier)
    def subject = new SimpleReusableObjects(10, 10, creator)

    when:
    def t1 = subject.capture()
    def t2 = subject.capture()
    def t3 = subject.capture()
    def tsorted = [t1, t2, t3].sort()

    then:
    3 * creator.get() >>> [o1, o2, o3]
    tsorted[0].is(osorted[0])
    tsorted[1].is(osorted[1])
    tsorted[2].is(osorted[2])

    and:
    subject.createdTotal() == 3
    subject.cachedTotal() == 0

    when:
    subject.capture()

    then:
    1 * creator.get() >> { throw new RuntimeException('not so far') }

    then:
    def e = thrown(RuntimeException)

    and:
    subject.createdTotal() == 3
    subject.cachedTotal() == 0

    when:
    subject.release(t1)
    subject.release(t2)
    subject.release(t3)

    then:
    subject.createdTotal() == 3
    subject.cachedTotal() == 3

    when:
    t1 = subject.capture()
    t2 = subject.capture()
    t3 = subject.capture()
    tsorted = [t1, t2, t3].sort()

    then:
    0 * creator.get()
    tsorted[0].is(osorted[0])
    tsorted[1].is(osorted[1])
    tsorted[2].is(osorted[2])

    and:
    subject.createdTotal() == 3
    subject.cachedTotal() == 0
  }
}