package io.github.lujian213.eggfund.config

import io.swagger.v3.oas.models.OpenAPI
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(classes = [OpenApiConfig])
class OpenApiConfigSpec extends Specification {

    @Autowired
    OpenAPI customOpenAPI

    def "bean registration"() {
        expect:
        customOpenAPI
    }

}