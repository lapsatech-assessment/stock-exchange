package util.nogc

import spock.lang.Specification

class MoreArraysTest extends Specification {

  def 'test distinct values natural ordering'(def String[] input, def String[] expectedArray) {
    when:
    def newlen = MoreArrays.distinct(input)
    def distinctArray = Arrays.copyOf(input, newlen)

    then:
    distinctArray == expectedArray

    where:
    input                                                  | expectedArray
    ['a1', 'c1', 'a1', 'a2', 'c2', 'c1', 'a1', 'q3', 'Q4'] | ['Q4', 'a1', 'a2', 'c1', 'c2', 'q3']
    []                                                     | []
    ['a1']                                                 | ['a1']
    ['a1', 'a1', 'a1', 'a1', 'a1']                         | ['a1']
    ['a1', 'a1', 'A1', 'a1', 'a1']                         | ['A1', 'a1']
  }

  def 'test distinct values sting case insensitive ordering'(def String[] input, def String[] expectedArray) {
    when:
    def newlen = MoreArrays.distinct(input, { s1,s2 -> s1.compareToIgnoreCase(s2) })
    def distinctArray = Arrays.copyOf(input, newlen)

    then:
    distinctArray == expectedArray

    where:
    input                                                  | expectedArray
    ['a1', 'c1', 'A1', 'a2', 'c2', 'C1', 'a1', 'q3', 'Q4'] | ['a1', 'a2', 'c1', 'c2', 'q3', 'Q4']
    []                                                     | []
    ['a1']                                                 | ['a1']
    ['a1', 'A1', 'a1', 'A1', 'a1']                         | ['a1']
  }
}