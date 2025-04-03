package io.github.lujian213.eggfund.controller

import io.github.lujian213.eggfund.config.SecurityConfig
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import io.github.lujian213.eggfund.exception.EggFundException
import io.github.lujian213.eggfund.model.*
import io.github.lujian213.eggfund.service.FundDataService
import io.github.lujian213.eggfund.service.InvestService
import io.github.lujian213.eggfund.utils.Constants
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc

import java.time.LocalDate
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(EggFundService)
@ContextConfiguration(classes = [SecurityConfig.class, EggFundService.class])
@WithMockUser(roles="USER")
class EggFundServiceTestSpec extends Specification {

    @SpringBean
    FundDataService fundDataService = Mock(FundDataService)
    @SpringBean
    InvestService investService = Mock(InvestService)
    @Autowired
    MockMvc mockMvc

    def "getAllFunds"() {
        when:
        def fundList = [new FundInfo("1", "test")]
        fundDataService.getAllFunds() >> fundList
        then:
        mockMvc.perform(get("/funds"))
                .andExpect(status().isOk())
                .andExpect(content().json(Constants.MAPPER.writeValueAsString(fundList)))
    }

    def "testGetAllUserInvestedFunds"() {
        when:
        def fundList = [new FundInfo("1", "test")]
        investService.getUserInvestedFunds("user1") >> fundList
        then:
        mockMvc.perform(get("/funds/user1"))
                .andExpect(status().isOk())
                .andExpect(content().json(Constants.MAPPER.writeValueAsString(fundList)))
    }

    def "testGetAllInvestors"() {
        when:
        def investorList = [new Investor("user1", "test", null)]
        investService.getAllInvestors() >> investorList
        then:
        mockMvc.perform(get("/investors"))
                .andExpect(status().isOk())
                .andExpect(content().json(Constants.MAPPER.writeValueAsString(investorList)))
    }

    def "testGetInvestors"() {
        when:
        def investor = new Investor("user1", "test", null)
        def fundInfo = new FundInfo("10000", "test")
        def investorList = [investor]
        fundDataService.checkFund("10000") >> fundInfo
        fundDataService.checkFund("10001") >> { throw new EggFundException() }
        investService.getInvestors("10000") >> investorList
        then:
        mockMvc.perform(get("/investors/10000"))
                .andExpect(status().isOk())
                .andExpect(content().json(Constants.MAPPER.writeValueAsString(investorList)))
        mockMvc.perform(get("/investors/10001"))
                .andExpect(status().isNotFound())
    }

    def "testGetFundValues"() {
        when:
        def fundValueList = List.of(new FundValue().setDay("2020-01-01").setUnitValue(1.0))
        def from = LocalDate.parse("2020-01-01", Constants.DATE_FORMAT)
        def to = LocalDate.parse("2020-01-05", Constants.DATE_FORMAT)
        fundDataService.getFundValues("10000", new DateRange(from, to)) >> fundValueList
        then:
        mockMvc.perform(get("/values/10000").param("from", "2020-01-01").param("to", "2020-01-05"))
                .andExpect(status().isOk())
                .andExpect(content().json(Constants.MAPPER.writeValueAsString(fundValueList)))
    }

    def "testGetFundRTValue"() {
        when:
        def fundRTValueMap = ["10000": new FundRTValue("2025-01-01 15:00", 1.0, 1.1),
                              "10001": new FundRTValue("2025-01-01 15:00", 1.0, 1.1)]
        def codeList = ["10000", "10001"]
        fundDataService.getFundRTValues(codeList) >> fundRTValueMap
        then:
        mockMvc.perform(get("/rtvalues").param("codes", "10000 , , 10001"))
                .andExpect(status().isOk())
                .andExpect(content().json(Constants.MAPPER.writeValueAsString(fundRTValueMap)))
    }

    def "testGetFundRTValueHistory"() {
        when:
        def fundRTValueHistory = [new FundRTValue("2025-01-01 15:00", 1.0, 1.1),
                                  new FundRTValue("2025-01-01 15:15", 1.0, 1.1)]
        fundDataService.getFundRTValueHistory("1000") >> fundRTValueHistory
        then:
        mockMvc.perform(get("/rtvalues/history").param("code", "1000"))
                .andExpect(status().isOk())
                .andExpect(content().json(Constants.MAPPER.writeValueAsString(fundRTValueHistory)))
    }


    def "testGetInvests"() {
        when:
        def investList1 = [new Invest(day: "2020-01-01", code: "10000", userIndex: 1)]
        def investList2 = [new Invest(day: "2020-01-02", code: "10000", userIndex: 1)]
        def from = LocalDate.parse("2020-01-01", Constants.DATE_FORMAT)
        def to = LocalDate.parse("2020-01-05", Constants.DATE_FORMAT)
        investService.getInvests("user1", "10000", new DateRange(from, to), -1) >> investList1
        investService.getInvests("user1", "10000", new DateRange(null, null), 1) >> investList2
        then:
        mockMvc.perform(get("/invests/user1/10000").param("from", "2020-01-01").param("to", "2020-01-05"))
                .andExpect(status().isOk())
                .andExpect(content().json(Constants.MAPPER.writeValueAsString(investList1)))
        mockMvc.perform(get("/invests/user1/10000").param("batch", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(Constants.MAPPER.writeValueAsString(investList2)))
    }

    def "getInvestAudits"() {
        when:
        def investAudits = [new InvestAudit("2025-01-01", new Invest(day: "2025-01-01", id: "invest1"), null),
                            new InvestAudit("2025-01-01", null, new Invest(day: "2025-01-01", id: "invest2"))]
        1 * investService.getInvestAudits(null) >> investAudits
        1 * investService.getInvestAudits("2025-01-01") >> investAudits
        then:
        mockMvc.perform(get("/audit"))
                .andExpect(status().isOk())
                .andExpect(content().json(Constants.MAPPER.writeValueAsString(investAudits)))
        mockMvc.perform(get("/audit").param("date", "2025-01-01"))
                .andExpect(status().isOk())
                .andExpect(content().json(Constants.MAPPER.writeValueAsString(investAudits)))
    }

    @WithMockUser(roles = ["ADMIN"])
    def "testAddNewFund"() {
        when:
        def fundInfo = new FundInfo("10000", "test")
        fundDataService.addNewFund(fundInfo) >> fundInfo
        then:
        mockMvc.perform(put("/fund/10000").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(Constants.MAPPER.writeValueAsString(fundInfo)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json(Constants.MAPPER.writeValueAsString(fundInfo)))
    }

    @WithMockUser(username = "user1")
    def "testAddNewInvest"() {
        when:
        def investList = [new Invest(day: "2020-01-01", code: "10000", userIndex: 1, id: "invest1")]
        def fundInfo = new FundInfo("10000", "test")
        fundDataService.checkFund("10000") >> fundInfo
        investService.addInvests("user1", fundInfo, investList, false) >> investList
        then:
        mockMvc.perform(put("/invest/user1/10000").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(Constants.MAPPER.writeValueAsString(investList)))
                .andExpect(status().isOk())
                .andExpect(content().json(Constants.MAPPER.writeValueAsString(investList)))
    }

    @WithMockUser(roles = ["ADMIN"])
    def "testAddNewInvestor"() {
        when:
        def investor = new Investor("user1", "test", null)
        investService.addNewInvestor(investor) >> investor
        then:
        mockMvc.perform(put("/investor").param("id", "user1").param("name", "test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json(Constants.MAPPER.writeValueAsString(investor)))
    }

    @WithMockUser(roles = ["ADMIN"])
    def "testUpdateFund"() {
        when:
        def fundInfo1 = new FundInfo("10000", "test")
        def fundInfo2 = new FundInfo("10002", "test")
        fundDataService.updateFund(fundInfo1) >> fundInfo1
        then:
        mockMvc.perform(post("/fund/10000").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(Constants.MAPPER.writeValueAsString(fundInfo2)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json(Constants.MAPPER.writeValueAsString(fundInfo1)))
    }

    def "testUpdateFundValues"() {
        when:
        def from = LocalDate.parse("2020-01-01", Constants.DATE_FORMAT)
        def to = LocalDate.parse("2020-01-05", Constants.DATE_FORMAT)
        fundDataService.updateFundValues("10000", new DateRange(from, to)) >> {}
        then:
        mockMvc.perform(post("/value/10000").param("from", "2020-01-01").param("to", "2020-01-05"))
                .andExpect(status().isOk())
    }

    @WithMockUser(username = "user1")
    def "testUpdateInvestor"() {
        when:
        def investor = new Investor("user1", "test", null)
        investService.updateInvestor(investor) >> investor
        then:
        mockMvc.perform(post("/investor/user1").param("name", "test1").param("password", "password"))
                .andExpect(status().isOk())
                .andExpect(content().json(Constants.MAPPER.writeValueAsString(investor)))
    }

    @WithMockUser(username = "invest1")
    def "testUpdateInvest"() {
        when:
        def invest = new Invest(day: "2020-01-01", code: "10000", userIndex: 1, id: "invest1")
        def fundInfo = new FundInfo("10000", "test")
        fundDataService.checkFund("10000") >> fundInfo
        investService.updateInvest("invest1", invest) >> invest
        then:
        mockMvc.perform(post("/invest/{id}", "invest1").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(Constants.MAPPER.writeValueAsString(invest)))
                .andExpect(status().isOk())
                .andExpect(content().json(Constants.MAPPER.writeValueAsString(invest)))
    }

    @WithMockUser(username = "user1")
    def "testDeleteInvest"() {
        when:
        1 * investService.deleteInvests("user1", ["invest1"]) >> {}
        then:
        mockMvc.perform(delete("/invest/{id}/{investId}", "user1", "invest1"))
                .andExpect(status().isOk())
    }

    @WithMockUser(roles = ["ADMIN"])
    def "testDeleteInvests"() {
        when:
        1 * investService.deleteInvests("user1", ["invest1", "invest2"]) >> {}
        then:
        mockMvc.perform(delete("/invest/{id}", "user1").param("investIds", "invest1", "invest2"))
                .andExpect(status().isOk())
    }

    @WithMockUser(roles = ["ADMIN"])
    def "testDeleteInvestor"() {
        when:
        investService.deleteInvestor("user") >> {}
        then:
        mockMvc.perform(delete("/investor/{id}", "user1"))
                .andExpect(status().isOk())
    }

    @WithMockUser(roles = ["ADMIN"])
    def "testDeleteFund"() {
        when:
        fundDataService.deleteFund("10000") >> {}
        then:
        mockMvc.perform(delete("/fund/{code}", "10000"))
                .andExpect(status().isOk())
    }

    def "testDisableInvest"() {
        when:
        def invest1 = new Invest(day: "2020-01-01", code: "10000", userIndex: 1, id: "invest1", enabled: false)
        def invest2 = new Invest(day: "2020-01-01", code: "10000", userIndex: 1, id: "invest2", enabled: true)
        investService.disableInvest("user1", "invest1", false) >> invest1
        investService.disableInvest("user1", "invest2", true) >> invest2
        then:
        mockMvc.perform(post("/disableinvest/{id}/{investId}", "user1", "invest1")
                .param("enabled", "false"))
                .andExpect(status().isOk())
                .andExpect(content().json(Constants.MAPPER.writeValueAsString(invest1)))
        mockMvc.perform(post("/disableinvest/{id}/{investId}", "user1", "invest2"))
                .andExpect(status().isOk())
                .andExpect(content().json(Constants.MAPPER.writeValueAsString(invest2)))
    }

    def "testGenerateInvestSummary"() {
        expect:
        mockMvc.perform(post("/summary/{id}/{code)", "user1", "10000")
                .param("from", "2025-01-01")
                .param("to", "2025-01-05").param("raiseRate", "1.1"))
                .andExpect(status().isOk())
    }

    def "testGenerateInvestorSummary"() {
        expect:
        mockMvc.perform(post("/summary/fid)", "user1")
                .param("from", "2025-01-01")
                .param("to", "2025-01-05"))
                .andExpect(status().isOk())
    }

    @WithMockUser(username = "user1")
    def "testUploadInvests"() {
        when:
        def fundInfo = new FundInfo("10000", "test")
        def investList = [new Invest(day: "2020-01-01", code: "10000", userIndex: 1, id: "invest1")]
        fundDataService.checkFund("10000") >> fundInfo
        investService.addInvests("user1", fundInfo, investList, true) >> investList
        def file = new MockMultipartFile(
                "file",
                "invests.json",
                MediaType.APPLICATION_JSON_VALUE,
                Constants.MAPPER.writeValueAsBytes(investList)
        )
        then:
        mockMvc.perform(multipart("/uploadinvests/{id}/{code}", "user1", "10000")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(content().json(Constants.MAPPER.writeValueAsString(investList)))
    }

    @WithMockUser(username = "user1")
    def "testUploadInvests with multiple funds"() {
        when:
        def investList = [new Invest(day: "2020-01-01", code: "10000", userIndex: 1, id: "invest1")]
        investService.addInvests("user1", investList, true) >> investList
        def file = new MockMultipartFile(
                "file",
                "invests.json",
                MediaType.APPLICATION_JSON_VALUE,
                Constants.MAPPER.writeValueAsBytes(investList)
        )
        then:
        mockMvc.perform(multipart("/uploadinvests/{id}", "user1")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(content().json(Constants.MAPPER.writeValueAsString(investList)))
    }

    def "testExportInvests"() {
        when:
        def invests = [new Invest(day: "2020-01-01", code: "10000", userIndex: 1, id: "invest1")]
        def id = "user1"
        def code = "10000"
        def from = "2020-01-01"
        def to = "2020-01-05"
        def batch = -1
        investService.getInvests(id, code, new DateRange(LocalDate.parse(from), LocalDate.parse(to)), batch) >> invests
        then:
        mockMvc.perform(get("/exportinvests/{id}/{code}", id, code)
                .param("from", from)
                .param("to", to)
                .param("batch", String.valueOf(batch)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Content-Disposition", "attachment; filename=invests.json;"))
                .andExpect(content().json(Constants.MAPPER.writeValueAsString(invests)))
    }

    @WithAnonymousUser
    def "return 401 without user Login"() {
        expect:
        this.mockMvc.perform(get("/loginUser"))
                .andExpect(status().is(401))
    }

    def "testLoginUser with user Login"() {
        expect:
        this.mockMvc.perform(get("/loginUser"))
                .andExpect(status().isOk())
    }

    def "should return forbidden if not admin"() {
        when:
        def fundInfo = new FundInfo("10000", "test")
        then:
        mockMvc.perform(put("/fund/10000").contentType(MediaType.APPLICATION_JSON_VALUE).content(Constants.MAPPER.writeValueAsString(fundInfo)))
                .andExpect(status().isForbidden())
    }

    @WithMockUser(username = "user2")
    def "should return forbidden if not self"() {
        when:
        def investList = [new Invest(day: "2020-01-01", code: "10000", userIndex: 1, id: "invest1")]
        then:
        mockMvc.perform(put("/invest/user1/10000").contentType(MediaType.APPLICATION_JSON_VALUE).content(Constants.MAPPER.writeValueAsString(investList)))
                .andExpect(status().isForbidden())
    }

}