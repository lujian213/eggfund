package io.github.lujian213.eggfund.model

import spock.lang.Shared
import spock.lang.Specification


class InvestorSpec extends Specification {

    @Shared
    def investor1, investor2, investor3

    def setup() {
        investor1 = new Investor("Alex", "Alex Smith", "icon1")
        investor2 = new Investor()
        with(investor2) {
            setId("Bob")
            setName("Bob Smith")
            setIcon("icon2")
        }

        investor3 = new Investor()
        with(investor3) {
            setId("Alex")
            setName("Alex Peter")
            setIcon("icon3")
        }
    }
    def "constructor"() {
        when:
        def investor = new Investor("Alex ", " Alex Smith", "icon1")
        then:
        with(investor) {
            name == "Alex Smith"
            id == "Alex"
        }

        when:
        new Investor("", "name", null)
        then:
        thrown(IllegalArgumentException)

        when:
        new Investor("id", "", null)
        then:
        thrown(IllegalArgumentException)
    }
    def "Update"() {
        when:
        investor1.update(investor2)
        then:
        with(investor1 as Investor) {
            getId() == "Alex"
            getName() == "Bob Smith"
            getIcon() == "icon2"
        }
    }

    def "Equals"() {
        expect:
        result == (inst1 == inst2)
        where:
        inst1     | inst2     | result
        investor1 | investor2 | false
        investor1 | investor3 | true
        investor2 | investor3 | false
    }

    def "HashCode"() {
        expect:
        result == (inst1.hashCode() == inst2.hashCode())
        where:
        inst1     | inst2     | result
        investor1 | investor2 | false
        investor1 | investor3 | true
        investor2 | investor3 | false
    }
}