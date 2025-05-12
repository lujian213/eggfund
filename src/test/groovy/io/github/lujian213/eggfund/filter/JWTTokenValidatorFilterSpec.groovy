package io.github.lujian213.eggfund.filter

import jakarta.servlet.FilterChain
import io.jsonwebtoken.Claims
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import spock.lang.Specification


class JWTTokenValidatorFilterSpec extends Specification {

    JWTTokenValidatorFilter targetClass = Spy(JWTTokenValidatorFilter)
    def request = new MockHttpServletRequest()
    def response = new MockHttpServletResponse()
    def filterChain = Spy(FilterChain) {
        doFilter(request, response) >> {}
    }

    def "If no Bearer token, pass to next filterChain"() {
        when:
        targetClass.doFilterInternal(request, response, filterChain)
        then:
        1 * filterChain.doFilter(request, response) >> {}
    }

    def "If invalid token, throw 401"() {
        given:
        request.addHeader("Authorization", "Bearer invalidToken")
        when:
        targetClass.doFilterInternal(request, response, filterChain)
        then:
        response.status == 401
        0 * filterChain.doFilter(request, response) >> {}
    }

    def "If valid token, pass to next filterChain"() {
        given:
        request.addHeader("Authorization", "Bearer validToken")
        targetClass.parseToken(_) >> Mock(Claims)
        when:
        targetClass.doFilterInternal(request, response, filterChain)
        then:
        1 * filterChain.doFilter(request, response) >> {}
    }
}