package io.github.lujian213.eggfund.config

import spock.lang.Specification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest(classes = WebProxyConfig)
@TestPropertySource(properties = [
        "webproxy.type=HTTP",
        "webproxy.host=proxyhost",
        "webproxy.port=8080"
])
class WebProxyConfigSpec extends Specification {
    @Autowired
    Proxy proxy

    def "CreateProxy"() {
        expect:
        proxy.type() == Proxy.Type.HTTP
        proxy.address().hostName == "proxyhost"
        proxy.address().port == 8080
    }
}

@SpringBootTest(classes = WebProxyConfig)
@TestPropertySource(properties = [
        "webproxy.type=",
        "webproxy.host=proxyhost",
        "webproxy.port=8080"
])
class WebProxyConfigSpec2 extends Specification {
    @Autowired
    Proxy proxy

    def "CreateProxy"() {
        expect:
        proxy == Proxy.NO_PROXY
    }
}