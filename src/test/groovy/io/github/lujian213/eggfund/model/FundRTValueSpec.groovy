package io.github.lujian213.eggfund.model

import spock.lang.Specification

class FundRTValueSpec extends Specification {

    def "FundRTValue"() {
        given:
        def fundRTValue = new FundRTValue("2025-01-04 14:00", 1.01, 0.002)
        expect:
        with(fundRTValue) {
            getTime() == "2025-01-04 14:00"
            getDay() == "2025-01-04"
        }
    }

}