package io.github.lujian213.eggfund.utils

import spock.lang.Specification

class CommonUtilSpec extends Specification {
    def "isZero"() {
        expect:
        CommonUtil.isZero(value, delta) == result
        where:
        value | delta | result
        0.1   | 0.01  | false
        0.01  | 0.01  | true
        0.001 | 0.01  | true
    }
}