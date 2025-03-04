package io.github.lujian213.eggfund.monitoring

import org.springframework.boot.actuate.health.Status
import spock.lang.Specification

class ProviderHealthIndicatorSpec extends Specification {

    def "Health"() {
        given:
        def indicator = new ProviderHealthIndicator()
        expect:
        indicator.health().status == Status.UP
    }

}