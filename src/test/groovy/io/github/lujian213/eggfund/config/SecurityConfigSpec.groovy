package io.github.lujian213.eggfund.config

import io.github.lujian213.eggfund.dao.FileUserDetailsManager
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class SecurityConfigSpec extends Specification {

    @Autowired
    SecurityFilterChain defaultSecurityFilterChain
    @Autowired
    PasswordEncoder passwordEncoder
    @Autowired
    RoleHierarchy roleHierarchy
    @Autowired
    MockMvc mockMvc
    @SpringBean
    FileUserDetailsManager userDetailsManager = Mock(FileUserDetailsManager)

    def "bean registration"() {
        expect:
        defaultSecurityFilterChain
        passwordEncoder
        roleHierarchy
    }

    def "should allow swagger endpoints without auth"() {
        expect:
        mockMvc.perform(get(path)).andExpect(status().is(returnCode))

        where:
        path                     | returnCode
        "/swagger-ui.html"       | 302
        "/swagger-ui/index.html" | 200
        "/v3/api-docs"           | 200
        "/v3/api-docs/"          | 404
        "/v3/api-docs/123"       | 404
        "/some/v3/api-docs"      | 401
    }

    def "should not allow resources endpoints without auth"() {
        expect:
        mockMvc.perform(get(path)).andExpect(status().is(returnCode))

        where:
        path         | returnCode
        "/loginUser" | 401
        "/investor"  | 401
    }

    def "should allow resources endpoint with auth"() {
        given:
        def user = "user"
        def pass = "pass"
        userDetailsManager.loadUserByUsername(_) >>
                new User(user, passwordEncoder.encode(pass), List.of(new SimpleGrantedAuthority("ROLE_USER")))

        expect:
        mockMvc.perform(get(path).header("Authorization", getBasicAuthenticationHeader(user, pass)).header("accept", "application/json"))
                .andExpect(status().is(returnCode))

        where:
        path         | returnCode
//        "/loginUser" | 200
        "/investors" | 200
    }

    private static final String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }
}