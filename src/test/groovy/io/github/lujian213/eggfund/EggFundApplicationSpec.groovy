package io.github.lujian213.eggfund


import io.github.lujian213.eggfund.config.ExecutorConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import spock.lang.Specification

@SpringBootTest(classes = ExecutorConfig)
class EggFundApplicationSpec extends Specification {
    @Autowired
    ApplicationContext context

    def "test main"() {
        expect:
        context.getBean("fundValueRetrieveExecutor")
    }
}
