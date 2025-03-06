package io.github.lujian213.eggfund.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Import
import spock.lang.Specification

@SpringBootTest(classes = FilterConfig)
@Import(HostNameFilter)
class FilterConfigSpec extends Specification {
    @Autowired
    FilterRegistrationBean filterRegistrationBean
    @Autowired
    HostNameFilter hostNameFilter

    def "bean registration"() {
        expect:
        filterRegistrationBean
        filterRegistrationBean.getFilter() == hostNameFilter
    }

}