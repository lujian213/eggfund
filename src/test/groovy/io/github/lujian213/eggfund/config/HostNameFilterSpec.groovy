package io.github.lujian213.eggfund.config

import spock.lang.Specification
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class HostNameFilterSpec extends Specification {

    def "DoFilter"() {
        given:
        def filter = new HostNameFilter()
        filter.whitelistHosts = whitelistHosts
        def request = Mock(HttpServletRequest) {
            getRemoteAddr() >> remoteAddr
            getRequestURI() >> uri
        }
        def response = Mock(HttpServletResponse)
        def chain = Mock(FilterChain)
        when:
        filter.init(null)
        filter.doFilter(request, response, chain)
        then:
        m * chain.doFilter(request, response)
        n * response.sendError(HttpServletResponse.SC_FORBIDDEN, _)
        where:
        whitelistHosts  | remoteAddr        | uri                  | m | n
        ["*"]           | "10.1.1.100"      | "/a/b"               | 1 | 0
        ["10.1.1.100"]  | "10.1.1.100"      | "/a/b"               | 1 | 0
        ["10.1.1.101"]  | "10.1.1.100"      | "/a/b"               | 0 | 1
        ["10.1.1.101"]  | "127.0.0.1"       | "/a/b"               | 1 | 0
        ["10.1.1.101"]  | "0:0:0:0:0:0:0:1" | "/a/b"               | 1 | 0
        ["dummyserver"] | "10.0.0.100"      | "/a/b"               | 0 | 1
        ["dummyserver"] | "dummyserver"     | "/a/b"               | 1 | 0
        ["dummyserver"] | "dummyserver"     | "/actuator/shutdown" | 0 | 1
        ["dummyserver"] | "127.0.0.1"       | "/actuator/shutdown" | 1 | 0
        ["dummyserver"] | "0:0:0:0:0:0:0:1" | "/actuator/shutdown" | 1 | 0

    }

}