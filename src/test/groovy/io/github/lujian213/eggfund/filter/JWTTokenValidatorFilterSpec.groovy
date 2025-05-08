package io.github.lujian213.eggfund.filter

import io.github.lujian213.eggfund.utils.Constants
import jakarta.servlet.FilterChain
import org.springframework.core.env.Environment
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import spock.lang.Specification


class JWTTokenValidatorFilterSpec extends Specification {

    JWTTokenValidatorFilter targetClass = Spy(JWTTokenValidatorFilter) {
        getEnvironment() >> Mock(Environment) {
            getProperty(Constants.JWT_SECRET_KEY, Constants.JWT_SECRET_DEFAULT_VALUE) >> "mock-secret-mock-secret-mock-secret"
        }
    }
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
        request.addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJFZ2dGdW5kIiwic3ViIjoiSldUIFRva2VuIiwidXNlcm5hbWUiOiIxMjMiLCJhdXRob3JpdGllcyI6IlJPTEVfVVNFUiIsImlhdCI6MTc0NjcxOTc2OSwiZXhwIjoxNzQ2ODA2MTY5fQ.yD69MNySGexG36I8AwJ_HvNX9xRBtjF7EaqvOMHuWSE")
        when:
        targetClass.doFilterInternal(request, response, filterChain)
        then:
        1 * filterChain.doFilter(request, response) >> {}
    }
}