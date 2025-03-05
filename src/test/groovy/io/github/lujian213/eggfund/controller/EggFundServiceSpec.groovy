package io.github.lujian213.eggfund.controller

import spock.lang.Specification
import io.github.lujian213.eggfund.exception.EggFundException
import io.github.lujian213.eggfund.model.Investor
import io.github.lujian213.eggfund.service.FundDataService
import io.github.lujian213.eggfund.service.InvestService
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class EggFundServiceSpec extends Specification {

    def "GetFundRTValue"() {
        given:
        def eggFundService = new EggFundService()
        def fundDataService = Mock(FundDataService)
        eggFundService.setFundDataService(fundDataService)
        when:
        eggFundService.getFundRTValue(codes as String)

        then:
        1 * fundDataService.getFundRTValues(codeList)
        where:
        codes           | codeList
        "code"          | ["code"]
        "code1, code2"  | ["code1", "code2"]
        "code1,, code2" | ["code1", "code2"]
    }

    def "handleException"() {
        given:
        def eggFundService = new EggFundService()
        when:
        eggFundService.handleException("error msg", exception)
        then:
        def e = thrown(ResponseStatusException)
        e.getStatusCode() == status
        where:
        status                           | exception
        HttpStatus.NOT_FOUND             | new EggFundException()
        HttpStatus.INTERNAL_SERVER_ERROR | new RuntimeException()
        HttpStatus.INTERNAL_SERVER_ERROR | new IOException()
    }

    def "runWithExceptionHandling with exception"() {
        given:
        def eggFundService = new EggFundService()
        when:
        eggFundService.runWithExceptionHandling("error msg", { throw new EggFundException() })
        then:
        def e = thrown(ResponseStatusException)
        e.getStatusCode() == HttpStatus.NOT_FOUND
    }

    def "runWithExceptionHandling with no exception"() {
        given:
        def eggFundService = new EggFundService()
        when:
        def result = eggFundService.runWithExceptionHandling("error msg", { return 1 })
        then:
        result == 1
    }

    def "addNewInvestor"() {
        given:
        def eggFundService = new EggFundService()
        def investService = Mock(InvestService) {
            addNewInvestor(_ as Investor) >> { Investor investor -> investor }
        }
        eggFundService.setInvestService(investService)
        expect:
        eggFundService.addNewInvestor(idInput, "Alex Cheng", null).getId() == idOutput
        where:
        idInput     | idOutput
        "Alex"      | "Alex"
        "Alex Chen" | "Alex_Chen"
    }
}