package io.github.lujian213.eggfund.service.loader

import io.github.lujian213.eggfund.config.ExecutorConfig
import io.github.lujian213.eggfund.model.FundInfo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import java.time.LocalDate

@SpringBootTest(classes = [HKStockInfoLoader, LocalFundInfoLoader])
@Import ([RestTemplate, ExecutorConfig])
class FundInfoLoaderSpec extends Specification{
    @Autowired
    HKStockInfoLoader hkStockInfoLoader

    @Autowired
    RestTemplate restTemplate

    def "load hk 09988 name"() {
        given:
        def fundInfo = new FundInfo(id: "09988")
        when:
        hkStockInfoLoader.loadFund(fundInfo)
        then:
        with(fundInfo) {
            name == "阿里巴巴-W"
            currency == HKStockInfoLoader.CURRENCY
        }
    }

    def "load hk 09988 value"() {
        when:
        def from = LocalDate.now().minusDays(30)
        def to = LocalDate.now().minusDays(15)
//        def from = LocalDate.parse("2025-09-01", Constants.DATE_FORMAT)
//        def to = LocalDate.parse("2025-09-11", Constants.DATE_FORMAT)
        def values = hkStockInfoLoader.loadFundValue("09988", from, to)

        then:
        values.size() > 0
    }

    def "load hk rt values"() {
        when:
        hkStockInfoLoader.loadFundRTValues()
        then:
        println "hkStockInfoLoader.rtValueMap.size() = ${hkStockInfoLoader.rtValueMap.size()}"
        hkStockInfoLoader.rtValueMap.size() > 0
        hkStockInfoLoader.rtValueMap."09988"
    }
}
