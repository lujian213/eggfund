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
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import java.time.LocalDate
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

class FundDataServiceSpec extends Specification {

    def "groupCodesToBatches"() {
        given:
        def service = new FundDataService()

        expect:
        batches == service.groupCodesToBatches(codes, maxBatches, minBatchSize).stream().map(item -> item.length).toList()
        where:
        codes                                               | maxBatches | minBatchSize | batches
        ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"] | 3          | 1            | [4, 4, 2]
        ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"] | 3          | 2            | [4, 4, 2]
        ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"] | 3          | 3            | [4, 4, 2]
        ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"] | 3          | 4            | [4, 4, 2]
        ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"] | 3          | 5            | [5, 5]
        ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"] | 3          | 6            | [6, 4]
        ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"] | 3          | 7            | [7, 3]
        ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"] | 3          | 8            | [8, 2]
        ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"] | 3          | 9            | [9, 1]
        ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"] | 3          | 10           | [10]

    }

    def "getBackupFundRTValue"() {
        given:
        def service = Spy(FundDataService) {
            getLatestFundValue("1000") >> new FundValue("2025-01-01", 1.01, 1.01, 0.01)
            getLatestFundValue("1001") >> null
        }
        when:
        def rtValue = service.getBackupFundRTValue(code)
        then:
        time == Optional.ofNullable(rtValue).map(FundRTValue::getTime).orElse(null)
        where:
        code   | time
        "1000" | "2025-01-01 15:00"
        "1001" | null
    }

    def "handleBatchRTValues"() {
        given:
        def service = new FundDataService()
        when:
        def batch = new HashMap<String, FundRTValue>()
        batch << ["999": new FundRTValue("2025-01-01 14:00", 1.00, 0.02)]
        batch << ["1000": null]
        service.handleBatchRTValues(batch)
        then:
        with(service.fundRTValueHistoryMap["999"]) {
            day() == "2025-01-01"
            rtValueMap()["2025-01-01 14:00"].unitValue == 1.00d
        }
        !service.fundRTValueHistoryMap["1000"]
        when:
        batch = new HashMap<String, FundRTValue>()
        batch << ["999": new FundRTValue("2025-01-01 14:30", 1.01, 0.01)]
        batch << ["1000": new FundRTValue("2025-01-01 14:32", 1.05, 0.05)]
        service.handleBatchRTValues(batch)
        then:
        with(service.fundRTValueHistoryMap["999"]) {
            day() == "2025-01-01"
            rtValueMap()["2025-01-01 14:00"].unitValue == 1.00d
            rtValueMap()["2025-01-01 14:30"].unitValue == 1.01d
        }

        with(service.fundRTValueHistoryMap["1000"]) {
            day() == "2025-01-01"
            rtValueMap()["2025-01-01 14:32"].unitValue == 1.05d
        }
        when:
        batch = new HashMap<String, FundRTValue>()
        batch << ["999": new FundRTValue("2025-01-02 14:00", 1.03, 0.01)]
        service.handleBatchRTValues(batch)
        then:
        with(service.fundRTValueHistoryMap["999"]) {
            day() == "2025-01-02"
            rtValueMap.size() == 1
            rtValueMap()["2025-01-02 14:00"].unitValue == 1.03d
        }
    }

    def "updateFundsValues"() {
        given:
        def service = new FundDataService()
        def fund0 = new FundInfo("1000", "fund0")
        def fund1 = new FundInfo("1001", "fund1")
        def fund2 = new FundInfo("1002", "fund2")
        def fund3 = new FundInfo("1003", "fund3")
        service.fundInfoMap << ["1000": fund0]
        service.fundInfoMap << ["1001": fund1]
        service.fundInfoMap << ["1002": fund2]
        service.fundInfoMap << ["1003": fund3]
        def codes = [] as Set
        def asyncService = Mock(AsyncActionService) {
            4 * updateFundValues(_ as FundInfo, _ as DateRange, _ as FundValueListener) >> {
                FundInfo fund, DateRange range, FundValueListener listener ->
                    if (fund.getId() == "1003") {
                        CompletableFuture<Void> future = new CompletableFuture<>()
                        future.completeExceptionally(new EggFundException())
                        return future
                    }
                    codes << fund.getId()
                    return CompletableFuture.completedFuture(null)
            }
        }
        service.setAsyncActionService(asyncService)
        when:
        service.updateFundsValues()
        then:
        codes == ["1000", "1001", "1002"] as Set
    }

    def "extractFundName" () {
        given:
        def service = new FundDataService()
        def content = """\
/** * 测试数据 * @type {arry} *//*2025-03-03 21:28:39*/var ishb=false;/*基金或股票信息*/var fS_name = "华夏国证半导体芯片ETF联接C";var fS_code = "008888";/*原费率*/var fund_sourceRate="0.00";/*现费率*/var fund_Rate="0.00";/*最小申购金额*/var fund_minsg="10";/*基金持仓股票代码*/var stockCodes=[];/*基金持仓债券代码*/var zqCodes = "0197491,0197491,0196311,0196311,0197061";/*基金持仓股票代码(新市场号)*/var stockCodesNew =[];/*基金持仓债券代码（新市场号）\
        """
        expect:
        service.extractFundName(content) == "华夏国证半导体芯片ETF联接C"
    }
    def "extractFundName with bad content"() {
        given:
        def service = new FundDataService()
        def content = "some bad content"
        expect:
        !service.extractFundName(content)
    }

    def "checkFund & findFund"() {
        given:
        def service = new FundDataService()
        def fund = new FundInfo("1000", "fund0")
        service.fundInfoMap << ["1000": fund]
        expect:
        service.checkFund("1000") == fund
        service.findFund("1000") == fund
        when:
        service.checkFund("1001")
        then:
        thrown(EggFundException)
        expect:
        !service.findFund("1001")
    }

    def "deleteFund"() {
        given:
        def service = new FundDataService()
        def dao = Mock(FundDao) {
            saveFunds(_ as Collection<FundInfo>) >> null
            deleteFundValues(_ as String) >> null
        }
        service.setFundDao(dao)
        def fund1 = new FundInfo("1001", "fund1")
        def fund2 = new FundInfo("1002", "fund2")
        service.fundInfoMap << ["1001": fund1]
        service.fundInfoMap << ["1002": fund2]
        when:
        service.deleteFund("1001")
        then:
        service.fundInfoMap.size() == 1
        service.fundInfoMap["1001"] == null
    }


    def "deletefund with exception"() {
        given:
        def service = new FundDataService()
        def dao = Mock(FundDao) {
            saveFunds(_ as Collection<FundInfo>) >> { throw new IOException() }
            deleteFundValues(_ as String) >> null
        }
        service.setFundDao(dao)
        def fund1 = new FundInfo("1001", "fund1")
        def fund2 = new FundInfo("1002", "fund2")
        service.fundInfoMap << ["1001": fund1]
        service.fundInfoMap << ["1002": fund2]
        when:
        service.deleteFund("1001")
        then:
        thrown(EggFundException)
        service.fundInfoMap.size() == 2
    }

    def "updateFund"() {
        given:
        def service = new FundDataService()
        def dao = Mock(FundDao) {
            saveFunds(_ as Collection<FundInfo>) >> null
        }
        service.setFundDao(dao)
        def fund1 = new FundInfo("1001", "fundl")
        def fund2 = new FundInfo("1002", "fund2")
        def newFund = new FundInfo("1002", "newfund2")
        with(newFund) {
            setPriority(1)
            setUrl("http://newfund2")
            setAlias("newfund2")
            setCategory("cat")
            setEtf(true)
        }
        service.fundInfoMap << ["1001": fund1]
        service.fundInfoMap << ["1002": fund2]
        when:
        def result = service.updateFund(newFund)

        then:
        service.fundInfoMap.size() == 2
        with(service.fundInfoMap["1002"]) {
            getId() == "1002"
            getName() == "fund2"
            getPriority() == 1
            getUrl() == "http://newfund2"
            getAlias() == "newfund2"
            getCategory() == "cat"
            isEtf()
        }
        result == service.fundInfoMap["1002"]
    }

    def "updateFund with exception"() {
        given:
        def service = new FundDataService()
        def dao = Mock(FundDao) {
            saveFunds(_ as Collection<FundInfo>) >> { throw new IOException() }
        }
        service.setFundDao(dao)
        def fund1 = new FundInfo("1001", "Fund1")
        def fund2 = new FundInfo("1002", "Fund2")
        def newFund = new FundInfo("1002", "newfund2").setAlias("newFund")
        service.fundInfoMap << ["1001": fund1]
        service.fundInfoMap << ["1002": fund2]
        when:
        service.updateFund(newFund)
        then:
        thrown(EggFundException)
        service.fundInfoMap.size() == 2
        service.fundInfoMap["1002"].getAlias() == null
    }

    def "addNewFund"() {
        given:
        def service = Spy(FundDataService) {
            loadFundName(_ as String) >> "fund3"
        }
        def meterRegistry = Mock(MeterRegistry) {
            timer(_ as String) >> Mock(Timer) {
                record(_ as Supplier) >> { Supplier supp -> supp.get() }
            }
        }
        def dao = Mock(FundDao) {
            saveFunds(_ as Collection<FundInfo>) >> null
        }
        def asyncService = Mock(AsyncActionService) {
            updateFundValues(_ as FundInfo, _ as DateRange, _ as FundValueListener) >> CompletableFuture.completedFuture(null)
        }
        service.setFundDao(dao)
        service.setMeterRegistry(meterRegistry)
        service.setAsyncActionService(asyncService)
        def fund1 = new FundInfo("1001", "fund1")
        def fund2 = new FundInfo("1002", "fund2")
        def fund3 = new FundInfo("1003", null)
        service.fundInfoMap << ["1001": fund1]
        service.fundInfoMap << ["1002": fund2]
        when:
        def result = service.addNewFund(fund3)
        then:
        service.fundInfoMap.size() == 3
        with(service.fundInfoMap["1003"]) {
            getId() == "1003"
            getName() == "fund3"
        }
        result == service.fundInfoMap["1003"]
    }

    def "addNewFund with existing fund"() {
        given:
        def service = new FundDataService()
        def fund1 = new FundInfo("1001", "fund1")
        def fund2 = new FundInfo("1002", "fund2")
        def fund3 = new FundInfo("1002", null)
        service.fundInfoMap << ["1001": fund1]
        service.fundInfoMap << ["1002": fund2]
        when:
        service.addNewFund(fund3)
        then:
        service.fundInfoMap.size() == 2
        thrown(EggFundException)
    }

    def "addNewFund with exception"() {
        given:
        def service = Spy(FundDataService) {
            loadFundName(_ as String) >> "fund3"
        }
        def meterRegistry = Mock(MeterRegistry) {
            timer(_ as String) >> Mock(Timer) {
                record(_ as Supplier) >> { Supplier supp -> supp.get() }
            }
        }
        def dao = Mock(FundDao) {
            saveFunds(_ as Collection<FundInfo>) >> { throw new IOException() }
        }
        service.setFundDao(dao)
        service.setMeterRegistry(meterRegistry)
        def fund1 = new FundInfo("1001", "fund1")
        def fund2 = new FundInfo("1002", "fund2")
        def fund3 = new FundInfo("1003", null)
        service.fundInfoMap << ["1001": fund1]
        service.fundInfoMap << ["1002": fund2]
        when:
        service.addNewFund(fund3)
        then:
        thrown(EggFundException)
        service.fundInfoMap.size() == 2
    }

    def "getAllFunds"() {
        given:
        def service = new FundDataService()
        def fund1 = new FundInfo("1001", "fund1").setPriority(10)
        def fund2 = new FundInfo("1002", "fund2").setPriority(8)
        def fund3 = new FundInfo("1003", "fund3").setPriority(6)
        def fund4 = new FundInfo("1004", "fund4").setPriority(4)
        service.fundInfoMap << ["1001": fund1]
        service.fundInfoMap << ["1002": fund2]
        service.fundInfoMap << ["1003": fund3]
        service.fundInfoMap << ["1004": fund4]
        expect:
        service.getAllFunds() == [fund4, fund3, fund2, fund1]
    }

    def "getFundValue"() {
        given:
        def service = Spy(FundDataService) {
            updateFundValues(_ as String, _ as DateRange) >> null
            checkFund(_ as String) >> null
        }
        def value1 = new FundValue("2025-01-01", 1.01, 1.01, 0.01)
        def value2 = new FundValue("2025-01-02", 1.02, 1.02, 0.02)
        def value3 = new FundValue("2025-01-03", 1.03, 1.03, 0.03)
        def fundValueList = [value1, value2, value3]
        service.fundValueMap << ["1000": ["202501": fundValueList]]
        expect:
        def ret = service.getFundValue(code, LocalDate.parse(date, Constants.DATE_FORMAT))
        Optional.ofNullable(ret).orElse(new FundValue("2025-12-31", 1.0, 1.0, 1.0)).getDay() == result
        where:
        code    | date         | result
        "1000"  | "2025-01-01" | "2025-01-01"
        "1000"  | "2025-01-02" | "2025-01-02"
        "1000 " | "2025-01-04" | "2025-12-31"
        "1001"  | "2025-01-01" | "2025-12-31"
    }

    def "updateFundRTValues"() {
        given:
        def service = new FundDataService()
        def asyncService = Mock(AsyncActionService) {
            getFundRTValueInBatch(_ as String[]) >> { String[] batch ->
                def ret = [:] as Map
                if (batch.length == 2) {
                    CompletableFuture<Map<String, FundRTValue>> future = new CompletableFuture<>()
                    future.completeExceptionally(new EggFundException())
                    return future
                }
                batch.each {
                    ret << [(it): new FundRTValue("2025-01-01 14:00", 1.00, 0.02)]
                }
                return CompletableFuture.completedFuture(ret)
            }
        }
        service.setAsyncActionService(asyncService)
        service.fundInfoMap << ["1000": new FundInfo("1000", "fund0")]
        service.fundInfoMap << ["1001": new FundInfo("1001", "fund1")]
        service.fundInfoMap << ["1002": new FundInfo("1002", "fund2")]
        service.fundInfoMap << ["1003": new FundInfo("1003", "fund3")]
        service.fundInfoMap << ["1004": new FundInfo("1004", "fund4")]

        when:
        service.updateFundRTValues()
        then:
        service.fundRTValueHistoryMap.size() == 3
    }

    def "onFundValueChange"() {
        def service = new FundDataService()
        def value1 = new FundValue("2025-01-01", 1.01, 1.01, 0.01)
        def value2 = new FundValue("2025-01-02", 1.02, 1.02, 0.02)
        def value3 = new FundValue("2025-01-03", 1.03, 1.03, 0.03)
        service.fundValueMap << ["1001": ["202501": [value1, value2, value3]]]
        when:
        service.onFundValueChange("1001", "200502", [value1, value2, value3])
        then:
        service.fundValueMap.size() == 1
        service.fundValueMap["1001"]["200502"].size() == 3
        when:
        service.onFundValueChange("1002", "200502", [value1, value2, value3])
        then:
        service.fundValueMap.size() == 2
        service.fundValueMap["1002"]["200502"].size() == 3
    }

    def "getFundRTValues"() {
        def service = Spy(FundDataService) {
            1 * getBackupFundRTValue(_) >> { String code ->
                switch (code) {
                    case "1003" -> new FundRTValue("2025-01-01 14:05", 1.06, 0.02)
                    default -> null
                }
            }
        }
        def fund1 = new FundInfo("1001", "fund1").setPriority(10)
        def fund2 = new FundInfo("1002", "fund2").setPriority(8)
        def fund3 = new FundInfo("1003", "fund3").setPriority(6)
        def fund4 = new FundInfo("1004", "fund4").setPriority(4)

        service.fundInfoMap << ["1001": fund1]
        service.fundInfoMap << ["1002": fund2]
        service.fundInfoMap << ["1003": fund3]
        service.fundInfoMap << ["1004": fund4]
        def rtValueMap = [:] as Map
        rtValueMap << ["2025-01-01 14:00": new FundRTValue("2025-01-01 14:00", 1.00, 0.02)]
        rtValueMap << ["2025-01-01 13:30": new FundRTValue("2025-01-01 13:30", 1.01, 0.01)]
        service.fundRTValueHistoryMap << ["1001": new FundDataService.FundRTValueHistory("2025-01-01", rtValueMap)]
        service.fundRTValueHistoryMap << ["1002": new FundDataService.FundRTValueHistory("2025-01-01", ["2025-01-01 14:00": new FundRTValue("2025-01-01 14:00", 1.00, 0.02)])]
        when:
        def result = service.getFundRTValues(["1001", "1002", "1003", "1005"])
        then:
        result.size() == 3
        result["1001"].time == "2025-01-01 14:00"
        result["1002"] != null
        result["1003"] != null
    }

    def "getFundRTValueHistory"() {
        def service = new FundDataService()
        def fund1 = new FundInfo("1001", "fund1").setPriority(10)
        def fund2 = new FundInfo("1002", "fund2").setPriority(8)
        def fund3 = new FundInfo("1003", "fund3").setPriority(6)
        def fund4 = new FundInfo("1004", "fund4").setPriority(4)
        service.fundInfoMap << ["1001": fund1]
        service.fundInfoMap << ["1002": fund2]
        service.fundInfoMap << ["1003": fund3]
        service.fundInfoMap << ["1004": fund4]
        def rtValueMap = [:] as Map
        rtValueMap << ["2025-01-01 14:00": new FundRTValue("2025-01-01 14:00", 1.00, 0.02)]
        rtValueMap << ["2025-01-01 13:30": new FundRTValue("2025-01-01 13:30", 1.01, 0.01)]
        rtValueMap << ["2025-01-01 13:25": new FundRTValue("2025-01-01 13:25", 1.02, 0.01)]
        service.fundRTValueHistoryMap << ["1001": new FundDataService.FundRTValueHistory("2025-01-01", rtValueMap)]
        service.fundRTValueHistoryMap << ["1002": new FundDataService.FundRTValueHistory("2025-01-01", ["2025-01-01 14:00": new FundRTValue("2025-01-01 14:00", 1.00, 0.02)])]
        when:
        def result = service.getFundRTValueHistory("1001")
        then:
        result.size() == 3
        result[0].time == "2025-01-01 13:25"
        result[1].time == "2025-01-01 13:30"
        result[2].time == "2025-01-01 14:00"
        when:
        result = service.getFundRTValueHistory("1002")
        then:
        result.size() == 1
        result[0].time == "2025-01-01 14:00"
        when:
        result = service.getFundRTValueHistory("1003")
        then:
        result.size() == 0
    }

    def "getLatestFundValue"() {
        given:
        def service = new FundDataService()
        service.fundInfoMap << ["1001": new FundInfo("1001", "fund1")]
        service.fundInfoMap << ["1002": new FundInfo("1002", "fund2")]

        def value1 = new FundValue("2025-01-01", 1.01, 1.01, 0.01)
        def value2 = new FundValue("2025-01-02", 1.02, 1.02, 0.02)
        def value3 = new FundValue("2025-01-03", 1.03, 1.03, 0.03)
        service.fundValueMap << ["1001": ["202501": [value1, value2, value3]]]

        def value4 = new FundValue("2025-02-01", 1.01, 1.01, 0.01)
        def value5 = new FundValue("2025-02-02", 1.02, 1.02, 0.02)
        service.fundValueMap["1001"] << ["202502": [value4, value5]]
        expect:
        service.getLatestFundValue("1001") == value5
        service.getLatestFundValue("1002") == null
    }

    def "getFundValues"() {
        given:
        def service = new FundDataService()
        service.fundInfoMap << ["1001": new FundInfo("1001", "fund1")]
        service.fundInfoMap << ["1002": new FundInfo("1002", "fund2")]

        def value1 = new FundValue("2025-01-01", 1.01, 1.01, 0.01)
        def value2 = new FundValue("2025-01-02", 1.02, 1.02, 0.02)
        def value3 = new FundValue("2025-01-03", 1.03, 1.03, 0.03)
        service.fundValueMap << ["1001": ["202501": [value1, value2, value3]]]

        def value4 = new FundValue("2025-02-01", 1.01, 1.01, 0.01)
        def value5 = new FundValue("2025-02-02", 1.02, 1.02, 0.02)
        service.fundValueMap["1001"] << ["202502": [value4, value5]]

        when:
        def from = LocalDate.parse("2025-01-02", Constants.DATE_FORMAT)
        def to = LocalDate.parse("2025-02-01", Constants.DATE_FORMAT)
        def result = service.getFundValues("1001", new DateRange(from, to))
        then:
        result.size() == 3
        result[0] == value2
        result[1] == value3
        result[2] == value4
        when:
        result = service.getFundValues("1002", new DateRange(from, to))
        then:
        result.size() == 0
        when:
        from = LocalDate.parse("2025-03-01", Constants.DATE_FORMAT)
        to = LocalDate.parse("2025-03-03", Constants.DATE_FORMAT)
        result = service.getFundValues("1001", new DateRange(from, to))
        then:
        result.size() == 0
    }

    def "init"() {
        given:
        def service = new FundDataService()
        def dao = Mock(FundDao) {
            1 * loadFundInfo() >> [new FundInfo("1001", "fund1"), new FundInfo("1002", "fund2")]
            2 * loadFundValues(_) >> ["202501": new FundValue("2025-01-01", 1.01, 1.01, 0.01)]
        }
        service.setFundDao(dao)
        when:
        service.init()
        then:
        service.fundInfoMap.size() == 2
        service.fundValueMap.size() == 2
    }

    def "loadFundName"() {
        given:
        def service = Spy(FundDataService) {
            extractFundName(_) >> "fund1"
        }
        def restTemplate = Mock(RestTemplate) {
            getForEntity(_, _) >> new ResponseEntity("content", HttpStatus.OK)
        }
        service.setRestTemplate(restTemplate)
        when:
        def result = service.loadFundName("1001")
        then:
        result == "fund1"
    }

    def "loadFundName with non-ok status"() {
        given:
        def service = new FundDataService()
        def restTemplate = Mock(RestTemplate) {
            getForEntity(_, _) >> new ResponseEntity("content", HttpStatus.NOT_FOUND)
        }
        service.setRestTemplate(restTemplate)
        when:
        def result = service.loadFundName("1001")
        then:
        thrown(EggFundException)
    }

    def "loadFundName with bad content"() {
        def service = Spy(FundDataService) {
            extractFundName(_) >> null
        }
        def restTemplate = Mock(RestTemplate) {
            getForEntity(_, _) >> new ResponseEntity("content", HttpStatus.OK)
        }
        service.setRestTemplate(restTemplate)
        when:
        def result = service.loadFundName("1001")
        then:
        thrown(EggFundException)
    }

    def "loadFundName with exception in request"() {
        given:
        def service = new FundDataService()
        def restTemplate = Mock(RestTemplate) {
            getForEntity(_, _) >> { throw new RestClientException("") }
        }
        service.setRestTemplate(restTemplate)

        when:
        def result = service.loadFundName("1001")
        then:
        thrown(EggFundException)
    }
}