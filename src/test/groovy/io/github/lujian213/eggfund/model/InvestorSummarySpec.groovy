package io.github.lujian213.eggfund.model

import spock.lang.Specification

class InvestorSummarySpec extends Specification {

    def "sample case"() {
        when:
        def invest1 = Spy(InvestSummary) {
            getFundId() >> "012734"
            getTotalLongAmt() >> 4000
            getTotalShortAmt() >> 1635.80
            getNetAmt() >> -2364.19
            getPredictedValue() >> 2678.33
            getEarning() >> 314.14
            getTotalFee() >> 0
        }
        def invest2 = Spy(InvestSummary) {
            getFundId() >> "008888"
            getTotalLongAmt() >> 11000
            getTotalShortAmt() >> 8833.98
            getNetAmt() >> -2166.00
            getPredictedValue() >> 2713.67
            getEarning() >> 547.67
            getTotalFee() >> 7.03
        }
        def investorSummary = new InvestorSummary("user", List.of(invest1, invest2))

        then:
        with(investorSummary) {
            Math.abs(getTotalLongAmt() - 15000) < 0.01
            Math.abs(getTotalShortAmt() - 10469.78) < 0.001
            Math.abs(getNetAmt() - (-4530.19)) < 0.001
            Math.abs(getPredictedValue() - 5392) < 0.0001
            Math.abs(getEarning() - 861.81) < 0.0001
            Math.abs(getEarningRate() - (0.0574)) < 0.0001
            Math.abs(getTotalFee() - 7.03) < 0.0001
            Math.abs(getGrossEarning() - 868.84) < 0.0001
            Math.abs(getGrossEarningRate() - 0.0579) < 0.0001
        }
    }

}