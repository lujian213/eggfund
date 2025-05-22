package io.github.lujian213.eggfund.model

import io.github.lujian213.eggfund.utils.Constants
import spock.lang.Shared
import spock.lang.Specification


class FundInfoSpec extends Specification {

    @Shared
    def fundInfo1, fundInfo2, fundInfo3

    def setup() {
        fundInfo1 = new FundInfo("12345", "fund1", FundInfo.FundType.HK_STOCK)
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
            id == "12345"
            name == "fund1"
            alias == "alias1"
            category == "category1"
            etf
            url == "ur11"
            priority == 1
            type == FundInfo.FundType.LOCAL_FUND
            currencySign == "¥"
            currency == "RMB"
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

    def "json serialize and deserialize"() {
        given:
        def fundInfo = new FundInfo("12345", "fund1", FundInfo.FundType.HK_STOCK)
        fundInfo.setAlias("alias1")
        fundInfo.setCategory("category1")
        fundInfo.setEtf(true)
        fundInfo.setUrl("url1")
        fundInfo.setPriority(1)

        when:
        def json = Constants.MAPPER.writeValueAsString(fundInfo)
        println json
        def deserializedFundInfo = Constants.MAPPER.readValue(json, FundInfo)

        then:
        deserializedFundInfo.getId() == "12345"
        deserializedFundInfo.getName() == "fund1"
        deserializedFundInfo.getType() == FundInfo.FundType.HK_STOCK
        deserializedFundInfo.getAlias() == "alias1"
        deserializedFundInfo.getCategory() == "category1"
        deserializedFundInfo.isEtf()
        deserializedFundInfo.getUrl() == "url1"
        deserializedFundInfo.getPriority() == 1
    }
}