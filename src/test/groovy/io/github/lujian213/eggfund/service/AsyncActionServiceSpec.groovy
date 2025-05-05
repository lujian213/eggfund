package io.github.lujian213.eggfund.service

import io.github.lujian213.eggfund.dao.FundDao
import io.github.lujian213.eggfund.model.DateRange
import io.github.lujian213.eggfund.model.FundInfo
import io.github.lujian213.eggfund.model.FundRTValue
import io.github.lujian213.eggfund.service.loader.FundInfoLoader
import io.github.lujian213.eggfund.utils.Constants
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import spock.lang.Specification

import java.time.LocalDate
import java.util.function.Supplier

class AsyncActionServiceSpec extends Specification {

    def "UpdateFundValues"() {
        given:
        def dao = Mock(FundDao)
        def registry = Mock(MeterRegistry) {
            timer(_, _) >> Mock(Timer) {
                record(_) >> { Supplier supplier -> supplier.get() }
            }
        }
        def listener = Mock(FundValueListener)
        def service = Spy(AsyncActionService) {
            getFundInfoLoader(_) >> Mock(FundInfoLoader) {
                loadFundValue(_ as String, _ as LocalDate, _ as LocalDate) >> new ArrayList<>()
            }
        }
        service.setFundDao(dao)
        service.setMeterRegistry(registry)
        def fundInfo = new FundInfo("1000", "abc")
        def from = LocalDate.parse("2024-11-11", Constants.DATE_FORMAT)
        def to = LocalDate.parse("2025-02-03", Constants.DATE_FORMAT)
        when:
        service.updateFundValues(fundInfo, new DateRange(from, to), listener)
        then:
        4 * dao.saveFundValues(_, _, _)
        4 * listener.onFundValueChange(_, _, _)
    }

    def "getFundRTValueInBatch"() {
        given:
        def service = Spy(AsyncActionService) {
            getFundInfoLoader(_) >> Mock(FundInfoLoader) {
                1 * getFundRTValue("1000") >> new FundRTValue("2024-11-11 11:11", 1.0d, 0.01d)
                1 * getFundRTValue("1001") >> null
            }
        }
        def registry = Mock(MeterRegistry) {
            timer(_, _) >> Mock(Timer) {
                record(_) >> { Supplier supplier -> supplier.get() }
            }
        }
        service.setMeterRegistry(registry)
        when:
        def result = service.getFundRTValueInBatch(new FundInfo(id: "1000"), new FundInfo(id: "1001"))
        then:
        with(result.get()) {
            size() == 2
            it["1000"]
            !it["1001"]
        }
    }
}