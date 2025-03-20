package io.github.lujian213.eggfund.config

import io.github.lujian213.eggfund.dao.FileUserDetailsManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.servlet.handler.HandlerMappingIntrospector
import spock.lang.Specification

@SpringBootTest(classes = [SecurityConfig, FileUserDetailsManager, TestMvcConfig])
class SecurityConfigSpec extends Specification {

    @Autowired
    SecurityFilterChain defaultSecurityFilterChain
    @Autowired
    UserDetailsService userDetailsService
    @Autowired
    PasswordEncoder passwordEncoder
    @Autowired
    RoleHierarchy roleHierarchy

    def "bean registration"() {
        expect:
        defaultSecurityFilterChain
        userDetailsService
        passwordEncoder
        roleHierarchy
        userDetailsService.loadUserByUsername("admin") != null
    }

}

@TestConfiguration
class TestMvcConfig {
    @Bean
    HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
        return new HandlerMappingIntrospector();
    }
}