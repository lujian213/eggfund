package io.github.lujian213.eggfund.dao

import io.github.lujian213.eggfund.model.Invest
import io.github.lujian213.eggfund.model.Investor
import spock.lang.Specification

class FileSystemInvestDaoImplSpec extends Specification {
    FileSystemInvestDaoImpl dao

    def setup() {
        dao = new FileSystemInvestDaoImpl(new File("dummyInvestFolder"))
    }

    def cleanup() {
        new File("dummyInvestFolder").deleteDir()
    }

    def "getInvestFileName"() {
        expect:
        dao.getInvestFile(investId) == result
        where:
        investId | result
        "Alex"   | new File("dummyFolder", "invest_Alex.json")
        "Bob"    | new File("dummyFolder", "invest_Bob.json")
    }

    def "saveInvestors & loadInvestors"() {
        given:
        def investor1 = new Investor("Alex", "Alex Smith", "icon1")
        def investor2 = new Investor()
        with(investor2) {
            setId("Bob")
            setName("Bob Smith")
            setIcon("icon2")
        }
        def investors = [investor1, investor2]
        when:
        dao.setFileUserDetailsManager(Mock(FileUserDetailsManager))
        dao.saveInvestors(investors)
        then:
        def investorList = dao.loadInvestors()
        investorList.size() == 2
        with(investorList[0]) {
            getId() == "Alex"
            getName() == "Alex Smith"
            getIcon() == "icon1"
        }
        with(investorList[1]) {
            getId() == "Bob"
            getName() == "Bob Smith"
            getIcon() == "icon2"
        }
    }

    def "loadInvestors - loadEmptyList"() {
        when:
        def investorList = dao.loadInvestors()
        then:
        investorList.size() == 0
    }

    def "saveInvests & loadInvests"() {
        given:
        def invest1 = new Invest(Invest.TYPE_TRADE, "001-001", "123456", "2025-01-01", 1000, 1.3, 10)
        def invest2 = new Invest()
        with(invest2) {
            setType(TYPE_DIVIDEND)
            setId("002-002")
            setCode("654321")
            setDay("2025-01-02")
            setShare(2000)
            setUnitPrice(1.4)
            setFee(20)
            setUserIndex(2)
            setEnabled(false)
            setBatch(3)
            setComments("some comments")
        }
        def invests = [invest1, invest2]
        when:
        dao.saveInvests("Alex", invests)

        then:
        def investList = dao.loadInvests("Alex")
        investList.size() == 2
        with(investList[0]) {
            type == TYPE_TRADE
            id == "001-001"
            code == "123456"
            day == "2025-01-01"
            share == 1000
            unitPrice == 1.3d
            fee == 10
            userIndex == 0
            enabled
            batch == 0
            comments == null
        }

        with(investList[1]) {
            type == TYPE_DIVIDEND
            id == "002-002"
            code == "654321"
            day == "2025-01-02"
            share == 2000
            unitPrice == 1.4d
            fee == 20
            userIndex == 2
            !enabled
            batch == 3
            comments == "some comments"
        }
    }

    def "saveInvests"() {
        given:
        def invest1 = new Invest(Invest.TYPE_TRADE, "001-001", "123456", "2025-01-01", 1000, 1.3, 10)
        def invests = [invest1]
        when:
        dao.saveInvests("Alex", invests)
        then:
        def investList = dao.loadInvests("Alex")
        investList.size() == 1
    }

    def "loadInvests - loadEmptyList"() {
        when:
        def investList = dao.loadInvests("Bob")
        then:
        investList.size() == 0
    }
}