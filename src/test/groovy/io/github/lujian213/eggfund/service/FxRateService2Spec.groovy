package io.github.lujian213.eggfund.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

@SpringBootTest(classes = [RestTemplate])
class FxRateService2Spec extends Specification{
    @Autowired
    RestTemplate restTemplate

    def "load currencies"() {
        when:
        FxRateService service = new FxRateService()
        service.setRestTemplate(restTemplate)
        def fxRates = service.loadCurrencies(["HKD"])
        then:
        fxRates.size() == 2
    }
}
