package io.github.lujian213.eggfund.service

import io.github.lujian213.eggfund.dao.FundDao
import io.github.lujian213.eggfund.exception.EggFundException
import io.github.lujian213.eggfund.model.DateRange
import io.github.lujian213.eggfund.model.FundInfo
import io.github.lujian213.eggfund.model.FundRTValue
import io.github.lujian213.eggfund.model.FundValue
import io.github.lujian213.eggfund.utils.Constants
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.time.LocalDate
import java.util.function.Supplier
import spock.lang.Specification

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
            loadFundValue(_ as String, _ as LocalDate, _ as LocalDate) >> new ArrayList<>()
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

    def "extractFundValue"() {
        def service = new AsyncActionService()
        def content = """\
var apidata={ content:"<table class='w782 comm lsjz'>\
<thead><tr><th class='first'>净值日期</th><th>单位净值</th><th>累计净值</th><th>日增长率</th><th>申购状态</th><th>赎回状态</th><th class='tor last'>分红送配</th></tr></thead>\
<tbody>\
<tr><td>2025-03-04</td><td class='tor bold'>1.1653</td><td class='tor bold'>1.1653</td><td class='tor bold red'>1.80%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-03-03</td><td class='tor bold'>1.1447</td><td class='tor bold'>1.1447</td><td class='tor bold grn'>-1.67%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-02-28</td><td class='tor bold'>1.1641</td><td class='tor bold'>1.1641</td><td class='tor bold grn'>-4.63%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-02-27</td><td class='tor bold'>1.2206</td><td class='tor bold'>1.2206</td><td class='tor bold grn'>-0.59%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-02-26</td><td class='tor bold'>1.2278</td><td class='tor bold'>1.2278</td><td class='tor bold red'>1.46%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-02-25</td><td class='tor bold'>1.2101</td><td class='tor bold'>1.2101</td><td class='tor bold grn'>-0.56%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-02-24</td><td class='tor bold'>1.2169</td><td class='tor bold'>1.2169</td><td class='tor bold red'>0.40%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-02-21</td><td class='tor bold'>1.2121</td><td class='tor bold'>1.2121</td><td class='tor bold red'>5.40%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-02-20</td><td class='tor bold'>1.1500</td><td class='tor bold'>1.1500</td><td class='tor bold grn'>-0.68%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-02-19</td><td class='tor bold'>1.1579</td><td class='tor bold'>1.1579</td><td class='tor bold red'>3.61%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-02-18</td><td class='tor bold'>1.1176</td><td class='tor bold'>1.1176</td><td class='tor bold grn'>-1.90%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-02-17</td><td class='tor bold'>1.1392</td><td class='tor bold'>1.1392</td><td class='tor bold red'>1.41%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-02-14</td><td class='tor bold'>1.1234</td><td class='tor bold'>1.1234</td><td class='tor bold red'>0.15%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-02-13</td><td class='tor bold'>1.1217</td><td class='tor bold'>1.1217</td><td class='tor bold grn'>-2.66%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-02-12</td><td class='tor bold'>1.1524</td><td class='tor bold'>1.1524</td><td class='tor bold red'>2.82%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-02-11</td><td class='tor bold'>1.1208</td><td class='tor bold'>1.1208</td><td class='tor bold grn'>-0.95%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-02-10</td><td class='tor bold'>1.1316</td><td class='tor bold'>1.1316</td><td class='tor bold red'>1.07%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-02-07</td><td class='tor bold'>1.1196</td><td class='tor bold'>1.1196</td><td class='tor bold red'>0.52%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-02-06</td><td class='tor bold'>1.1138</td><td class='tor bold'>1.1138</td><td class='tor bold red'>3.56%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-02-05</td><td class='tor bold'>1.0755</td><td class='tor bold'>1.0755</td><td class='tor bold red'>3.21%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-01-27</td><td class='tor bold'>1.0421</td><td class='tor bold'>1.0421</td><td class='tor bold grn'>-2.75%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-01-24</td><td class='tor bold'>1.0716</td><td class='tor bold'>1.0716</td><td class='tor bold red'>0.56%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-01-23</td><td class='tor bold'>1.0656</td><td class='tor bold'>1.0656</td><td class='tor bold grn'>-1.43%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-01-22</td><td class='tor bold'>1.0811</td><td class='tor bold'>1.0811</td><td class='tor bold grn'>-0.18%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-01-21</td><td class='tor bold'>1.0830</td><td class='tor bold'>1.0830</td><td class='tor bold red'>0.74%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-01-20</td><td class='tor bold'>1.0750</td><td class='tor bold'>1.0750</td><td class='tor bold red'>0.11%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-01-17</td><td class='tor bold'>1.0738</td><td class='tor bold'>1.0738</td><td class='tor bold red'>1.99%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-01-16</td><td class='tor bold'>1.0528</td><td class='tor bold'>1.0528</td><td class='tor bold grn'>-2.57%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-01-15</td><td class='tor bold'>1.0806</td><td class='tor bold'>1.0806</td><td class='tor bold grn'>-0.18%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
<tr><td>2025-01-14</td><td class='tor bold'>1.0826</td><td class='tor bold'>1.0826</td><td class='tor bold red'>3.26%</td><td>开放申购</td><td>开放赎回</td><td class='red unbold'></td></tr>\
</tbody></table>",records:1153,pages:39,curpage:1};\
        """
        when:
        def fundValueList = service.extractFundValue(content)
        then:
        fundValueList.size() == 30
    }

    def "extractFundRTValue"() {
        def service = new AsyncActionService()
        def content = """\
jsonpgz({"fundcode":"008888","name":"华夏国证半导体芯片ETF联接C","jzrq":"2025-03-03","dwjz":"1.1447","gsz":"1.1660","gszzl":"1.86","gztime":"2025-03-04 15:00"});\
"""
        when:
        def fundRTValue = service.extractFundRTValue(content, "008888")
        then:
        with(fundRTValue) {
            time == "2025-03-04 15:00"
            unitValue == 1.1660d
            increaseRate == 1.86 / 100d
        }
    }

    def "extractFundRTValue with exception"() {
        given:
        def service = new AsyncActionService()
        def content = 'some bad payload'
        expect:
        !service.extractFundRTValue(content, "217022")
    }


    def "getFundRTValueInBatch"() {
        given:
        def service = Spy(AsyncActionService) {
            1 * getFundRTValue("1000") >> new FundRTValue("2024-11-11 11:11", 1.0d, 0.01d)
            1 * getFundRTValue("1001") >> null
        }
        def registry = Mock(MeterRegistry) {
            timer(_, _) >> Mock(Timer) {
                record(_) >> { Supplier supplier -> supplier.get() }
            }
        }
        service.setMeterRegistry(registry)
        when:
        def result = service.getFundRTValueInBatch("1000", "1001")
        then:
        with(result.get()) {
            size() == 2
            it["1000"]
            !it["1001"]
        }
    }

    def "getFundRTValue"() {
        given:
        def service = Spy(AsyncActionService) {
            1 * extractFundRTValue(_, _) >> new FundRTValue("2024-11-11 11:11", 1.0d, 0.01d)
        }
        def content = "some content"
        def restTemplate = Mock(RestTemplate) {
            1 * getForEntity(_, _) >> new ResponseEntity<>(content, HttpStatus.OK)
        }
        service.setRestTemplate(restTemplate)
        when:
        def result = service.getFundRTValue("1000")
        then:
        with(result) {
            time == "2024-11-11 11:11"
            unitValue == 1.0d
            increaseRate == 0.01d
        }
    }

    def "getFundRTValue with non-ok status"() {
        given:
        def service = new AsyncActionService()
        def content = "some content"
        def restTemplate = Mock(RestTemplate) {
            1 * getForEntity(_, _) >> new ResponseEntity<>(content, HttpStatus.NOT_FOUND)
        }
        service.setRestTemplate(restTemplate)
        expect:
        !service.getFundRTValue("1000")
    }

    def "getFundRTValue with bad payload"() {
        given:
        def service = Spy(AsyncActionService) {
            1 * extractFundRTValue(_, _) >> { throw new IOException() }
        }
        def content = "some content"
        def restTemplate = Mock(RestTemplate) {
            1 * getForEntity(_, _) >> new ResponseEntity<>(content, HttpStatus.OK)
        }
        service.setRestTemplate(restTemplate)
        expect:
        !service.getFundRTValue("1000")
    }

    def "loadFundValue"() {
        given:
        def service = Spy(AsyncActionService) {
            1 * extractFundValue(_) >> [new FundValue("2024-11-11", 1.0d, 1.0d, 0.01d),
                                          new FundValue("2024-11-12", 1.1d, 1.1d, 0.02d)]
        }
        def content = "some content"
        def restTemplate = Mock(RestTemplate) {
            1 * getForEntity(_, _) >> new ResponseEntity<>(content, HttpStatus.OK)
        }
        service.setRestTemplate(restTemplate)
        when:
        def result = service.loadFundValue("1000", LocalDate.now(), LocalDate.now().plusDays(1))
        then:
        with(result) {
            size() == 2
        }
    }

    def "loadFundValue with non-ok status"() {
        given:
        def service = new AsyncActionService()
        def content = "some content"
        def restTemplate = Mock(RestTemplate) {
            1 * getForEntity(_, _) >> new ResponseEntity<>(content, HttpStatus.NOT_FOUND)
        }
        service.setRestTemplate(restTemplate)
        when:
        service.loadFundValue("1000", LocalDate.now(), LocalDate.now().plusDays(1))
        then:
        thrown(EggFundException)
    }
}