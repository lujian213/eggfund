package io.github.lujian213.eggfund.utils

import spock.lang.Specification

class LocalDateUtilSpec extends Specification {
    def "parse"() {
        when:
        def result = LocalDateUtil.parse(date)
        def str = (result == null ? null : result.format(Constants.DATE_FORMAT))
        then:
        str == value
        where:
        date         | value
        "2025-01-01" | "2025-01-01"
        null         | null
    }
}