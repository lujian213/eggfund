package io.github.lujian213.eggfund.model

import io.github.lujian213.eggfund.utils.Constants
import spock.lang.Specification
import java.time.LocalDate

class DateRangeSpec extends Specification {

    def "InRange"() {
        given:
        def date = LocalDate.parse(day, Constants.DATE_FORMAT)
        def date1 = day1 == null ? null : LocalDate.parse(day1, Constants.DATE_FORMAT)
        def date2 = day2 == null ? null : LocalDate.parse(day2, Constants.DATE_FORMAT)
        def range = new DateRange(date1, date2)
        expect:
        range.inRange(date) == result
        where:
        day1         | day          | day2         | result
        "2025-01-01" | "2025-01-02" | "2025-01-03" | true
        "2025-01-01" | "2025-01-01" | "2025-01-03" | true
        "2025-01-01" | "2025-01-03" | "2025-01-03" | true
        "2025-01-01" | "2024-12-31" | "2025-01-03" | false
        "2025-01-01" | "2025-01-04" | "2025-01-03" | false
        null         | "2024-12-31" | "2025-01-03" | true
        "2025-01-01" | "2025-01-04" | null         | true
    }

}