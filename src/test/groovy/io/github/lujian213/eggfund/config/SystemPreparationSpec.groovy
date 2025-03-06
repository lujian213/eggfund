package io.github.lujian213.eggfund.config

import spock.lang.Specification
import io.github.lujian213.eggfund.service.FundDataService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = SystemPreparation)
class SystemPreparationSpec extends Specification {

    @Autowired
    CommandLineRunner runner
    @SpringBean
    FundDataService fundDataService = Mock(FundDataService)

    def "register bean"() {
        expect:
        runner
    }
}