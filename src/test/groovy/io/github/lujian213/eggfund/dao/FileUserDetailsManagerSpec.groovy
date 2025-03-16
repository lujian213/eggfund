package io.github.lujian213.eggfund.dao

import io.github.lujian213.eggfund.model.Investor
import org.springframework.security.core.userdetails.UsernameNotFoundException
import io.github.lujian213.eggfund.utils.Constants;
import spock.lang.Specification


class FileUserDetailsManagerSpec extends Specification {

    FileUserDetailsManager targetClass

    def setup() {
        targetClass = new FileUserDetailsManager(new File("dummyFolder"))
    }

    def cleanup() {
        new File("dummyFolder").deleteDir()
    }

    def "has built-in admin user"() {
        expect:
        targetClass.users != null
        targetClass.loadUserByUsername("admin") != null
    }

    def "add investor to file and can effective immediately"() {
        given:
        def investor = new Investor("Alex", "Alex Smith", "icon1")
        when:
        targetClass.saveUsers(List.of(investor))
        then:
        new File("dummyFolder", Constants.USERS_FILE_NAME).isFile()
        targetClass.loadUserByUsername("Alex") != null
        targetClass.loadUserByUsername("admin") != null
    }

    def "throw UsernameNotFoundException when user not found"() {
        when:
        targetClass.loadUserByUsername("notExist")
        then:
        thrown(UsernameNotFoundException)
    }

}