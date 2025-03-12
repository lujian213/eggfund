package io.github.lujian213.eggfund.model

import io.github.lujian213.eggfund.utils.CommonUtil
import spock.lang.Specification


class InvestSummaryItemSpec extends Specification {

    def "InvestSummaryItem with invest and price"() {
        given:
        def invest = new Invest(Invest.TYPE_TRADE, "001-001", "123456", "2025-01-01", quota, 1.3, 10)
        with(invest) {
            setUserIndex(2)
            setEnabled(false)
            setBatch(3)
            setComments("some comments")
        }
        when:
        def item = new InvestSummaryItem(invest, 1.4)
        then:
        with(item) {
            getDay() == "2025-01-01"
            getType() == Invest.TYPE_TRADE
            getPrice() == 1.3d
            getQuota() == quota
            getFee() == 10
            getBatch() == 3
            getInvestAmt() == invest.getAmount()
            Math.abs(getVar() - _var as double) < 0.0001
            Math.abs(getEarning() - earning as double) < 0.0001
            getIndex() == 2
            !isEnabled()
            getBatch() == 3
            getComments() == "some comments"
            getIncreaseRate() == 0
            getInvestId() == "001-001"
            Math.abs(getPrice_2pct() - 1.3 * 1.02) < 0.0001
            Math.abs(getPrice_minus2pct() - 1.3 * 0.98) < 0.0001
        }
        where:
        quota | _var   | earning
        1000  | 0.0769 | 100.7692
        -1000 | 0      | 0

    }

    def "InvestSummaryItem with fundValue, non-null invest and price"() {
        given:
        def invest = new Invest(Invest.TYPE_TRADE, "001-001", "123456", "2025-01-01", quota, -1, 10)
        with(invest) {
            setUserIndex(2)
            setEnabled(false)
            setBatch(3)
            setComments("some comments")
        }
        def fundValue = new FundValue("2025-01-01", 1.3, 1.3, 0.001)
        when:
        def item = new InvestSummaryItem(fundValue, invest, 1.4)
        then:
        with(item) {
            getDay() == "2025-01-01"
            getType() == Invest.TYPE_TRADE
            getPrice() == 1.3d
            getQuota() == quota
            getFee() == 10
            getBatch() == 3
            getInvestAmt() == invest.amount(1.3)
            Math.abs(getVar() - _var as double) < 0.0001
            Math.abs(getEarning() - earning as double) < 0.0001
            getIndex() == 2
            !isEnabled()
            getBatch() == 3
            getComments() == "some comments"
            getIncreaseRate() == 0.001d
            getInvestId() == "001-001"
            Math.abs(getPrice_2pct() - 1.3 * 1.02) < 0.0001
            Math.abs(getPrice_minus2pct() - 1.3 * 0.98) < 0.0001
        }
        where:
        quota | _var   | earning
        1000  | 0.0769 | 100.7692
        -1000 | 0      | 0
    }

    def "InvestSummaryItem with fundValue, null invest and price"() {
        given:
        def fundValue = new FundValue("2025-01-01", 1.3, 1.3, 0.001)
        when:
        def item = new InvestSummaryItem(fundValue, null, 1.4)
        then:
        with(item) {
            getDay() == "2025-01-01"
            getType() == null
            getPrice() == 1.3d
            getQuota() == 0
            getFee() == 0
            getBatch() == 0
            getInvestAmt() == 0
            getVar() == 0
            getEarning() == 0
            getIndex() == 0
            isEnabled()
            getBatch() == 0
            getComments() == null
            getIncreaseRate() == 0.001d
            getInvestId() == null
            Math.abs(getPrice_2pct() - 1.3 * 1.02) < 0.0001
            Math.abs(getPrice_minus2pct() - 1.3 * 0.98) < 0.0001
        }
    }

    def "liquidate"() {
        when:
        def item1 = new InvestSummaryItem(new Invest(id: "buy1", share: buyShare, unitPrice: 10, type: Invest.TYPE_TRADE, batch: 0, day: "2025-02-01"), 12)
        def item3 = new InvestSummaryItem(new Invest(id: "sell1", share: sellShare, unitPrice: 10.3, type: Invest.TYPE_TRADE, batch: 0, day: "2025-02-04"), 12)
        item1.liquidate(item3)
        then:
        with(item1) {
            quota == buyQuota
            liquidatedQuota == buyLiquidatedQuota
            enabled == buyEnabled
        }
        with(item3) {
            quota == sellQuota
            liquidatedQuota == sellLiquidatedQuota
            enabled == sellEnabled
        }

        where:
        buyShare | sellShare | buyQuota | buyLiquidatedQuota | buyEnabled | sellQuota | sellLiquidatedQuota | sellEnabled
        1000     | -1400     | 0        | 1000               | false      | -400      | -1000               | true
        1500     | -1400     | 100      | 1400               | true       | 0         | -1400               | false
        1400     | -1400     | 0        | 1400               | false      | 0         | -1400               | false
    }

    def "liquidate with exception"() {
        when:
        def item1 = new InvestSummaryItem(new Invest(id: "buy1", share: 1500, unitPrice: 10, type: Invest.TYPE_TRADE, batch: 0, day: "2025-02-01"), 12)
        def item3 = new InvestSummaryItem(new Invest(id: "sell1", share: -1400, unitPrice: 10.3, type: Invest.TYPE_TRADE, batch: 0, day: "2025-02-04"), 12)
        item3.liquidate(item1)
        then:
        thrown(IllegalArgumentException)
    }

    def "liquidate on non-trade item"() {
        when:
        def item1 = new InvestSummaryItem(new FundValue(day: "2025-02-02", unitValue: 10.1), null, 12)
        def item3 = new InvestSummaryItem(new Invest(id: "sell1", share: -1400, unitPrice: 10.3, type: Invest.TYPE_TRADE, batch: 0, day: "2025-02-04"), 12)
        item1.liquidate(item3)
        then:
        with(item1) {
            quota == 0
            liquidatedQuota == 0
            enabled
        }
        with(item3) {
            quota == -1400
            liquidatedQuota == 0
            enabled
        }
    }
}