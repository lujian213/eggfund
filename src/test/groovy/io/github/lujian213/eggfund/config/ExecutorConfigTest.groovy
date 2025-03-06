package io.github.lujian213.eggfund.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import spock.lang.Specification

@SpringBootTest(classes = ExecutorConfig)
class ExecutorConfigTest extends Specification {
    @Autowired
    ApplicationContext context

    def "FundValueRetrieveExecutor"() {
        expect:
        context.getBean("fundValueRetrieveExecutor")
    }
}