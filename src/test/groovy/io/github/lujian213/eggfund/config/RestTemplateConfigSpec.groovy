package io.github.lujian213.eggfund.config

import spock.lang.Specification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpResponse
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@SpringBootTest(classes = RestTemplateConfig)
@Import (WebProxyConfig)
class RestTemplateConfigSpec extends Specification {

    @Autowired
    RestTemplate template
    def "restTemplate"() {
        expect:
        template
    }

    def "intercept" () {
        given:
        def inst = new RestTemplateConfig()
        def request = Mock(HttpRequest)
        def httpHeaders = new HttpHeaders(new LinkedMultiValueMap<String, String>(["Content-Type": [contentType]]))
        def execution = Mock(ClientHttpRequestExecution) {
            execute(_, _) >> Mock(ClientHttpResponse) {
                getHeaders() >> httpHeaders
            }
        }
        when:
        def resp = inst.intercept(request, new byte[0], execution)
        then:
        resp.getHeaders().getFirst("Content-Type") == actualType
        where:
        contentType                          | actualType
        "application/json"                   | "application/json"
        "application/json;charset=UTF-8,gbk" | "application/json;charset=UTF-8"
    }

}