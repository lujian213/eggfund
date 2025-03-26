package io.github.lujian213.eggfund.controller

import spock.lang.Specification
import io.github.lujian213.eggfund.model.*
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest
import org.springframework.graphql.test.tester.GraphQlTester


import java.time.LocalDate

@GraphQlTest(EggFundController.class)
class EggFundControllerTestSpec extends Specification {
    @Autowired
    GraphQlTester graphQlTester
    @SpringBean
    EggFundService eggFundService = Mock(EggFundService)

    def "test context"() {
        expect:
        graphQlTester
        eggFundService
    }

    def "testGetAllFunds"() {
        when:
        def fundList = [new FundInfo(id: "1", name: "test", alias: "dummy")]
        eggFundService.getAllFunds() >> fundList
        then:
        this.graphQlTester.document("""
                        query MyQuery {
                            getAllFunds {
                                alias
                                category
                            }
                        }
                        """)
                .execute()
                .path("getAllFunds[0].alias")
                .entity(String.class)
                .isEqualTo("dummy")
    }

    def "testGetAllUserInvestedFunds"() {
        when:
        def fundList = [new FundInfo(id: "1", name: "test", alias: "dummy")]
        eggFundService.getAllUserInvestedFunds("user1") >> fundList
        then:
        graphQlTester.document("""
                query MyQuery {
                    getAllUserInvestedFunds(id: "user1") {
                        alias category
                    }
                }
                """)
                .execute()
                .path("getAllUserInvestedFunds[0].alias")
                .entity(String.class)
                .isEqualTo("dummy")
    }

    def "testGetAllInvestors"() {
        when:
        def investorList = [new Investor("1", "test", "icon")]
        eggFundService.getAllInvestors() >> investorList
        then:
        graphQlTester.document("""
                        query MyQuery {
                            getAllInvestors {
                                id 
                                name
                            }
                        }
                    """)
                .execute()
                .path("getAllInvestors[0].id")
                .entity(String.class)
                .isEqualTo("1")
    }

    def "testGetInvestorsByFund"() {
        when:
        def investorList = [new Investor("1", "test", "icon")]
        eggFundService.getInvestors("10000") >> investorList
        then:
        graphQlTester.document("""
                    query MyQuery {
                        getInvestorsByFund(code: "10000") {
                            id
                            name
                        }
                    }
                    """)
                .execute()
                .path("getInvestorsByFund[0].id")
                .entity(String.class)
                .isEqualTo("1")
    }

    def "testGetFundValues"() {
        when:
        def fundValueList = [new FundValue("2020-01-01", 100.0, 100, 1.1)]
        eggFundService.getFundValues("10000", "2020-01-01", "2020-01-05") >> fundValueList
        then:
        graphQlTester.document("""
                        query MyQuery {
                            getFundValues(code: "10000", from: "2020-01-01", to: "2020-01-05") {
                                day
                                unitValue
                            }
                        }
                        """)
                .execute()
                .path("getFundValues[0].day")
                .entity(String.class)
                .isEqualTo("2020-01-01")
    }

    def "testGetFundRTValue"() {
        when:
        def fundRTValueMap = ["10000": new FundRTValue("2025-01-01 15:00", 100, 1.1)]
        eggFundService.getFundRTValue("10000") >> fundRTValueMap
        then:
        graphQlTester.document("""
                query MyQuery {
                    getFundRTValue(codes: "10000") {
                        key
                        value {
                            time
                        }
                    }
                }
                """)
                .execute()
                .path("getFundRTValue[0].key")
                .entity(String.class)
                .isEqualTo("10000")
    }

    def 'testGetFundRTValueHistory'() {
        given:
        def code = "10000"
        when:
        def fundRTValueList = [new FundRTValue("2025-01-01 15:00", 100, 1.1)]
        eggFundService.getFundRTValueHistory(code) >> fundRTValueList
        then:
        graphQlTester.document("""
                query MyQuery {
                    getFundRTValueHistory(code: "$code") {
                        time
                        unitValue
                    }
                }
                """)
                .execute()
                .path("getFundRTValueHistory[0].time")
                .entity(String.class)
                .isEqualTo("2025-01-01 15:00")
    }

    def "testGetInvests"() {
        when:
        def investList = [new Invest(day: "2020-01-01", code: "10000", userIndex: 1, id: "invest1")]
        eggFundService.getInvests("user1", "10000", "2020-01-01", "2020-01-05", -1) >> investList
        then:
        graphQlTester.document("""
                        query MyQuery {
                            getInvests(id: "user1", code: "10000", from: "2020-01-01", to: "2020-01-05", batch: -1) {
                                id
                                day
                            }
                        }
                        """)
                .execute()
                .path("getInvests[0].id")
                .entity(String.class)
                .isEqualTo("invest1")
    }

    def 'testAddNewFund'() {
        when:
        def fundInfo = new FundInfo("10000", "test")
        eggFundService.addNewFund("10000", fundInfo) >> fundInfo
        then:
        graphQlTester.document("""
                        mutation MyMutation {
                            addNewFund(fundInfo: {id: "10000", alias: "test"}) {
                                id
                                name
                            }
                        }
                        """)
                .execute()
                .path("addNewFund.id")
                .entity(String.class)
                .isEqualTo("10000")
    }

    def 'testAddNewInvestor'() {
        when:
        def investor = new Investor("user1", "test", "icon")
        eggFundService.addNewInvestor ( "user1", "test", "icon" ) >> investor

        then:
        graphQlTester.document("""
                    mutation MyMutation {
                        addNewInvestor(id: "user1", name: "test", icon: "icon") {
                            id name
                        }
                    }
                    """)
                .execute()
                .path("addNewInvestor.id")
                .entity(String.class)
                .isEqualTo("user1")
    }

    def 'testAddNewInvest'() {
        when:
        def investList = [new Invest(day: "2020-01-01", code: "10000", userIndex: 1, id: "invest1")]
        eggFundService.addNewInvest("user1", "10000", investList) >> investList
        then:
        graphQlTester.document("""
                        mutation MyMutation {
                            addNewInvest(id: "user1", code: "10000", invests: [{day: "2020-01-01", code: "10000", id: "invest1", share: 1}]) {
                                id
                                day
                            }
                        }
                        """)
                .execute()
                .path("addNewInvest[0].id")
                .entity(String.class)
                .isEqualTo("invest1")
    }

    def 'testUpdateFund'() {
        when:
        def fundInfo = new FundInfo("10000", "test")
        eggFundService.updateFund("10000", fundInfo) >> fundInfo
        then:
        graphQlTester.document("""
                        mutation MyMutation {
                            updateFund(fundInfo: {id: "10000", alias: "test"}) {
                                id
                                name
                            }
                        }
                        """)
                .execute()
                .path("updateFund.id")
                .entity(String.class)
                .isEqualTo("10000")
    }

    def 'testUpdateFundValues'() {
        when:
        eggFundService.updateFundValues("10000", "2020-01-01", "2020-01-05") >> {}
        then:
        graphQlTester.document("""
                        mutation MyMutation {
                            updateFundValues(code: "10000", from: "2020-01-01", to: "2020-01-05")
                        }
                        """)
                .execute()
                .path("updateFundValues")
                .entity(Boolean.class)
                .isEqualTo(true)
    }

    def 'testUpdateInvestor'() {
        when:
        def investor = new Investor("user1", "test", "icon")
        eggFundService.updateInvestor("user1", "test", "icon", "password") >> investor
        then:
        graphQlTester.document("""
                    mutation MyMutation {
                        updateInvestor (id: "user1", name: "test", icon: "icon", password: "password") {
                            id
                            name
                        }
                    }
                    """)
                .execute()
                .path("updateInvestor.id")
                .entity(String.class)
                .isEqualTo("user1")
    }

    def 'testUpdateInvest'() {
        when:
        def invest = new Invest(day: "2020-01-01", code: "10000", userIndex: 1, id: "invest1")
        eggFundService.updateInvest("user1", invest) >> invest
        then:
        graphQlTester.document("""
                        mutation MyMutation {
                            updateInvest(id: "user1", invest: {day: "2020-01-01", code: "10000", id: "invest1", share: 1}) {
                                id
                                day
                            }
                        }
                        """)
                .execute()
                .path("updateInvest.id")
                .entity(String.class)
                .isEqualTo("invest1")
    }

    def 'testDeleteInvest'() {
        when:
        eggFundService.deleteInvest("user1", "invest1") >> {}
        then:
        graphQlTester.document("""
                mutation MyMutation {
                    deleteInvest(id: "user1", investId: "invest1")
                }
                """)
                .execute()
                .path("deleteInvest")
                .entity(Boolean.class)
                .isEqualTo(true)
    }

    def 'testDeleteInvestor'() {
        when:
        eggFundService.deleteInvestor("user1") >> {}
        then:
        graphQlTester.document("""
                mutation MyMutation {
                    deleteInvestor(id: "user1")
                }
                """)
                .execute()
                .path("deleteInvestor")
                .entity(Boolean.class)
                .isEqualTo(true)
    }

    def 'testDeleteFund'() {
        when:
        eggFundService.deleteFund("10000") >> {}
        then:
        graphQlTester.document("""
                mutation MyMutation {
                    deleteFund(code: "10000")
                }
                """)
                .execute()
                .path("deleteFund")
                .entity(Boolean.class)
                .isEqualTo(true)
    }

    def 'testDisableInvest'() {
        when:
        def invest = new Invest(day: "2020-01-01", code: "10000", userIndex: 1, id: "invest1", enabled: false)
        eggFundService.disableInvest("user1", "invest1", false) >> invest
        then:
        graphQlTester.document("""
                mutation MyMutation {
                    disableInvest(id: "user1", investId: "invest1", enabled: false) {
                        id
                        enabled
                    }
                }
                """)
                .execute()
                .path("disableInvest.id")
                .entity(String.class)
                .isEqualTo("invest1")
    }

    def 'testGenerateInvestSummary'() {
        when:
        def summary = Mock(InvestSummary)
        eggFundService.generateInvestSummary("user1", "10000", "2020-01-01", "2020-01-05", -1, 1.1f) >> summary
        then:
        graphQlTester.document("""
                    query MyQuery {
                        generateInvestSummary (id: "user1", code: "10000", from: "2020-01-01", to: "2020-01-05", batch: -1, raiseRate: 1.1) {
                            totalLongAmt
                        }
                    }
                    """)
                .execute()
                .path("generateInvestSummary.totalLongAmt")
                .entity(Double.class)
                .isEqualTo(summary.getTotalLongAmt())
    }

    def 'testGetInvestAudits'() {
        given:
        def date = "2020-01-01"
        def investAudits = [new InvestAudit(date, new Invest(day: "2020-01-01", id: "invest1"), null)]
        eggFundService.getInvestAudits(date) >> investAudits
        expect:
        graphQlTester.document("""
                query MyQuery {
                    getInvestAudits(date: "$date") {
                        day
                    }
                }
                """)
                .execute()
                .path("getInvestAudits[0].day")
                .entity(String.class)
                .isEqualTo(date)
    }

    def "testGenerateInvestorSummary"() {
        when:
        def summary = Mock(InvestorSummary)
        eggFundService.generateInvestorSummary("user1", "2020-01-01", "2020-01-05") >> summary
        then:
        graphQlTester.document("""
                query MyQuery {
                    generateInvestorSummary (id: "user1", from: "2020-01-01", to: "2020-01-05") {
                        totalLongAmt
                    }
                }
                """)
                .execute()
                .path("generateInvestorSummary.totalLongAmt")
                .entity(Double.class)
                .isEqualTo(summary.getTotalLongAmt())
    }
}