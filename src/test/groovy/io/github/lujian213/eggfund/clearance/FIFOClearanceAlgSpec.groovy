package io.github.lujian213.eggfund.clearance

import io.github.lujian213.eggfund.model.FundValue
import io.github.lujian213.eggfund.model.Invest
import io.github.lujian213.eggfund.model.InvestSummaryItem
import io.github.lujian213.eggfund.utils.CommonUtil
import spock.lang.Specification

class FIFOClearanceAlgSpec extends Specification {
    def alg = new FIFOClearanceAlg()

    def "Clear1"() {
        when:
        def itemList = []
        itemList << new InvestSummaryItem(new Invest(id: "buy1", share: 1000, unitPrice: 10, type: Invest.TYPE_TRADE, batch: 0, day: "2025-02-01"), 12)
        itemList << new InvestSummaryItem(new FundValue(day: "2025-02-02", unitValue: 10.1), null, 12)
        itemList << new InvestSummaryItem(new Invest(id: "buy2", share: 500, unitPrice: 10.2, type: Invest.TYPE_TRADE, batch: 0, day: "2025-02-03"), 12)
        itemList << new InvestSummaryItem(new Invest(id: "sell1", share: -800, unitPrice: 10.3, type: Invest.TYPE_TRADE, batch: 0, day: "2025-02-04"), 12)
        def result = alg.clear(itemList)

        then:
        result.size() == 4
        with (result[0]) {
            day == "2025-02-01"
            quota == 200
            liquidatedQuota == 800
            enabled
        }
        with (result[1]) {
            day == "2025-02-02"
            !type
        }
        with (result[2]) {
            day == "2025-02-03"
            quota == 500
            liquidatedQuota == 0
            enabled
        }
        with (result[3]) {
            day == "2025-02-04"
            quota == 0
            liquidatedQuota == -800
            !enabled
        }
    }

    def "Clear2"() {
        when:
        def itemList = []
        itemList << new InvestSummaryItem(new Invest(id: "buy1", share: 1000, unitPrice: 10, type: Invest.TYPE_TRADE, fee: 10, batch: 0, day: "2025-02-01"), 12)
        itemList << new InvestSummaryItem(new FundValue(day: "2025-02-02", unitValue: 10.1), null, 12)
        def invest = new Invest(id: "buy2", share: 500, unitPrice: 10.2, type: Invest.TYPE_TRADE, fee: 5, batch: 0, day: "2025-02-03")
        def item = new InvestSummaryItem(invest, 12)
        itemList << item
        itemList << new InvestSummaryItem(new Invest(id: "sell1", share: -1200, unitPrice: 10.3, type: Invest.TYPE_TRADE, fee: 12, batch: 0, day: "2025-02-04"), 12)
        def result = alg.clear(itemList)

        then:
        result.size() == 4
        with (result[0]) {
            day == "2025-02-01"
            quota == 0
            liquidatedQuota == 1000
            !enabled
        }
        with (result[1]) {
            day == "2025-02-02"
            !type
        }
        with (result[2]) {
            day == "2025-02-03"
            quota == 300
            liquidatedQuota == 200
            fee == 5
            CommonUtil.isZero(investAmt - invest.getAmount()/ 5 * 3, 0.001)
            CommonUtil.isZero(earning - item.getEarning()/ 5 * 3, 0.001)
            enabled
        }
        with (result[3]) {
            day == "2025-02-04"
            quota == 0
            liquidatedQuota == -1200
            !enabled
        }
    }

    def "Clear3"() {
        when:
        def itemList = []
        itemList << new InvestSummaryItem(new Invest(id: "buy1", share: 1000, unitPrice: 10, type: Invest.TYPE_TRADE, batch: 0, day: "2025-02-01"), 12)
        itemList << new InvestSummaryItem(new FundValue(day: "2025-02-02", unitValue: 10.1), null, 12)
        itemList << new InvestSummaryItem(new Invest(id: "buy2", share: 500, unitPrice: 10.2, type: Invest.TYPE_TRADE, batch: 0, day: "2025-02-03"), 12)
        itemList << new InvestSummaryItem(new Invest(id: "sell1", share: -1500, unitPrice: 10.3, type: Invest.TYPE_TRADE, batch: 0, day: "2025-02-04"), 12)
        def result = alg.clear(itemList)

        then:
        result.size() == 4
        with (result[0]) {
            day == "2025-02-01"
            quota == 0
            liquidatedQuota == 1000
            !enabled
        }
        with (result[1]) {
            day == "2025-02-02"
            !type
        }
        with (result[2]) {
            day == "2025-02-03"
            quota == 0
            liquidatedQuota == 500
            !enabled
        }
        with (result[3]) {
            day == "2025-02-04"
            quota == 0
            liquidatedQuota == -1500
            !enabled
        }
    }

    def "Clear4"() {
        when:
        def itemList = []
        itemList << new InvestSummaryItem(new Invest(id: "buy1", share: 1000, unitPrice: 10, type: Invest.TYPE_TRADE, batch: 0, day: "2025-02-01"), 12)
        itemList << new InvestSummaryItem(new FundValue(day: "2025-02-02", unitValue: 10.1), null, 12)
        itemList << new InvestSummaryItem(new Invest(id: "buy2", share: 500, unitPrice: 10.2, type: Invest.TYPE_TRADE, batch: 0, day: "2025-02-03"), 12)
        itemList << new InvestSummaryItem(new Invest(id: "sell1", share: -1600, unitPrice: 10.3, type: Invest.TYPE_TRADE, batch: 0, day: "2025-02-04"), 12)
        def result = alg.clear(itemList)

        then:
        result.size() == 4
        with (result[0]) {
            day == "2025-02-01"
            quota == 0
            liquidatedQuota == 1000
            !enabled
        }
        with (result[1]) {
            day == "2025-02-02"
            !type
        }
        with (result[2]) {
            day == "2025-02-03"
            quota == 0
            liquidatedQuota == 500
            !enabled
        }
        with (result[3]) {
            day == "2025-02-04"
            quota == -100
            liquidatedQuota == -1500
            !enabled
        }
    }

    def "Clear5"() {
        when:
        def itemList = []
        itemList << new InvestSummaryItem(new Invest(id: "buy0", share: 700, unitPrice: 9.9, type: Invest.TYPE_TRADE, batch: 0, day: "2025-01-31"), 12)
        itemList << new InvestSummaryItem(new Invest(id: "buy1", share: 1000, unitPrice: 10, type: Invest.TYPE_TRADE, batch: 1, day: "2025-02-01"), 12)
        itemList << new InvestSummaryItem(new FundValue(day: "2025-02-02", unitValue: 10.1), null, 12)
        itemList << new InvestSummaryItem(new Invest(id: "buy2", share: 500, unitPrice: 10.2, type: Invest.TYPE_TRADE, batch: 1, day: "2025-02-03"), 12)
        itemList << new InvestSummaryItem(new Invest(id: "sell1", share: -1600, unitPrice: 10.3, type: Invest.TYPE_TRADE, batch: 1, day: "2025-02-04"), 12)
        def result = alg.clear(itemList)

        then:
        result.size() == 5
        with (result[0]) {
            day == "2025-01-31"
            quota == 700
            liquidatedQuota == 0
            enabled
        }
        with (result[1]) {
            day == "2025-02-01"
            quota == 0
            liquidatedQuota == 1000
            !enabled
        }
        with (result[2]) {
            day == "2025-02-02"
            !type
        }
        with (result[3]) {
            day == "2025-02-03"
            quota == 0
            liquidatedQuota == 500
            !enabled
        }
        with (result[4]) {
            day == "2025-02-04"
            quota == -100
            liquidatedQuota == -1500
            !enabled
        }
    }

    def "Clear6"() {
        when:
        def itemList = []
        itemList << new InvestSummaryItem(new Invest(id: "buy1", share: 1000, unitPrice: 10, type: Invest.TYPE_TRADE, batch: 0, day: "2025-02-01"), 12)
        itemList << new InvestSummaryItem(new FundValue(day: "2025-02-02", unitValue: 10.1), null, 12)
        itemList << new InvestSummaryItem(new Invest(id: "buy2", share: 500, unitPrice: 10.2, type: Invest.TYPE_TRADE, batch: 0, day: "2025-02-03"), 12)
        itemList << new InvestSummaryItem(new Invest(id: "sell1", share: -1400, unitPrice: 10.3, type: Invest.TYPE_TRADE, batch: 0, day: "2025-02-04"), 12)
        itemList << new InvestSummaryItem(new Invest(id: "buy3", share: 900, unitPrice: 10.4, type: Invest.TYPE_TRADE, batch: 0, day: "2025-02-05"), 12)
        itemList << new InvestSummaryItem(new Invest(id: "sell2", share: -500, unitPrice: 10.5, type: Invest.TYPE_TRADE, batch: 0, day: "2025-02-06"), 12)
        def result = alg.clear(itemList)

        then:
        result.size() == 6
        with (result[0]) {
            day == "2025-02-01"
            quota == 0
            liquidatedQuota == 1000
            !enabled
        }
        with (result[1]) {
            day == "2025-02-02"
            !type
        }
        with (result[2]) {
            day == "2025-02-03"
            quota == 0
            liquidatedQuota == 500
            !enabled
        }
        with (result[3]) {
            day == "2025-02-04"
            quota == 0
            liquidatedQuota == -1400
            !enabled
        }
        with (result[4]) {
            day == "2025-02-05"
            quota == 500
            liquidatedQuota == 400
            enabled
        }
        with (result[5]) {
            day == "2025-02-06"
            quota == 0
            liquidatedQuota == -500
            !enabled
        }
    }
}
