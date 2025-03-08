package io.github.lujian213.eggfund.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import spock.lang.Specification

@SpringBootTest(classes = SecurityConfig)
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
        userDetailsService.loadUserByUsername("user") != null
    }


}