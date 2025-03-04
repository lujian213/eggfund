package io.github.lujian213.eggfund.monitoring

import org.springframework.boot.actuate.info.Info
import spock.lang.Specification


class AppInfoContributorSpec extends Specification {

    def "contribute" () {
        given:
        def contributor = new AppInfoContributor()
        def builder = new Info.Builder()
        when:
        contributor.contribute(builder)
        def info = builder.build()
        then:
        with(info) {
            app == "eggfund"
            author =="Lu Jian"
            email == "lujian213@msn.com"
        }
    }

}