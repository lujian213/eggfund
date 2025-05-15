package io.github.lujian213.eggfund.service.loader

import io.github.lujian213.eggfund.exception.EggFundException
import io.github.lujian213.eggfund.model.FundInfo
import io.github.lujian213.eggfund.model.FundRTValue
import io.github.lujian213.eggfund.model.FundValue
import io.github.lujian213.eggfund.utils.Constants
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import java.time.LocalDate

class HKStockInfoLoaderSpec extends Specification {
    def "loadFund"() {
        given:
        def loader = Spy(HKStockInfoLoader) {
            extractFundName(_) >> "fund1"
        }
        def restTemplate = Mock(RestTemplate) {
            getForEntity(_, _) >> new ResponseEntity("content", HttpStatus.OK)
        }
        loader.setRestTemplate(restTemplate)
        when:
        def fundInfo = new FundInfo(id: "1001")
        def result = loader.loadFund(fundInfo)
        then:
        with(fundInfo) {
            id == "1001"
            name == "fund1"
            currency == HKStockInfoLoader.CURRENCY
        }
    }

    def "loadFund with non-ok status"() {
        given:
        def loader = new HKStockInfoLoader()
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
        def loader = Spy(HKStockInfoLoader) {
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
        def loader = new HKStockInfoLoader()
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
        def loader = new HKStockInfoLoader()
        def content = """
{"version":"3382af663ddf3977478474e6da9ed61a","result":{"pages":1,"data":[{"SECUCODE":"09988.HK","SECURITY_CODE":"09988","SECURITY_INNER_CODE":"1002044333","SECURITY_NAME_ABBR":"阿里巴巴-W","HOLD_DATE":"2025-04-01 00:00:00","ORG_CODE":"10012881","ORG_NAME":"中国证券登记结算有限责任公司","HOLD_NUM":724629567,"MARKET_CODE":"S","HOLD_SHARES_RATIO":3.81,"HOLD_MARKET_CAP":94056917796.6,"CLOSE_PRICE":129.8,"CHANGE_RATE":1.4063,"HOLD_MARKET_CAPONE":2055484580.6,"HOLD_MARKET_CAPFIVE":2029313245.1,"HOLD_MARKET_CAPTEN":-8138024016.6,"PARTICIPANT_CODE":"A00003"},{"SECUCODE":"09988.HK","SECURITY_CODE":"09988","SECURITY_INNER_CODE":"1002044333","SECURITY_NAME_ABBR":"阿里巴巴-W","HOLD_DATE":"2025-04-01 00:00:00","ORG_CODE":"10012881","ORG_NAME":"CHINA SECURITIES DEPOSITORY AND CLEARING","HOLD_NUM":773152120,"MARKET_CODE":"S","HOLD_SHARES_RATIO":4.06,"HOLD_MARKET_CAP":100355145176,"CLOSE_PRICE":129.8,"CHANGE_RATE":1.4063,"HOLD_MARKET_CAPONE":1580729816,"HOLD_MARKET_CAPFIVE":1124408482.4,"HOLD_MARKET_CAPTEN":-11293576003,"PARTICIPANT_CODE":"A00004"}],"count":2},"success":true,"message":"ok","code":0}"""
        expect:
        loader.extractFundName(content) == "阿里巴巴-W"
    }

    def "extractFundName with bad content"() {
        given:
        def loader = new HKStockInfoLoader()
        def content = "some bad content"
        expect:
        !loader.extractFundName(content)
    }

    def "loadFundValue"() {
        given:
        def fundValues = [
                new FundValue("2025-04-01", 129.8d, 129.8d, 1.4063d),
                new FundValue("2025-04-02", 130.0d, 130.0d, 1.5d)
        ]
        def loader = Spy(HKStockInfoLoader) {
            extractFundValue(_) >> fundValues
        }
        def restTemplate = Mock(RestTemplate) {
            getForEntity(_, _) >> new ResponseEntity("content", HttpStatus.OK)
        }
        loader.setRestTemplate(restTemplate)
        when:
        def from = LocalDate.parse("2025-04-01", Constants.DATE_FORMAT)
        def to = LocalDate.parse("2025-04-02", Constants.DATE_FORMAT)
        def result = loader.loadFundValue("1001", from, to)
        then:
        result.size() == 2
        result[0].day == "2025-04-01"
        result[1].day == "2025-04-02"
    }

    def "loadFundValue with non-200 code"() {
        given:
        def loader = Spy(HKStockInfoLoader)
        def restTemplate = Mock(RestTemplate) {
            getForEntity(_, _) >> new ResponseEntity("error", HttpStatus.NOT_FOUND)
        }
        loader.setRestTemplate(restTemplate)
        when:
        def from = LocalDate.parse("2025-04-01", Constants.DATE_FORMAT)
        def to = LocalDate.parse("2025-04-02", Constants.DATE_FORMAT)
        loader.loadFundValue("1001", from, to)
        then:
        thrown(EggFundException)
    }

    def "loadFundValue with exception"() {
        given:
        def loader = Spy(HKStockInfoLoader)
        def restTemplate = Mock(RestTemplate) {
            getForEntity(_, _) >> { throw new RestClientException("error") }
        }
        loader.setRestTemplate(restTemplate)
        when:
        def from = LocalDate.parse("2025-04-01", Constants.DATE_FORMAT)
        def to = LocalDate.parse("2025-04-02", Constants.DATE_FORMAT)
        loader.loadFundValue("1001", from, to)
        then:
        thrown(EggFundException)
    }

    def "extractFundValue" () {
        given:
        def loader = new HKStockInfoLoader()
        def content = """
{"version":"3382af663ddf3977478474e6da9ed61a","result":{"pages":1,"data":[\n
{"SECUCODE":"09988.HK","SECURITY_CODE":"09988","SECURITY_INNER_CODE":"1002044333","SECURITY_NAME_ABBR":"阿里巴巴-W","HOLD_DATE":"2025-04-01 00:00:00","ORG_CODE":"10012881","ORG_NAME":"中国证券登记结算有限责任公司","HOLD_NUM":724629567,"MARKET_CODE":"S","HOLD_SHARES_RATIO":3.81,"HOLD_MARKET_CAP":94056917796.6,"CLOSE_PRICE":129.8,"CHANGE_RATE":1.4063,"HOLD_MARKET_CAPONE":2055484580.6,"HOLD_MARKET_CAPFIVE":2029313245.1,"HOLD_MARKET_CAPTEN":-8138024016.6,"PARTICIPANT_CODE":"A00003"},\
{"SECUCODE":"09988.HK","SECURITY_CODE":"09988","SECURITY_INNER_CODE":"1002044333","SECURITY_NAME_ABBR":"阿里巴巴-W","HOLD_DATE":"2025-04-01 00:00:00","ORG_CODE":"10012881","ORG_NAME":"CHINA SECURITIES DEPOSITORY AND CLEARING","HOLD_NUM":773152120,"MARKET_CODE":"S","HOLD_SHARES_RATIO":4.06,"HOLD_MARKET_CAP":100355145176,"CLOSE_PRICE":129.8,"CHANGE_RATE":1.4063,"HOLD_MARKET_CAPONE":1580729816,"HOLD_MARKET_CAPFIVE":1124408482.4,"HOLD_MARKET_CAPTEN":-11293576003,"PARTICIPANT_CODE":"A00004"},\
{"SECUCODE":"09988.HK","SECURITY_CODE":"09988","SECURITY_INNER_CODE":"1002044333","SECURITY_NAME_ABBR":"阿里巴巴-W","HOLD_DATE":"2025-04-02 00:00:00","ORG_CODE":"10012881","ORG_NAME":"中国证券登记结算有限责任公司","HOLD_NUM":724629567,"MARKET_CODE":"S","HOLD_SHARES_RATIO":3.81,"HOLD_MARKET_CAP":94056917796.6,"CLOSE_PRICE":129.9,"CHANGE_RATE":1.4065,"HOLD_MARKET_CAPONE":2055484580.6,"HOLD_MARKET_CAPFIVE":2029313245.1,"HOLD_MARKET_CAPTEN":-8138024016.6,"PARTICIPANT_CODE":"A00003"}],\
"count":3},"success":true,"message":"ok","code":0}"""
        when:
        def values = loader.extractFundValue(content)
        then:
        values.size() == 2
        with(values[0]) {
            day == "2025-04-01"
            unitValue == 129.8d
            increaseRate == 0.014063d
        }
        with(values[1]) {
            day == "2025-04-02"
            unitValue == 129.9d
            Math.abs(increaseRate - 0.014065d) < 0.00001d
        }
    }

    def "extractFundValue with wrong date format" () {
        given:
        def loader = new HKStockInfoLoader()
        def content = """
{"version":"3382af663ddf3977478474e6da9ed61a","result":{"pages":1,"data":[\n
{"SECUCODE":"09988.HK","SECURITY_CODE":"09988","SECURITY_INNER_CODE":"1002044333","SECURITY_NAME_ABBR":"阿里巴巴-W","HOLD_DATE":"2025-04-01 00:00:00","ORG_CODE":"10012881","ORG_NAME":"中国证券登记结算有限责任公司","HOLD_NUM":724629567,"MARKET_CODE":"S","HOLD_SHARES_RATIO":3.81,"HOLD_MARKET_CAP":94056917796.6,"CLOSE_PRICE":129.8,"CHANGE_RATE":1.4063,"HOLD_MARKET_CAPONE":2055484580.6,"HOLD_MARKET_CAPFIVE":2029313245.1,"HOLD_MARKET_CAPTEN":-8138024016.6,"PARTICIPANT_CODE":"A00003"},\
{"SECUCODE":"09988.HK","SECURITY_CODE":"09988","SECURITY_INNER_CODE":"1002044333","SECURITY_NAME_ABBR":"阿里巴巴-W","HOLD_DATE":"2025-04-01 00:00:00","ORG_CODE":"10012881","ORG_NAME":"CHINA SECURITIES DEPOSITORY AND CLEARING","HOLD_NUM":773152120,"MARKET_CODE":"S","HOLD_SHARES_RATIO":4.06,"HOLD_MARKET_CAP":100355145176,"CLOSE_PRICE":129.8,"CHANGE_RATE":1.4063,"HOLD_MARKET_CAPONE":1580729816,"HOLD_MARKET_CAPFIVE":1124408482.4,"HOLD_MARKET_CAPTEN":-11293576003,"PARTICIPANT_CODE":"A00004"},\
{"SECUCODE":"09988.HK","SECURITY_CODE":"09988","SECURITY_INNER_CODE":"1002044333","SECURITY_NAME_ABBR":"阿里巴巴-W","HOLD_DATE":"2025-04-02","ORG_CODE":"10012881","ORG_NAME":"中国证券登记结算有限责任公司","HOLD_NUM":724629567,"MARKET_CODE":"S","HOLD_SHARES_RATIO":3.81,"HOLD_MARKET_CAP":94056917796.6,"CLOSE_PRICE":129.9,"CHANGE_RATE":1.4065,"HOLD_MARKET_CAPONE":2055484580.6,"HOLD_MARKET_CAPFIVE":2029313245.1,"HOLD_MARKET_CAPTEN":-8138024016.6,"PARTICIPANT_CODE":"A00003"}],\
"count":3},"success":true,"message":"ok","code":0}"""
        when:
        def values = loader.extractFundValue(content)
        then:
        values.size() == 1
    }

    def "extractFundValue with missing date" () {
        given:
        def loader = new HKStockInfoLoader()
        def content = """
{"version":"3382af663ddf3977478474e6da9ed61a","result":{"pages":1,"data":[\n
{"SECUCODE":"09988.HK","SECURITY_CODE":"09988","SECURITY_INNER_CODE":"1002044333","SECURITY_NAME_ABBR":"阿里巴巴-W","HOLD_DATE":"2025-04-01 00:00:00","ORG_CODE":"10012881","ORG_NAME":"中国证券登记结算有限责任公司","HOLD_NUM":724629567,"MARKET_CODE":"S","HOLD_SHARES_RATIO":3.81,"HOLD_MARKET_CAP":94056917796.6,"CLOSE_PRICE":129.8,"CHANGE_RATE":1.4063,"HOLD_MARKET_CAPONE":2055484580.6,"HOLD_MARKET_CAPFIVE":2029313245.1,"HOLD_MARKET_CAPTEN":-8138024016.6,"PARTICIPANT_CODE":"A00003"},\
{"SECUCODE":"09988.HK","SECURITY_CODE":"09988","SECURITY_INNER_CODE":"1002044333","SECURITY_NAME_ABBR":"阿里巴巴-W","HOLD_DATE":"2025-04-01 00:00:00","ORG_CODE":"10012881","ORG_NAME":"CHINA SECURITIES DEPOSITORY AND CLEARING","HOLD_NUM":773152120,"MARKET_CODE":"S","HOLD_SHARES_RATIO":4.06,"HOLD_MARKET_CAP":100355145176,"CLOSE_PRICE":129.8,"CHANGE_RATE":1.4063,"HOLD_MARKET_CAPONE":1580729816,"HOLD_MARKET_CAPFIVE":1124408482.4,"HOLD_MARKET_CAPTEN":-11293576003,"PARTICIPANT_CODE":"A00004"},\
{"SECUCODE":"09988.HK","SECURITY_CODE":"09988","SECURITY_INNER_CODE":"1002044333","SECURITY_NAME_ABBR":"阿里巴巴-W","ORG_CODE":"10012881","ORG_NAME":"中国证券登记结算有限责任公司","HOLD_NUM":724629567,"MARKET_CODE":"S","HOLD_SHARES_RATIO":3.81,"HOLD_MARKET_CAP":94056917796.6,"CLOSE_PRICE":129.9,"CHANGE_RATE":1.4065,"HOLD_MARKET_CAPONE":2055484580.6,"HOLD_MARKET_CAPFIVE":2029313245.1,"HOLD_MARKET_CAPTEN":-8138024016.6,"PARTICIPANT_CODE":"A00003"}],\
"count":3},"success":true,"message":"ok","code":0}"""
        when:
        def values = loader.extractFundValue(content)
        then:
        values.size() == 1
    }

    def "loadFundRTValuesForPage"() {
        given:
        def fundRTValue = new FundRTValue("2025-05-02 16:08", 0.1d, 0.2d)
        def loader = Spy(HKStockInfoLoader) {
            extractFundRTValue("content") >> ["0001": fundRTValue]
        }
        def restTemplate = Mock(RestTemplate) {
            getForEntity(_, _, _) >> new ResponseEntity("content", HttpStatus.OK)
        }
        loader.setRestTemplate(restTemplate)
        when:
        def count = loader.loadFundRTValuesForPage(1)
        then:
        count == 1
        loader.rtValueMap["0001"] == fundRTValue
    }

    def "loadFundRTValuesForPage with non-200 code"() {
        given:
        def fundRTValue = new FundRTValue("2025-05-02 16:08", 0.1d, 0.2d)
        def loader = Spy(HKStockInfoLoader) {
            extractFundRTValue("content") >> ["0001": fundRTValue]
        }
        def restTemplate = Mock(RestTemplate) {
            getForEntity(_, _, _) >> new ResponseEntity("error", HttpStatus.NOT_FOUND)
        }
        loader.setRestTemplate(restTemplate)
        when:
        def count = loader.loadFundRTValuesForPage(1)
        then:
        count == 0
        loader.rtValueMap.isEmpty()
    }

    def "loadFundRTValuesForPage with exception"() {
        given:
        def loader = Spy(HKStockInfoLoader)
        def restTemplate = Mock(RestTemplate) {
            getForEntity(_, _, _) >> {throw new RestClientException("error")}
        }
        loader.setRestTemplate(restTemplate)
        when:
        def count = loader.loadFundRTValuesForPage(1)
        then:
        count == 0
        loader.rtValueMap.isEmpty()
    }

    def "extractFundRTValue"() {
        given:
        def loader = new HKStockInfoLoader()
        def content="""
[{"symbol":"09882","name":"永联丰控股","engname":"BEST LINKING","tradetype":"","lasttrade":"0.65000","prevclose":"0.74000","open":"0.64000","high":"0.64000","low":"0.64000","volume":"6000","currentvolume":"0","amount":"4000","ticktime":"2025/05/02 16:08:26","buy":"0.65000","sell":"0.73000","high_52week":"1.40000","low_52week":"0.28500","eps":"0","dividend":"0","stocks_sum":"0","pricechange":"-0.09000","changepercent":"-12.16217","market_value":"0","pe_ratio":"0"},{"symbol":"09885","name":"药师帮","engname":"YSB","tradetype":"","lasttrade":"7.14000","prevclose":"6.55000","open":"6.55000","high":"7.15000","low":"6.44000","volume":"1604600","currentvolume":"0","amount":"11221880","ticktime":"2025/05/02 16:08:26","buy":"7.06000","sell":"7.14000","high_52week":"9.08000","low_52week":"5.18000","eps":"0","dividend":"0","stocks_sum":"0","pricechange":"0.59000","changepercent":"9.00763","market_value":"0","pe_ratio":"0"}]"""
        when:
        def values = loader.extractFundRTValue(content)
        then:
        values.size() == 2
        with(values["09882"]) {
            time == "2025-05-02 16:08"
            unitValue == 0.65d
            Math.abs(increaseRate - (-0.1216217d)) < 0.00001d
        }
        with(values["09885"]) {
            time == "2025-05-02 16:08"
            unitValue == 7.14d
            Math.abs(increaseRate - 0.0900763d) < 0.00001d
        }
    }

    def "extractFundRTValue with wrong format tickertime"() {
        given:
        def loader = new HKStockInfoLoader()
        def content="""
[{"symbol":"09882","name":"永联丰控股","engname":"BEST LINKING","tradetype":"","lasttrade":"0.65000","prevclose":"0.74000","open":"0.64000","high":"0.64000","low":"0.64000","volume":"6000","currentvolume":"0","amount":"4000","ticktime":"2025/05/02","buy":"0.65000","sell":"0.73000","high_52week":"1.40000","low_52week":"0.28500","eps":"0","dividend":"0","stocks_sum":"0","pricechange":"-0.09000","changepercent":"-12.16217","market_value":"0","pe_ratio":"0"},\
{"symbol":"09885","name":"药师帮","engname":"YSB","tradetype":"","lasttrade":"7.14000","prevclose":"6.55000","open":"6.55000","high":"7.15000","low":"6.44000","volume":"1604600","currentvolume":"0","amount":"11221880","ticktime":"2025/05/02 16:08:26","buy":"7.06000","sell":"7.14000","high_52week":"9.08000","low_52week":"5.18000","eps":"0","dividend":"0","stocks_sum":"0","pricechange":"0.59000","changepercent":"9.00763","market_value":"0","pe_ratio":"0"}]"""
        when:
        def values = loader.extractFundRTValue(content)
        then:
        values.size() == 1
        with(values["09885"]) {
            time == "2025-05-02 16:08"
            unitValue == 7.14d
            Math.abs(increaseRate - 0.0900763d) < 0.00001d
        }
    }

    def "extractFundRTValue with bad content"() {
        given:
        def loader = new HKStockInfoLoader()
        def content="bad content"
        when:
        def values = loader.extractFundRTValue(content)
        then:
        values.size() == 0
    }

    def "loadFundRTValues"() {
        given:
        def loader = Spy(HKStockInfoLoader) {
            15 * loadFundRTValuesForPage(_) >> { int page ->
                def ret = switch (page) {
                    case 1..14 -> 1
                    default -> 0
                }
                println "page:$page, ret:$ret"
                ret
            }
        }
        loader.executor = new SimpleAsyncTaskExecutor()
        when:
        loader.loadFundRTValues()
        then:
        System.currentTimeMillis() - loader.lastFundRTValueLodeTime < 1000
    }

    def "getFundRTValue without load"() {
        given:
        def executor = new SimpleAsyncTaskExecutor()
        def rtValue = new FundRTValue("2025-05-02 16:08", 0.1d, 0.2d)
        def loader = Spy(HKStockInfoLoader) {
            it.@lastFundRTValueLodeTime = System.currentTimeMillis()
            it.@rtValueMap << ["0001": rtValue]
            it.executor = executor
            0 * loadFundRTValues(_)
        }
        expect:
        loader.getFundRTValue("0001") == rtValue
        cleanup:
        executor.close()
    }

    def "getFundRTValue with load"() {
        given:
        def rtValue = new FundRTValue("2025-05-02 16:08", 0.1d, 0.2d)
        def executor = new SimpleAsyncTaskExecutor()
        def loader = Spy(HKStockInfoLoader) {hkloader->
            hkloader.@lastFundRTValueLodeTime = System.currentTimeMillis() - HKStockInfoLoader.MAX_LOAD_INTERVAL - 1
            hkloader.executor = executor
            1 * hkloader.loadFundRTValues() >> {
                Thread.sleep(500)
                hkloader.@rtValueMap << ["0001": rtValue]
            }
        }
        expect:
        loader.getFundRTValue("0001") == null

        when:
        loader.lastFundRTValueLodeTime = System.currentTimeMillis()
        Thread.sleep(700)
        then:
        loader.getFundRTValue("0001") == rtValue
        cleanup:
        executor.close()
    }
}
