package io.github.lujian213.eggfund.config

import spock.lang.Specification
import io.github.lujian213.eggfund.model.Investor
import io.github.lujian213.eggfund.service.InvestService
import io.micrometer.core.instrument.binder.MeterBinder
import io.micrometer.core.instrument.search.MeterNotFoundException
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = EggFundMeterConfig)
class EggFundMeterConfigSpec extends Specification {

    @SpringBean
    InvestService investService = Mock(InvestService)
    @Autowired
    MeterBinder meterBinder

    def "CreateEggFundMeterBinder"() {
        expect:
        meterBinder
    }

    def "bind"() {
        given:
        def registry = new SimpleMeterRegistry()
        when:
        investService.getAllInvestors() >> [new Investor(id: "id1", name: "name1"), new Investor(id: "id2", name: "name2")]
        investService.getUserInvestedFunds(_) >> []
        meterBinder.bindTo(registry)
        then:
        registry.get("eggfund.invested.funds").gauges().size() == 2
        registry.get("eggfund.invested.funds").tag("investor", "name1").gauge()
        registry.get("eggfund.invested.funds").tag("investor", "name2").gauge()
        when:
        registry.get("eggfund.invested.funds").tag("investor", "name3").gauge()
        then:
        thrown(MeterNotFoundException)
    }
}