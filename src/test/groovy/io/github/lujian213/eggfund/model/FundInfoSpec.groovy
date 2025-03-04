package io.github.lujian213.eggfund.model

import spock.lang.Shared
import spock.lang.Specification


class FundInfoSpec extends Specification {

    @Shared
    def fundInfo1, fundInfo2, fundInfo3

    def setup() {
        fundInfo1 = new FundInfo("12345", "fund1")
        fundInfo2 = new FundInfo()
        with(fundInfo2) {
            setId("54321")
            setName("fund2")
            setAlias("alias1")
            setCategory("category1")
            setEtf(true)
            setUrl("ur11")
            setPriority(1)
        }
        fundInfo3 = new FundInfo()
        with(fundInfo3) {
            setId("12345")
            setName("fund3")
            setAlias("alias3")
            setCategory("category3")
            setEtf(true)
            setUrl("ur13")
            setPriority(3)
        }
    }

    def "Update"() {
        when:
        fundInfo1.update(fundInfo2)
        then:
        with(fundInfo1 as FundInfo) {
            getId() == "12345"
            getName() == "fund1"
            getAlias() == "alias1"
            getCategory() == "category1"
            isEtf()
            getUrl() == "ur11"
            getPriority() == 1
        }
    }

    def "Equals"() {
        expect:
        result == fund1.equals(fund2)
        where:
        fund1     | fund2     | result
        fundInfo1 | fundInfo2 | false
        fundInfo1 | fundInfo3 | true
        fundInfo2 | fundInfo3 | false
    }

    def "HashCode"() {
        expect:
        result == (fund1.hashCode() == fund2.hashCode())
        where:
        fund1     | fund2     | result
        fundInfo1 | fundInfo2 | false
        fundInfo1 | fundInfo3 | true
        fundInfo2 | fundInfo3 | false
    }
}