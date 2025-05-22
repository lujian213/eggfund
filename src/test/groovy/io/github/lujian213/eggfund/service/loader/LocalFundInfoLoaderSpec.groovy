package io.github.lujian213.eggfund.service.loader

import io.github.lujian213.eggfund.exception.EggFundException
import io.github.lujian213.eggfund.model.FundInfo
import io.github.lujian213.eggfund.model.FundRTValue
import io.github.lujian213.eggfund.model.FundValue
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import java.time.LocalDate

class LocalFundInfoLoaderSpec extends Specification {
    def "loadFund"() {
        given:
        def loader = Spy(LocalFundInfoLoader) {
            extractFundName(_) >> "fund1"
        }
        def restTemplate = Mock(RestTemplate) {
            getForEntity(_, _) >> new ResponseEntity("content", HttpStatus.OK)
        }
        loader.setRestTemplate(restTemplate)
        when:
        def fundInfo = new FundInfo(id: "1001")
        loader.loadFund(fundInfo)
        then:
        with(fundInfo) {
            id == "1001"
            name == "fund1"
            type == FundInfo.FundType.LOCAL_FUND
        }
    }

    def "loadFund with non-ok status"() {
        given:
        def loader = new LocalFundInfoLoader()
        def restTemplate = Mock(RestTemplate) {
            getForEntity(_, _) >> new ResponseEntity("content", HttpStatus.NOT_FOUND)
        }
        loader.setRestTemplate(restTemplate)
        when:
        loader.loadFund(new FundInfo(id: "1001"))
        then:
        thrown(EggFundException)
    }

    def "loadFund with bad content"() {
        def loader = Spy(LocalFundInfoLoader) {
            extractFundName(_) >> null
        }
        def restTemplate = Mock(RestTemplate) {
            getForEntity(_, _) >> new ResponseEntity("content", HttpStatus.OK)
        }
        loader.setRestTemplate(restTemplate)
        when:
        loader.loadFund(new FundInfo(id: "1001"))
        then:
        thrown(EggFundException)
    }

    def "loadFund with exception in request"() {
        given:
        def loader = new LocalFundInfoLoader()
        def restTemplate = Mock(RestTemplate) {
            getForEntity(_, _) >> { throw new RestClientException("") }
        }
        loader.setRestTemplate(restTemplate)

        when:
        loader.loadFund(new FundInfo(id: "1001"))
        then:
        thrown(EggFundException)
    }

    def "extractFundName" () {
        given:
        def loader = new LocalFundInfoLoader()
        def content = """\
/** * 测试数据 * @type {arry} *//*2025-03-03 21:28:39*/var ishb=false;/*基金或股票信息*/var fS_name = "华夏国证半导体芯片ETF联接C";var fS_code = "008888";/*原费率*/var fund_sourceRate="0.00";/*现费率*/var fund_Rate="0.00";/*最小申购金额*/var fund_minsg="10";/*基金持仓股票代码*/var stockCodes=[];/*基金持仓债券代码*/var zqCodes = "0197491,0197491,0196311,0196311,0197061";/*基金持仓股票代码(新市场号)*/var stockCodesNew =[];/*基金持仓债券代码（新市场号）\
        """
        expect:
        loader.extractFundName(content) == "华夏国证半导体芯片ETF联接C"
    }

    def "extractFundName with bad content"() {
        given:
        def loader = new LocalFundInfoLoader()
        def content = "some bad content"
        expect:
        !loader.extractFundName(content)
    }

    def "loadFundValue"() {
        given:
        def loader = Spy(LocalFundInfoLoader) {
            1 * extractFundValue(_) >> [new FundValue("2024-11-11", 1.0d, 1.0d, 0.01d),
                                        new FundValue("2024-11-12", 1.1d, 1.1d, 0.02d)]
        }
        def content = "some content"
        def restTemplate = Mock(RestTemplate) {
            1 * getForEntity(_, _) >> new ResponseEntity<>(content, HttpStatus.OK)
        }
        loader.setRestTemplate(restTemplate)
        when:
        def result = loader.loadFundValue("1000", LocalDate.now(), LocalDate.now().plusDays(1))
        then:
        with(result) {
            size() == 2
        }
    }

    def "loadFundValue with non-ok status"() {
        given:
        def loader = new LocalFundInfoLoader()
        def content = "some content"
        def restTemplate = Mock(RestTemplate) {
            1 * getForEntity(_, _) >> new ResponseEntity<>(content, HttpStatus.NOT_FOUND)
        }
        loader.setRestTemplate(restTemplate)
        when:
        loader.loadFundValue("1000", LocalDate.now(), LocalDate.now().plusDays(1))
        then:
        thrown(EggFundException)
    }

    def "extractFundValue"() {
        def loader = new LocalFundInfoLoader()
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
        def fundValueList = loader.extractFundValue(content)
        then:
        fundValueList.size() == 30
    }

    def "getFundRTValue"() {
        given:
        def loader = Spy(LocalFundInfoLoader) {
            1 * extractFundRTValue(_, _) >> new FundRTValue("2024-11-11 11:11", 1.0d, 0.01d)
        }
        def content = "some content"
        def restTemplate = Mock(RestTemplate) {
            1 * getForEntity(_, _) >> new ResponseEntity<>(content, HttpStatus.OK)
        }
        loader.setRestTemplate(restTemplate)
        when:
        def result = loader.getFundRTValue("1000")
        then:
        with(result) {
            time == "2024-11-11 11:11"
            unitValue == 1.0d
            increaseRate == 0.01d
        }
    }

    def "getFundRTValue with non-ok status"() {
        given:
        def loader = new LocalFundInfoLoader()
        def content = "some content"
        def restTemplate = Mock(RestTemplate) {
            1 * getForEntity(_, _) >> new ResponseEntity<>(content, HttpStatus.NOT_FOUND)
        }
        loader.setRestTemplate(restTemplate)
        expect:
        !loader.getFundRTValue("1000")
    }

    def "getFundRTValue with bad payload"() {
        given:
        def loader = Spy(LocalFundInfoLoader) {
            1 * extractFundRTValue(_, _) >> { throw new IOException() }
        }
        def content = "some content"
        def restTemplate = Mock(RestTemplate) {
            1 * getForEntity(_, _) >> new ResponseEntity<>(content, HttpStatus.OK)
        }
        loader.setRestTemplate(restTemplate)
        expect:
        !loader.getFundRTValue("1000")
    }

    def "extractFundRTValue"() {
        def loader = new LocalFundInfoLoader()
        def content = """\
jsonpgz({"fundcode":"008888","name":"华夏国证半导体芯片ETF联接C","jzrq":"2025-03-03","dwjz":"1.1447","gsz":"1.1660","gszzl":"1.86","gztime":"2025-03-04 15:00"});\
"""
        when:
        def fundRTValue = loader.extractFundRTValue(content, "008888")
        then:
        with(fundRTValue) {
            time == "2025-03-04 15:00"
            unitValue == 1.1660d
            increaseRate == 1.86 / 100d
        }
    }

    def "extractFundRTValue with exception"() {
        given:
        def loader = new LocalFundInfoLoader()
        def content = 'some bad payload'
        expect:
        !loader.extractFundRTValue(content, "217022")
    }

}
