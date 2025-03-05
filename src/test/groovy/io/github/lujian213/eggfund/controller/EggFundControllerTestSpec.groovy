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

//    def "test context"() {
//        expect:
//        graphQlTester
//        eggFundService
//    }

}