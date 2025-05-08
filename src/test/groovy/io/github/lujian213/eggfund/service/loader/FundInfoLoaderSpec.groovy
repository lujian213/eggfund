package io.github.lujian213.eggfund.service.loader


import io.github.lujian213.eggfund.config.ExecutorConfig
import io.github.lujian213.eggfund.utils.Constants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import java.time.LocalDate

@SpringBootTest(classes = [HKStockInfoLoader.class, LocalFundInfoLoader])
@Import ([RestTemplate, ExecutorConfig])
class FundInfoLoaderSpec extends Specification{
    @Autowired
    HKStockInfoLoader hkStockInfoLoader

    def "load hk 09988 name"() {
        expect:
        hkStockInfoLoader.loadFundName("09988") == "阿里巴巴-W"
    }

    def "load hk 09988 value"() {
        when:
        def from = LocalDate.parse("2025-04-01", Constants.DATE_FORMAT)
        def to = LocalDate.parse("2025-04-02", Constants.DATE_FORMAT)
        def values = hkStockInfoLoader.loadFundValue("09988", from, to)

        then:
        values.size() == 2
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
