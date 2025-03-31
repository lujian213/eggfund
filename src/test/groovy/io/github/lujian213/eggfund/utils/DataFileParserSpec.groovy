package io.github.lujian213.eggfund.utils

import io.github.lujian213.eggfund.model.Invest
import spock.lang.Specification

class DataFileParserSpec extends Specification {
    def "ParseInvestFile with csv"() {
        given:
        def dataFileParser = new DataFileParser()

        when:
        List<Invest> result
        new FileInputStream("test/data.csv").withCloseable {
            result = dataFileParser.parseInvestFile(it, "data.csv")
        }

        then:
        result.size() == 2
        with (result[0]) {
            day == "2025-02-28"
            type == "trade"
            id == null
            code == "159740"
            share == 5000d
            unitPrice == 0.749d
            fee == 1.12d
            tax == 0.000d
            userIndex == 0
            enabled
            batch == 0
            comments == null
        }
    }

    def "ParseInvestFile with json"() {
        given:
        def dataFileParser = new DataFileParser()

        when:
        List<Invest> result
        new FileInputStream("test/invests.json").withCloseable {
            result = dataFileParser.parseInvestFile(it, "invests.json")
        }

        then:
        result.size() == 6
        with (result[0]) {
            day == "2025-02-24"
            type == "trade"
            id == "125-270023-3497baca-8c8f-48fb-b0e8-d7ac8437beeb"
            code == "270023"
            share == 503.98d
            unitPrice == 3.9621d
            fee == 3.19d
            userIndex == 22
            enabled
            batch == 0
            comments == ""
            amount == -2000.009158d
        }
    }
}
