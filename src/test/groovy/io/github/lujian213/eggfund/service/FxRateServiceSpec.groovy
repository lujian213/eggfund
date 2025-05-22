package io.github.lujian213.eggfund.service

import io.github.lujian213.eggfund.dao.FxRateDao
import io.github.lujian213.eggfund.exception.EggFundException
import io.github.lujian213.eggfund.model.FundInfo
import io.github.lujian213.eggfund.model.FxRateInfo
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class FxRateServiceSpec extends Specification {
    def "extractFxRate with valid content"() {
        given:
        def fxRateService = new FxRateService()
        def content = """{
  "actionErrors": [],
  "actionMessages": [],
  "fieldErrors": {},
  "isPagination": "true",
  "jsonCallBack": null,
  "locale": "en",
  "pageNo": null,
  "pageSize": null,
  "queryDate": "",
  "result": [
    {
      "buyPrice": " 0.92317",
      "currencyType": "HKD",
      "updateDate": "20250514",
      "validDate": "20250514",
      "sellPrice": " 0.92323"
    },
    {
      "buyPrice": " 0.92551",
      "currencyType": "HKD",
      "updateDate": "20250513",
      "validDate": "20250513",
      "sellPrice": " 0.92529"
    },
    {
      "buyPrice": " 0.93124",
      "currencyType": "HKD",
      "updateDate": "20250512",
      "validDate": "20250512",
      "sellPrice": " 0.93236"
    },
    {
      "buyPrice": " 0.93114aaa",
      "currencyType": "HKD",
      "updateDate": "20250509",
      "validDate": "20250509",
      "sellPrice": " 0.93126"
    },
    {
      "buyPrice": " 0.93109",
      "currencyType": "HKD",
      "updateDate": "20250508",
      "validDate": "20250508",
      "sellPrice": " 0.93111"
    },
    {
      "buyPrice": " 0.93077",
      "currencyType": "HKD",
      "updateDate": "20250507",
      "validDate": "20250507",
      "sellPrice": " 0.93083"
    }
  ],
  "securityCode": "",
  "sqlId": "FW_HGT_JSHDBL",
  "texts": null,
  "type": "",
  "validateCode": ""
}
"""
        when:
        def fxRateList = fxRateService.extractFxRate(content)
        then:
        fxRateList.size() == 2
        with(fxRateList[0]) {
            currency() == "HKD"
            fxRate() == 0.92317d
            asOfTime() == "2025-05-14"
        }
        fxRateList[1] == FxRateInfo.RMB
    }

    def "extractFxRate with bad content"() {
        given:
        def fxRateService = new FxRateService()
        def content = "bad content"
        when:
        def fxRateList = fxRateService.extractFxRate(content)
        then:
        fxRateList.size() == 1
        fxRateList[0] == FxRateInfo.RMB
    }

    def "loadCurrencies"() {
        given:
        def content = "some content"
        def restTemplate = Mock(RestTemplate) {
            exchange(_, HttpMethod.GET, _, String.class) >> Mock(ResponseEntity) {
                getStatusCode() >> HttpStatus.OK
                getBody() >> content
            }

        }
        def fxRateService = Spy(FxRateService) {
            extractFxRate(content) >> [
                    new FxRateInfo("HKD", 0.92317d, "2025-05-14"),
                    FxRateInfo.RMB
            ]
        }
        fxRateService.setRestTemplate(restTemplate)

        when:
        def result = fxRateService.loadCurrencies(["HKD"])

        then:
        result.size() == 2
    }

    def "loadCurrencies with non 200 code"() {
        given:
        def restTemplate = Mock(RestTemplate) {
            exchange(_, HttpMethod.GET, _, String.class) >> Mock(ResponseEntity) {
                getStatusCode() >> HttpStatus.NOT_FOUND
            }
        }
        def fxRateService = new FxRateService()
        fxRateService.setRestTemplate(restTemplate)

        when:
        fxRateService.loadCurrencies(["HKD"])

        then:
        thrown(EggFundException)
    }

    def "loadCurrencies with exception"() {
        given:
        def restTemplate = Mock(RestTemplate) {
            exchange(_, HttpMethod.GET, _, String.class) >> Mock(ResponseEntity) {
                getStatusCode() >> { throw new RestClientException("error") }
            }
        }
        def fxRateService = new FxRateService()
        fxRateService.setRestTemplate(restTemplate)

        when:
        fxRateService.loadCurrencies(["HKD"])

        then:
        thrown(EggFundException)
    }

    def "updateFxRate"() {
        given:
        def fundDataService = Mock(FundDataService) {
            getAllFunds() >> [new FundInfo(type: FundInfo.FundType.HK_STOCK), new FundInfo(type: FundInfo.FundType.HK_STOCK)]
        }
        def registry = Mock(MeterRegistry) {
            timer(_) >> Mock(Timer) {
                record(_) >> { Runnable runnable -> runnable.run() }
            }
        }
        def fxRateDao = Mock(FxRateDao) {
            1 * saveFxRates(_) >> { }
        }
        def fxRateService = Spy(FxRateService) {
            loadCurrencies(["HKD"]) >> [
                    new FxRateInfo("HKD", 0.92317d, "2025-05-14"),
                    FxRateInfo.RMB
            ]
        }
        fxRateService.setFundDataService(fundDataService)
        fxRateService.setMeterRegistry(registry)
        fxRateService.setFxRateDao(fxRateDao)

        when:
        fxRateService.updateFxRate()

        then:
        with (fxRateService.getFxRate("HKD")) {
            currency() == "HKD"
            fxRate() == 0.92317d
            asOfTime() == "2025-05-14"
        }
        fxRateService.getFxRate("RMB") == FxRateInfo.RMB
    }

    def "updateFxRate with exception"() {
        given:
        def fundDataService = Mock(FundDataService) {
            getAllFunds() >> { throw new RuntimeException() }
        }
        def fxRateService = new FxRateService()
        fxRateService.setFundDataService(fundDataService)

        when:
        fxRateService.updateFxRate()

        then:
        !fxRateService.getFxRate("HKD")
        !fxRateService.getFxRate("RMB")
    }

    def "init"() {
        given:
        def fxRateDao = Mock(FxRateDao) {
            1 * loadFxRates() >> [
                        new FxRateInfo("HKD", 0.92317d, "2025-05-14"),
                        FxRateInfo.RMB
            ]
        }
        def fxRateService = new FxRateService()
        fxRateService.setFxRateDao(fxRateDao)

        when:
        fxRateService.init()

        then:
        with (fxRateService.getFxRate("HKD")) {
            currency() == "HKD"
            fxRate() == 0.92317d
            asOfTime() == "2025-05-14"
        }
        fxRateService.getFxRate("RMB") == FxRateInfo.RMB
    }
}
