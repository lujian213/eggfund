package io.github.lujian213.eggfund.model


import spock.lang.Specification

class InvestSpec extends Specification {

    def "Amount with input price"() {
        def invest = new Invest()
        expect:
        invest.setShare(share as double).setUnitPrice(unitPrice).setFee(fee).amount(amountInput as double) - amountOutput <= delta
        where:
        share    | unitPrice | fee   | amountInput | amountOutput | delta
        76781.26 | 1.3011    | 99.91 | -1          | -100000.00   | 0.01
        76781.26 | 1.3011    | 199.9 | 1.3011      | -100008.00   | 0.01
        -20000   | 1.3157    | 78.94 | -1          | 26235.06     | 0.01
    }

    def "getAmount"() {
        def invest = new Invest()
        expect:
        invest.setShare(share as double).setUnitPrice(unitPrice).setFee(fee).getAmount() - amountOutput <= delta
        where:
        share    | unitPrice | fee   | amountOutput | delta
        76781.26 | 1.3011    | 99.9  | -100000.00   | 0.01
        -20000   | 1.3157    | 78.94 | 26235.06     | 0.01
    }

    def "isMisMatchAlert"() {
        def invest = new Invest()
        invest.setShare(share as double).setUnitPrice(unitPrice).setFee(fee).setTotalSpend(totalSpend)
        expect:
        invest.isMisMatchAlert() == misMatchAlert
        where:
        share    | unitPrice | fee   | totalSpend | misMatchAlert
        76781.26 | 1.3011    | 99.9  | -100000.01   | false
        -20000   | 1.3157    | 78.94 | 26234.06     | true
    }

    def "Invest construct with Invest"() {
        given:
        def anotherInvest = new Invest(type: "trade", id: "001-001", code: "123456", day: "2025-01-01", share: 1000, unitPrice: 1.3, fee: 10)
        when:
        def targetInvest = new Invest(anotherInvest)

        with(targetInvest) {

            setUserIndex(2)

            setEnabled(false)

            setBatch(3)

            setComments("some comments")
        }

        then:

        with(targetInvest)

                {
                    type == "trade"
                    id == "001-001"
                    code == "123456"
                    day == "2025-01-01"
                    share == 1000
                    unitPrice == 1.3d
                    fee == 10
                    userIndex == 2
                    !enabled
                    batch == 3
                    comments == "some comments"
                }
    }

    def "Compare"() {
        given:
        def invest1 = new Invest().setDay(day1).setUserIndex(index1)
        def invest2 = new Invest().setDay(day2).setUserIndex(index2)
        expect:
        Invest.compare(invest1, invest2) == result
        where:
        day1         | index1 | day2         | index2 | result
        "2025-01-01" | 0      | "2025-01-01" | 0      | 0
        "2025-01-01" | 0      | "2025-01-01" | 1      | -1
        "2025-01-01" | 1      | "2025-01-01" | 0      | 1
        "2025-01-01" | 1      | "2025-01-02" | 0      | -1
        "2025-01-02" | 0      | "2025-01-01" | 1      | 1
    }

}