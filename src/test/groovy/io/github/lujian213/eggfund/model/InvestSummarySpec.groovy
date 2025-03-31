package io.github.lujian213.eggfund.model

import io.github.lujian213.eggfund.utils.CommonUtil
import io.github.lujian213.eggfund.utils.Constants
import spock.lang.Specification
import java.time.LocalDate

class InvestSummarySpec extends Specification {

    def "getAverageUnitValue with non zero quota"() {
        given:
        def investSummary = Spy(InvestSummary)
        investSummary.getNetAmt() >> 1000
        investSummary.getNetQuota() >> 100
        when:
        def result = investSummary.getAverageUnitValue()
        then:
        result > 9.9
        result - 10 < 0.0001
    }

    def "getAverageUnitValue with zero quota"() {
        given:
        def investSummary = Spy(InvestSummary) {
            getNetAmt() >> 1000d
            getNetQuota() >> 0.001d
        }
        when:
        def result = investSummary.getAverageUnitValue()
        then:
        result == 0
    }

    def "prepareFundValues"() {
        given:
        def investSummary = new InvestSummary()
        def originFundValues = new ArrayList<FundValue>()
        originFundValues << new FundValue("2025-01-01", 1.01, 1.01, 0.01)
        originFundValues << new FundValue("2025-01-02", 1.02, 1.02, 0.02)
        def result = investSummary.prepareFundValues(originFundValues, LocalDate.parse(endDate, Constants.DATE_FORMAT), new FundRTValue("$rtValueDate 15:00", -1, givenRaiseRate))
        expect:
        with(investSummary) {
            result.size() == size
            lastFundDate == _lastFundDate
            raiseRate == _raiseRate as double
            Math.abs(estPrice - _estPrice) < 0.00001
        }
        where:
        endDate      | rtValueDate  | givenRaiseRate | size | _lastFundDate | _raiseRate | _estPrice
        "2025-01-02" | "2025-01-02" | 0.05           | 2    | "2025-01-01"  | 0.02       | 1.02
        "2025-01-04" | "2025-01-04" | 0.05           | 3    | "2025-01-02"  | 0.05       | 1.02 * (1 + 0.05)
        "2025-01-04" | "2025-01-02" | 0.05           | 2    | "2025-01-01"  | 0.02       | 1.02

    }

    def "sample case"() {
        when:
        def values = new ArrayList<FundValue>()
        def invests = new ArrayList<Invest>()
        values << new FundValue("2024-10-09", 1.3011, 1, -0.058)
        values << new FundValue("2024-10-10", 1.3289, 1, 0.0214)
        values << new FundValue("2024-10-11", 1.3012, 1, -0.0208)
        values << new FundValue("2024-10-14", 1.321, 1, 0.0152)
        values << new FundValue("2024-10-15", 1.2898, 1, -0.0236)
        values << new FundValue("2024-10-16", 1.2873, 1, -0.0019)
        values << new FundValue("2024-10-17", 1.2691, 1, -0.0141)
        values << new FundValue("2024-10-18", 1.3039, 1, 0.0274)
        values << new FundValue("2024-10-21", 1.3031, 1, -0.0006)
        values << new FundValue("2024-10-22", 1.3085, 1, 0.0041)
        values << new FundValue("2024-10-23", 1.3157, 1, 0.0055)
        values << new FundValue("2024-10-24", 1.3029, 1, -0.0097)
        values << new FundValue("2024-10-25", 1.3042, 1, 0.0010)
        values << new FundValue("2024-10-28", 1.3055, 1, 0.0010)
        values << new FundValue("2024-10-29", 1.2973, 1, -0.0063)
        values << new FundValue("2024-10-30", 1.2822, 1, -0.0116)
        values << new FundValue("2024-10-31", 1.2814, 1, -0.0006)
        values << new FundValue("2024-11-01", 1.2900, 1, 0.0067)

        invests << new Invest(type: Invest.TYPE_TRADE, id: "dummy", code: "001548", day: "2024-10-09", share: 76781.26, unitPrice: 1.3011, fee: 99.9)
        invests << new Invest(type: Invest.TYPE_TRADE, id: "dummy", code: "001548", day: "2024-10-11", share: 15355.07, unitPrice: 1.3012, fee: 19.98)
        invests << new Invest(type: Invest.TYPE_TRADE, id: "dummy", code: "001548", day: "2024-10-15", share: 23236.18, unitPrice: 1.2898, fee: 29.97)
        invests << new Invest(type: Invest.TYPE_TRADE, id: "dummy", code: "001548", day: "2024-10-23", share: -20000, unitPrice: 1.3157, fee: 78.94)
        invests << new Invest(type: Invest.TYPE_TRADE, id: "dummy", code: "001548", day: "2024-10-24", share: 11501.27, unitPrice: 1.3029, fee: 14.99)
        invests << new Invest(type: Invest.TYPE_TRADE, id: "dummy", code: "001548", day: "2024-10-29", share: 19251.54, unitPrice: 1.2973, fee: 24.98)
        invests << new Invest(type: Invest.TYPE_TRADE, id: "dummy", code: "001548", day: "2024-10-30", share: 19478.26, unitPrice: 1.2822, fee: 24.98)

        def inst = new InvestSummary(new FundInfo("001548", "dummy name"), values, invests, new FundRTValue("2024-11-01 15:00", -1, 0.0087), LocalDate.parse("2024-11-01", Constants.DATE_FORMAT))

        then:
        with(inst) {
            Math.abs(getTotalLongAmt() - 215000) < 0.01
            Math.abs(getTotalShortAmt() - 26235.06) < 0.001
            Math.abs(getTotalLongQuota() - 165603.58) < 0.001
            Math.abs(getTotalShortQuota() - 20000) < 0.001
            Math.abs(getNetAmt() - (-188764.931)) < 0.001
            Math.abs(getEstPrice() - 1.29) < 0.001
            Math.abs(getLastUnitValue() - 1.2814) < 0.0001
            Math.abs(getPredictedValue() - 187828.6182) < 0.0001
            Math.abs(getEarning() - (-936.3137)) < 0.0001
            Math.abs(getEarningRate() - (-0.00435)) < 0.0001
            Math.abs(getAveragePrice() - 1.2982) < 0.0001
            Math.abs(getTotalFee() - 293.74) < 0.0001
            Math.abs(getRaiseRate() - 0.0067) < 0.0001
            CommonUtil.isZero(grossEarning - earning - totalFee, 0.001)
            CommonUtil.isZero(grossEarningRate - grossEarning/totalLongAmt, 0.001)
            clearanceMap.size() == 3
            fundId == "001548"
            estPriceTable.size() == 8
            estPriceTableItems.size() == 8
            totalDividendAmt == 0
            clearanceMap['FIFO']
            clearanceMap['LIFO']
            clearanceMap['HPFO']
        }
    }
}