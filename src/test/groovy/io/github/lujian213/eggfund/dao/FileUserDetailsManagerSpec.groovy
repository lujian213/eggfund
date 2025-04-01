package io.github.lujian213.eggfund.dao

import io.github.lujian213.eggfund.exception.EggFundException
import io.github.lujian213.eggfund.service.InvestService
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UsernameNotFoundException
import spock.lang.Specification

class FileUserDetailsManagerSpec extends Specification {

    public static final String ADMIN = "admin";

    FileUserDetailsManager targetClass
    InvestService investService;

    def setup() {
        targetClass = new FileUserDetailsManager()
        investService = Mock(InvestService) {
            getUser(ADMIN) >> new User(ADMIN, ADMIN, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
            getUser(_) >> new EggFundException()
        }
        targetClass.setInvestService(investService)
    }

    def "read user from InvestService"() {
        expect:
        targetClass.loadUserByUsername(ADMIN) != null
        targetClass.loadUserByUsername(ADMIN).authorities == Set.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
    }

    def "throw UsernameNotFoundException when user not found"() {
        when:
        targetClass.loadUserByUsername("notExist")
        then:
        thrown(UsernameNotFoundException)
    }
}