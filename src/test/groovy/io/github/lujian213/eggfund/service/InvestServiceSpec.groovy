package io.github.lujian213.eggfund.service

import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Specification

import io.github.lujian213.eggfund.dao.*
import io.github.lujian213.eggfund.exception.*
import io.github.lujian213.eggfund.model.*
import io.github.lujian213.eggfund.utils.Constants
import java.time.LocalDate

class InvestServiceSpec extends Specification {

    InvestDao investDao
    InvestAuditDao investAuditDao
    FundDataService fundService
    InvestService investService
    PasswordEncoder passwordEncoder

    def setup() {
        investDao = Mock(InvestDao)
        investAuditDao = Mock(InvestAuditDao)
        fundService = Mock(FundDataService)
        passwordEncoder = Mock(PasswordEncoder)
    }

    def "deleteInvest"() {
        given:
        def investorId = "investor1"
        def investor = new Investor(id: investorId)
        investService = new InvestService()
        investService.setInvestDao(investDao)
        investService.setInvestAuditDao(investAuditDao)
        investService.setFundDataService(fundService)
        investService.investMap[investor] = ["invest1": new Invest(id: "invest1"),
                                             "invest2": new Invest(id: "invest2"),
                                             "invest3": new Invest(id: "invest3")]
        investService.investorMap[investorId] = investor

        when:
        1 * investDao.saveInvests(_, _) >> { throw new IOException() }
        investService.deleteInvests(investorId, ["invest1"])

        then:
        thrown(EggFundException)

        when:
        1 * investDao.saveInvests(investorId, _)
        1 * investAuditDao.saveInvestAudits(_)
        investService.deleteInvests(investorId, ["invest1"])

        then:
        !investService.investMap[investor].containsKey("invest1")
        investService.investMap[investor].size() == 2

        when:
        1 * investDao.saveInvests(investorId, _)
        1 * investAuditDao.saveInvestAudits(_)
        investService.deleteInvests(investorId, ["invalidInvestId"])

        then:
        investService.investMap[(investor)].size() == 2

        when:
        1 * investDao.saveInvests(investorId, _)
        1 * investAuditDao.saveInvestAudits(_)
        investService.deleteInvests(investorId, ["invest2", "invest3", "invest4"])

        then:
        investService.investMap[(investor)].size() == 0
    }

    def "init"() {
        given:
        def investor1 = new Investor("Alex", "Alex Chen", null)
        investDao.loadInvestors() >> [investor1, new Investor("Bob", "Bob Smith", null)]
        fundService.getAllFunds() >> [new FundInfo("1000", "fund0"), new FundInfo("1001", "fund1")]

        investService = Spy(InvestService) {
            updateInvestPrice(_) >> {
                Invest invest ->
                    if (invest.code == "1000") {
                        investService.investDataChanged = true
                    }
                    return invest
            }
        }

        investService.setInvestDao(investDao)
        investService.setInvestAuditDao(investAuditDao)
        investService.setFundDataService(fundService)

        1 * investDao.saveInvests(_, _) >> {}

        when:
        2 * investDao.loadInvests(_) >> { String investorId ->
            if (investorId == "Alex") {
                return [new Invest(id: "1000-1", code: "1000"),
                        new Invest(id: "1002-1", code: "1002")]
            } else {
                return [new Invest(id: "1001-1", code: "1001"),
                        new Invest(id: "1003-1", code: "1003")]
            }
        }

        1 * investAuditDao.loadInvestAudits(_) >> []

        investService.init()

        then:
        investService.investorMap.size() == 2
        investService.investMap[investor1]["1000-1"] != null

        when:
        1 * investDao.loadInvests(_) >> { throw new IOException() }
        1 * investAuditDao.loadInvestAudits(_) >> []

        investService.init()

        then:
        thrown(IOException)
    }

    def "updateInvestPrice"() {
        given:
        investService = new InvestService()
        fundService = Mock(FundDataService) {
            checkFund(_) >> { String code ->
                if (code == "1000" || code == "1001") {
                    return new FundInfo(code, "fund")
                } else {
                    return new FundInfo(code, "fundx").setEtf(true)
                }
            }

            getFundValue(_, _) >> { String code, _ ->
                if (code == "1000") {
                    return new FundValue(unitValue: 1.03)
                } else {
                    return null
                }
            }
        }

        investService.setFundDataService(fundService)

        expect:
        investService.updateInvestPrice(invest).getUnitPrice() == unitPrice

        where:
        invest                                    | unitPrice
        new Invest(code: "1000", unitPrice: 1.01) | 1.01
        new Invest(code: "1000", unitPrice: -1)   | 1.03
        new Invest(code: "1001", unitPrice: -1)   | -1
        new Invest(code: "1002", unitPrice: -1)   | -1
    }

    def "genInvestId"() {
        given:
        def investService = new InvestService()

        expect:
        investService.genInvestId(investorId, code).startsWith(prefix)

        where:
        investorId | code   | prefix
        "Alex"     | "1000" | "Alex-1000-"
        "Bob"      | "1001" | "Bob-1001-"
    }

    def "checkInvestor"() {
        given:
        def investService = new InvestService()
        investService.investorMap = ["Alex": new Investor("Alex", "Alex Cheng", null)]

        expect:
        investService.checkInvestor("Alex").getId() == "Alex"

        when:
        investService.checkInvestor("Bob")
        then:
        thrown(EggFundException)
    }

    def "disableInvest"() {
        given:
        def investor1 = new Investor(id: "investor1")
        def invest = new Invest(id: "investId")
        def investService = new InvestService()
        investService.setInvestDao(investDao)

        investService.investMap[investor1] = ["investId": invest]
        investService.investorMap["investor1"] = new Investor(id: "investor1")
        investService.investorMap["investor2"] = new Investor(id: "investor2")

        when:
        investService.disableInvest("investor2", "investId", false)
        then:
        thrown(EggFundException)

        when:
        1 * investDao.saveInvests(_, _) >> {}
        def result = investService.disableInvest("investor1", "investId", false)
        then:
        !result.isEnabled()
        result == invest

        when:
        1 * investDao.saveInvests(_, _) >> { throw new IOException() }
        investService.disableInvest("investor1", "investId", false)
        then:
        thrown(EggFundException)
    }

    def "resetInvestUserIndex"() {
        given:
        def investMap = [:] as Map
        investMap << ["invest1": new Invest(id: "invest1", day: "2025-01-05", code: "1000", userIndex: 1)]
        investMap << ["invest2": new Invest(id: "invest2", day: "2025-01-05", code: "1000", userIndex: 4)]
        def invest3 = new Invest(id: "invest3", day: "2025-01-05", code: "1000")
        def invest4 = new Invest(id: "invest4", day: "2025-01-05", code: "1000")
        def invest5 = new Invest(id: "invest5", day: "2025-01-06", code: "1000")
        def invest6 = new Invest(id: "invest6", day: "2025-01-06", code: "1000")
        def invest7 = new Invest(id: "invest7", day: "2025-01-05", code: "1001")

        def invests = [invest3, invest4, invest5, invest6, invest7]
        investService = new InvestService()

        when:
        investService.resetInvestUserIndex(investMap, invests)

        then:
        invest3.userIndex == 5
        invest4.userIndex == 6
        invest5.userIndex == 0
        invest6.userIndex == 1
        invest7.userIndex == 0
    }

    def "getAllInvestors"() {
        given:
        investService = new InvestService()
        investService.investorMap = [
                "Alex" : new Investor("Alex", "Alex Cheng", null),
                "Cathy": new Investor("Cathy", "Cathy Zhang", null),
                "David": new Investor("David", "David Pan", null),
                "Bob"  : new Investor("Bob", "Bob Smith", null)
        ]

        when:
        def investors = investService.getAllInvestors()

        then:
        investors.size() == 4
        investors[0].id == "Alex"
        investors[1].id == "Bob"
        investors[2].id == "Cathy"
        investors[3].id == "David"
    }

    def "getInvestors"() {
        given:
        def investor1 = new Investor("Alex", "Alex Cheng", null)
        def investor2 = new Investor("Bob", "Bob Smith", null)
        def investor3 = new Investor("Cathy", "Cathy Zhang", null)

        investService = new InvestService()
        investService.investMap[investor1] = ["invest1": new Invest(id: "invest1", code: "1000"), "invest2": new Invest(id: "invest2", code: "1001")]
        investService.investMap[investor2] = ["invest3": new Invest(id: "invest3", code: "1000"), "invest4": new Invest(id: "invest4", code: "1002")]
        investService.investMap[investor3] = ["invest3": new Invest(id: "invest3", code: "1000")]

        expect:
        def ret = investService.getInvestors(code)
        ret.size() == size
        if (size > 2) {
            ret[0] == investor1
            ret[1] == investor2
            ret[2] == investor3
        }

        where:
        code   | size
        "1000" | 3
        "1001" | 1
        "1002" | 1
        "1003" | 0
    }

    def "overwriteInvests"() {
        given:
        investService = new InvestService()
        def fund = new FundInfo("1000", "fund")
        def investMap = [:] as Map
        investMap << ["invest1": new Invest(id: "invest1", code: "1000", day: "2025-01-05")]
        investMap << ["invest2": new Invest(id: "invest2", code: "1000", day: "2025-01-06")]
        investMap << ["invest3": new Invest(id: "invest3", code: "1001", day: "2025-01-06")]
        investMap << ["invest4": new Invest(id: "invest3", code: "1000", day: "2025-01-07")]
        def newInvestMap = [:] as Map
        newInvestMap << ["invest5": new Invest(id: "invest5", code: "1000", day: "2025-01-06")]
        newInvestMap << ["invest6": new Invest(id: "invest6", code: "1000", day: "2025-01-07")]

        when:
        def ret = investService.overwriteInvests(fund, investMap, newInvestMap)

        then:
        ret.size() == 4
        ret["invest1"] != null
        ret["invest3"] != null
        ret["invest5"] != null
        ret["invest6"] != null
    }

    def "getUserInvestedFunds"() {
        given:
        investService = new InvestService()

        def investor1 = new Investor("Alex", "Alex Cheng", null)
        def investor2 = new Investor("Bob", "Bob Smith", null)

        investService.investorMap = [
                "Alex": investor1,
                "Bob" : investor2
        ]

        investService.investMap[investor1] = [
                "invest1": new Invest(id: "invest1", code: "1000"),
                "invest2": new Invest(id: "invest2", code: "1001"),
                "invest3": new Invest(id: "invest3", code: "1002"),
                "invest4": new Invest(id: "invest3", code: "1002")
        ]
        investService.investMap[investor2] = [
                "invest5": new Invest(id: "invest5", code: "1000"),
                "invest6": new Invest(id: "invest6", code: "1001"),
                "invest7": new Invest(id: "invest7", code: "1004"),
                "invest8": new Invest(id: "invest8", code: "1005")

        ]

        fundService.findFund(_ as String) >> { String code ->
            switch (code) {
                case "1000" -> new FundInfo(code, "fund0").setPriority(4)
                case "1001" -> new FundInfo(code, "fund1").setPriority(3)
                case "1002" -> new FundInfo(code, "fund2").setPriority(3)
                default -> null
            }
        }

        investService.setFundDataService(fundService)

        when:
        def ret = investService.getUserInvestedFunds("Alex")

        then:
        ret.size() == 3
        ret[0].id == "1002"
        ret[1].id == "1001"
        ret[2].id == "1000"

        when:
        ret = investService.getUserInvestedFunds("Bob")

        then:
        ret.size() == 2
        ret[0].id == "1001"
        ret[1].id == "1000"
    }

    def "addNewInvestor"() {
        given:
        investService = new InvestService()
        investService.passwordEncoder = passwordEncoder
        investService.investorMap = ["Alex": new Investor("Alex", "Alex Cheng", null)]

        investDao = Mock(InvestDao) {
            saveInvestors(_) >> {}
        }
        investService.setInvestDao(investDao)

        when:
        investService.addNewInvestor(new Investor("Alex", "Alex Zheng", null))
        then:
        thrown(EggFundException)

        when:
        def ret = investService.addNewInvestor(new Investor("Bob", "Bob Smith", null))
        then:
        ret.with {
            id == "Bob"
            name == "Bob Smith"
            password == "***"
            roles == Constants.DEFAULT_ROLE_USER
        }
        investService.investorMap.size() == 2
        investService.investorMap["Bob"] != null

        when:
        investDao = Mock(InvestDao) {
            saveInvestors(_) >> { throw new IOException() }
        }
        investService.setInvestDao(investDao)

        investService.addNewInvestor(new Investor("Cathy", "Cathy Zhang", null))
        then:
        thrown(EggFundException)
        investService.investorMap.size() == 2
    }

    def "getInvests"() {
        given:
        def investor1 = new Investor("Alex", "Alex Cheng", null)
        def investor2 = new Investor("Bob", "Bob Smith", null)

        investService = new InvestService()
        investService.investMap[investor1] = [
                "invest1": new Invest(id: "invest1", day: "2025-01-05", code: "1000"),
                "invest2": new Invest(id: "invest2", day: "2025-01-06", code: "1000"),
                "invest3": new Invest(id: "invest3", day: "2025-01-07", code: "1000", batch: 2),
                "invest4": new Invest(id: "invest4", day: "2025-01-05", code: "1002"),
        ]
        investService.investorMap = [
                "Alex": investor1,
                "Bob" : investor2
        ]

        when:
        def ret = investService.getInvests(
                "Alex",
                "1000",
                new DateRange(
                        LocalDate.parse("2025-01-05", Constants.DATE_FORMAT),
                        LocalDate.parse("2025-01-06", Constants.DATE_FORMAT)
                ),
                0
        )
        then:
        ret.size() == 2
        ret[0].id == "invest1"
        ret[1].id == "invest2"

        when:
        ret = investService.getInvests(
                "Alex",
                "1000",
                new DateRange(
                        LocalDate.parse("2025-01-05", Constants.DATE_FORMAT),
                        LocalDate.parse("2025-01-07", Constants.DATE_FORMAT)
                ),
                2
        )
        then:
        ret.size() == 1
        ret[0].id == "invest3"

        when:
        ret = investService.getInvests(
                "Bob",
                "1000",
                new DateRange(
                        LocalDate.parse("2025-01-05", Constants.DATE_FORMAT),
                        LocalDate.parse("2025-01-07", Constants.DATE_FORMAT)
                ),
                2
        )
        then:
        ret.isEmpty()

        when:
        ret = investService.getInvests(
                "Alex",
                "1000",
                new DateRange(
                        LocalDate.parse("2025-01-05", Constants.DATE_FORMAT),
                        LocalDate.parse("2025-01-07", Constants.DATE_FORMAT)
                ),
                -1
        )
        then:
        ret.size() == 3
        ret[0].id == "invest1"
        ret[1].id == "invest2"
        ret[2].id == "invest3"
    }

    def "updateInvestor"() {
        given:
        def investor1 = new Investor("Alex", "Alex Cheng", null)
        def investor2 = new Investor("Bob", "Bob Smith", null)

        investService = new InvestService()
        investService.passwordEncoder = passwordEncoder
        investService.investorMap = ["Alex": investor1]

        investDao = Mock(InvestDao) {
            saveInvestors(_) >> {}
        }
        investService.setInvestDao(investDao)

        when:
        investService.updateInvestor(investor2)
        then:
        thrown(EggFundException)

        when:
        def ret = investService.updateInvestor(new Investor("Alex", "Alex Pink", "icon1", "password", null))
        then:
        with(ret) {
            id == "Alex"
            name == "Alex Pink"
            icon == "icon1"
            password == "***"
            roles == Constants.DEFAULT_ROLE_USER
        }
        investService.investorMap.size() == 1
        investService.investorMap["Alex"] == ret

        when:
        investDao = Mock(InvestDao) {
            saveInvestors(_) >> { throw new IOException() }
        }
        investService.setInvestDao(investDao)

        investService.updateInvestor(new Investor("Alex", "Alex Teng", "icon2"))
        then:
        thrown(EggFundException)
        investService.investorMap.size() == 1
        with(investService.investorMap["Alex"]) {
            name == "Alex Pink"
            icon == "icon1"
        }
    }

    def "deleteInvestor"() {
        given:
        def investor1 = new Investor("Alex", "Alex Cheng", null)
        def investor2 = new Investor("Bob", "Bob Smith", null)

        investService = new InvestService()
        investService.investorMap = ["Alex": investor1, "Bob": investor2]
        investService.investMap[investor1] = ["invest1": new Invest(id: "invest1", code: "1000")]
        investService.investMap[investor2] = ["invest2": new Invest(id: "invest2", code: "1000")]

        investDao = Mock(InvestDao) {
            saveInvestors(_) >> {}
            deleteInvest(_) >> {}
        }
        investService.setInvestDao(investDao)

        when:
        investService.deleteInvestor("Bob")
        then:
        investService.investorMap.size() == 1
        investService.investorMap["Alex"] != null
        investService.investMap.size() == 1
        investService.investMap[investor1] != null

        when:
        investDao = Mock(InvestDao) {
            saveInvestors(_) >> { throw new IOException() }
            deleteInvest(_) >> {}
        }
        investService.setInvestDao(investDao)

        investService.deleteInvestor("Alex")
        then:
        thrown(EggFundException)
        investService.investorMap.size() == 1
        investService.investMap.size() == 1
    }

    def "generateInvestorSummary"() {
        given:
        investService = Spy(InvestService) {
            checkInvestor(_) >> new Investor("Alex", "Alex Cheng", null)
            getUserInvestedFunds(_) >> List.of(
                    new FundInfo(id: "code1", name: "name1"),
                    new FundInfo(id: "code2", name: "name2")
            )
            generateSummary(_, _, _, _, _, _) >> new InvestSummary()
        }
        fundService.getFundRTValues(_) >> [
                "code1": new FundRTValue("2025-01-01 15:00", 100, 1.1),
                "code2": new FundRTValue("2025-01-01 15:00", 100, 1.2)
        ]
        investService.setFundDataService(fundService as FundDataService)

        when:
        def ret = investService.generateInvestorSummary("Alex", "2025-01-01", "2025-01-02")

        then:
        ret != null
    }

    def "generateSummary throw exception"() {
        given:
        investService = Spy(InvestService) {
            getInvests(_, _, _, _) >> {
                [
                        new Invest(code: "1000", share: 1000, unitPrice: 1.0, day: "2025-01-01"),
                        new Invest(code: "1000", share: 1000, unitPrice: 1.1, day: "2025-01-02")
                ]
            }
        }

        investService.investorMap = [
                "Alex": new Investor("Alex", "Alex Cheng", null)
        ]

        fundService.checkFund(_) >> new FundInfo("1000", "fund0")
        fundService.getFundValues(_, _) >> List.of(
                new FundValue().setDay("2020-01-01").setUnitValue(1.0)
        )

        investService.setFundDataService(fundService as FundDataService)

        when:
        investService.generateSummary("Alex",
                "008888", "2025-01-01", "2025-01-02",
                0,
                0)

        then:
        thrown(EggFundException)
    }

    def "addInvests for one fund"() {
        given:
        def investor1 = new Investor("Alex", "Alex Cheng", null)

        investService = Spy(InvestService) {
            resetInvestPrice(_, _) >> {}
            updateInvestPrice(_) >> { Invest invest -> invest }
            overwriteInvests(_, _, _) >> ["invest10": new Invest(id: "invest10", code: "1000"),
                                          "invest11": new Invest(id: "invest11", code: "1000")]
            prepareAuditList(_, _) >> [new InvestAudit("2025-02-01",
                    new Invest(id: "invest10", code: "1000"),
                    null)]
            resetInvestUserIndex(_, _) >> {}
        }
        investService.investorMap << ["Alex": investor1]
        investService.investMap << [(investor1): ["invest1": new Invest(id: "invest1", code: "1000", day: "2025-01-05", share: 1000)]]
        investDao = Mock(InvestDao) {
            saveInvests(_, _) >> {}
        }
        investAuditDao = Mock(InvestAuditDao) {
            saveInvestAudits(_) >> {}
        }

        investService.setInvestDao(investDao)
        investService.setInvestAuditDao(investAuditDao)

        def invests = [new Invest(id: "invest100", code: "1000"), new Invest(id: "invest101", code: "1000")]

        when:
        investService.addInvests("Alex", new FundInfo("1001", "fund0"), invests, false)

        then:
        thrown(EggFundException)

        when:
        def ret = investService.addInvests("Alex", new FundInfo("1000", "fund0"), invests, false)
        then:
        ret.size() == 2
        investService.investMap[investor1].size() == 3
        investService.investAuditList.size() == 2

        when:
        ret = investService.addInvests("Alex", new FundInfo("1000", "fund0"), invests, true)
        then:
        ret.size() == 2
        investService.investMap[investor1].size() == 2
        investService.investAuditList.size() == 3

        when:
        investDao = Mock(InvestDao) {
            saveInvests(_, _) >> { throw new IOException() }
        }
        investService.setInvestDao(investDao)
        investService.addInvests("Alex", new FundInfo("1000", "fund0"), invests, true)
        then:
        thrown(EggFundException)
    }

    def "addInvests for multiple funds"() {
        given:
        def investor1 = new Investor("Alex", "Alex Cheng", null)

        investService = Spy(InvestService) {
            generateInvestMap(_, _, _, _, _,_, _) >> {
                Investor investor, FundInfo fund, List<Invest> investList, boolean overwrite, Map<String, Invest> userInvestMap, Map<String, Invest> newUserInvestMap, List<InvestAudit> auditList -> {
                    def invest1 = "invest-$fund.id-10"
                    def invest2 = "invest-$fund.id-11"
                    newUserInvestMap << userInvestMap
                    newUserInvestMap << [(invest1): new Invest(id: invest1, code: fund.id),
                                         (invest2): new Invest(id: invest2, code: fund.id)]
                    auditList << new InvestAudit("2025-02-01", null, new Invest(id: invest1, code: fund.id))
                    auditList << new InvestAudit("2025-02-01", null, new Invest(id: invest2, code: fund.id))
                    return [(invest1): new Invest(id: invest1, code: fund.id),
                            (invest2): new Invest(id: invest2, code: fund.id)]
                }
            }
        }
        investService.investorMap << ["Alex": investor1]
        investService.investMap << [(investor1): ["invest1": new Invest(id: "invest1", code: "1000", day: "2025-01-05", share: 1000)]]
        investDao = Mock(InvestDao) {
            saveInvests(_, _) >> {}
        }
        investAuditDao = Mock(InvestAuditDao) {
            saveInvestAudits(_) >> {}
        }
        fundService.checkFund(_) >> {
            String code -> {
                switch (code) {
                    case "1000", "1001" -> new FundInfo(id: code, name: code)
                    default -> throw new EggFundException()
                }
            }
        }

        investService.setInvestDao(investDao)
        investService.setInvestAuditDao(investAuditDao)
        investService.setFundDataService(fundService)

        when:
        def invests = [new Invest(id: "invest100", code: "1000"), new Invest(id: "invest101", code: "1002")]
        def ret = investService.addInvests("Alex", invests, false)

        then:
        thrown(EggFundException)

        when:
        invests = [new Invest(id: "invest100", code: "1000"), new Invest(id: "invest101", code: "1001")]
        ret = investService.addInvests("Alex", invests, false)

        then:
        ret.size() == 4
        investService.investMap[investor1].size() == 5
        investService.investAuditList.size() == 4
    }

    def "getInvestAudits"() {
        when:
        investService = Spy(InvestService) {
            checkInvestAudit() >> [
                    new InvestAudit("2025-01-01", new Invest(id: "invest1", code: "1000"), null),
                    new InvestAudit("2025-01-02", new Invest(id: "invest2", code: "1000"), null),
                    new InvestAudit("2025-01-03", new Invest(id: "invest3", code: "1001"), null)
            ]
        }

        then:
        investService.getInvestAudits(null).size() == 3

        when:
        investAuditDao = Mock(InvestAuditDao) {
            loadInvestAudits(_) >> [new InvestAudit("2025-01-01", new Invest(id: "invest1", code: "1000"), null)]
        }
        investService.setInvestAuditDao(investAuditDao)

        then:
        investService.getInvestAudits("2025-02-01").size() == 1

        when:
        investAuditDao = Mock(InvestAuditDao) {
            loadInvestAudits(_) >> { throw new IOException() }
        }
        investService.setInvestAuditDao(investAuditDao)

        then:
        investService.getInvestAudits("2025-02-01").size() == 0
    }

    def "prepareAuditList"() {
        given:
        investService = new InvestService()
        def userInvestMap = [
                "invest1": new Invest(id: "invest1", code: "1000", day: "2025-01-01", share: 1000),
                "invest2": new Invest(id: "invest2", code: "1000", day: "2025-01-02", share: 1000),
                "invest3": new Invest(id: "invest3", code: "1000", day: "2025-01-03", share: 2000)
        ]
        def newUserInvestMap = [
                "invest4": new Invest(id: "invest4", code: "1000", day: "2025-01-01", share: 1000),
                "invest2": new Invest(id: "invest2", code: "1000", day: "2025-01-02", share: 1001),
                "invest3": new Invest(id: "invest3", code: "1000", day: "2025-01-03", share: 2001)
        ]

        when:
        def ret = investService.prepareAuditList(userInvestMap, newUserInvestMap)

        then:
        ret.size() == 4
        ret.each { audit ->
            if (audit.oldInvest() == null) {
                audit.newInvest().getId() == "invest4"
            } else {
                switch (audit.oldInvest().id) {
                    case "invest1" -> audit.newInvest() == null
                    case "invest2" -> audit.newInvest().share == 1001
                    case "invest3" -> audit.newInvest().share == 2001
                    default -> false
                }
            }
        }
    }

    def "checkInvestAudit"() {
        given:
        investService = new InvestService()

        when:
        investService.investAuditList << new InvestAudit("2025-01-01", new Invest(id: "invest1", code: "1000"), null)
        investService.investAuditList << new InvestAudit("2025-01-01", new Invest(id: "invest2", code: "1000"), null)

        then:
        investService.checkInvestAudit().size() == 0
        investService.investAuditList.size() == 0

        when:
        def date = LocalDate.now(Constants.ZONE_ID).format(Constants.DATE_FORMAT)
        investService.investAuditList << new InvestAudit(date, new Invest(id: "invest1", code: "1000"), null)
        investService.investAuditList << new InvestAudit(date, new Invest(id: "invest2", code: "1000"), null)

        then:
        investService.checkInvestAudit().size() == 2
        investService.investAuditList.size() == 2
    }
}