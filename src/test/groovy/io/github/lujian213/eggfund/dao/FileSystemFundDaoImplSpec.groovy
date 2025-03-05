package io.github.lujian213.eggfund.dao

import spock.lang.Specification
import io.github.lujian213.eggfund.model.FundInfo
import io.github.lujian213.eggfund.model.FundValue

class FileSystemFundDaoImplSpec extends Specification {

    FileSystemFundDaoImpl dao

    def setup() {
        dao = new FileSystemFundDaoImpl(new File("dummyFolder"))
    }

    def cleanup() {
        new File("dummyFolder").deleteDir()
    }

    def "GetFundValueFile"() {
        when:
        def result = dao.getFundValueFile(code, month)
        then:
        result == new File("dummyFolder", fileName)
        where:
        code   | month    | fileName
        "1000" | "202501" | "1000-202501.json"
        "1000" | "202502" | "1000-202502.json"
    }

    def "IsQualifiedFundValueFile"() {
        expect:
        dao.isQualifiedFundValueFile(fileName, code) == result
        where:
        fileName           | code   | result
        "1000-202501.json" | "1000" | true
        "1001-202501.json" | "1000" | false
        "1000202501.json"  | "1000" | false
    }

    def "saveFunds & loadFundInfo"() {
        given:
        def fund1 = new FundInfo("1000", "namel")
        def fund2 = new FundInfo("1001", "name2")
        with(fund2) {
            setAlias("alias2")
            setCategory("category2")
            setEtf(true)
            setUrl("some url")
            setPriority(8)
        }
        def funds = [fund1, fund2]
        when:
        dao.saveFunds(funds)
        then:
        def fundInfoList = dao.loadFundInfo()
        fundInfoList.size() == 2
        with(fundInfoList[0]) {
            id == "1000"
            name == "namel"
            alias == null
            category == null
            !etf
            url == null
            priority == 10
        }
        with(fundInfoList[1]) {
            id == "1001"
            name == "name2"
            alias == "alias2"
            category == "category2"
            etf
            url == "some url"
            priority == 8
        }
    }

    def "loadFundInfo - loadEmptyList"() {
        when:
        def fundInfoList = dao.loadFundInfo()
        then:
        fundInfoList.size() == 0
    }

    def "saveFundValues & loadFundValues"() {
        given:
        def fundValuel = new FundValue("2025-01-01", 1.01, 1.01, 0.01)
        def fundValue2 = new FundValue()
        with(fundValue2) {
            setDay("2025-01-02")
            setUnitValue(1.02)
            setAccumulatedValue(1.02)
            setIncreaseRate(0.02)
        }
        def fundValues = [fundValuel, fundValue2]
        when:
        dao.saveFundValues("1000", "202501", fundValues)
        then:
        def fundValueList = dao.loadFundValues("1000").get("202501")
        fundValueList.size() == 2
        with(fundValueList[0]) {
            day == "2025-01-01"
            unitValue == 1.01d
            accumulatedValue == 1.01d
            increaseRate == 0.01d
        }
        with(fundValueList[1]) {
            day == "2025-01-02"
            unitValue == 1.02d
            accumulatedValue == 1.02d
            increaseRate == 0.02d
        }
    }

    def "saveFundValues & deleteFundValues"() {
        given:
        def fundValue1 = new FundValue("2025-01-01", 1.01, 1.01, 0.01)
        def fundValues = [fundValue1]
        when:
        dao.saveFundValues("1000", "202501", fundValues)
        then:
        def fundValueList = dao.loadFundValues("1000").get("202501")
        fundValueList.size() == 1
        dao.deleteFundValues("1000")
        !new File("dummyFolder", "1000-202501 json").exists()
    }

    def "loadFundValues - loadEmptyList"() {
        when:
        def fundValueList = dao.loadFundValues("1000")
        then:
        fundValueList.size() == 0
    }
}