package io.github.lujian213.eggfund.dao

import io.github.lujian213.eggfund.model.Investor
import io.github.lujian213.eggfund.service.InvestService
import io.github.lujian213.eggfund.utils.Constants
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException
import spock.lang.Specification

import static io.github.lujian213.eggfund.utils.Constants.ADMIN

class FileUserDetailsManagerSpec extends Specification {

    FileUserDetailsManager targetClass
    InvestService investService;

    def setup() {
        targetClass = new FileUserDetailsManager()
        investService = Mock(InvestService) {
            getAllInvestors() >> List.of(new Investor(ADMIN, ADMIN, null, Constants.DEFAULT_AABB, List.of(ADMIN)))
        }
        targetClass.setInvestService(investService)
    }

    def "read user from InvestService"() {
        expect:
        targetClass.loadUserByUsername("admin") != null
        targetClass.loadUserByUsername("ADMIN") != null
        targetClass.loadUserByUsername("ADMIN").authorities == Set.of(new SimpleGrantedAuthority("ROLE_" + ADMIN))
    }

    def "throw UsernameNotFoundException when user not found"() {
        when:
        targetClass.loadUserByUsername("notExist")
        then:
        thrown(UsernameNotFoundException)
    }
}