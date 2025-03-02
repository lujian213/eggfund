package io.github.lujian213.eggfund.utils

import spock.lang.Specification

class FileNameUtilSpec extends Specification {
    def "isValidFileName"() {
        expect:
        FileNameUtil.isValidFileName(fileName) == result
        where:
        fileName  | result
        "abc"     | true
        "abc.txt" | true
        "abc 1"   | false
        "abc<1"   | false
    }

    def "makeValidFileName"() {
        expect:
        FileNameUtil.makeValidFileName(fileName) == result
        where:
        fileName  | result
        "abc"     | "abc"
        "abc.txt" | "abc.txt"
        "abc 1"   | "abc_1"
        "abc<1"   | "abc_1"
    }
}