package io.github.lujian213.eggfund.service;

import com.jayway.jsonpath.JsonPath;
import io.github.lujian213.eggfund.dao.FxRateDao;
import io.github.lujian213.eggfund.exception.EggFundException;
import io.github.lujian213.eggfund.model.FundInfo;
import io.github.lujian213.eggfund.model.FxRateInfo;
import io.github.lujian213.eggfund.utils.Constants;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FxRateService {
    private static final Logger log = LoggerFactory.getLogger(FxRateService.class);
    private static final String FX_RATE_URL = "https://query.sse.com.cn/commonSoaQuery.do?isPagination=true&updateDate=%s&updateDateEnd=%s&sqlId=FW_HGT_JSHDBL&pageHelp.cacheSize=1&pageHelp.pageSize=10000&pageHelp.pageNo=1&pageHelp.beginPage=1&pageHelp.endPage=1";

    final Map<String, FxRateInfo> fxRateMap = new ConcurrentHashMap<>();

    private FundDataService fundDataService;
    private FxRateDao fxRateDao;
    private Timer loadFxRateTimer;
    private RestTemplate restTemplate;

    @Autowired
    public void setFundDataService(FundDataService fundDataService) {
        this.fundDataService = fundDataService;
    }

    @Autowired
    public void setFxRateDao(FxRateDao fxRateDao) {
        this.fxRateDao = fxRateDao;
    }

    @Autowired
    public void setMeterRegistry(MeterRegistry meterRegistry) {
        loadFxRateTimer = meterRegistry.timer("eggfund.loadFxRate");
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    protected void init() throws IOException {
        List<FxRateInfo> fxRates = fxRateDao.loadFxRates();
        fxRates.forEach(fxRateInfo -> fxRateMap.put(fxRateInfo.currency(), fxRateInfo));
    }

    public FxRateInfo getFxRate(String currency) {
        return fxRateMap.get(currency);
    }

    @Scheduled(cron = "0 0 */3 * * MON-FRI", zone = "${zone.id:Asia/Shanghai}")
    public void updateFxRate() {
        log.info("start to update fx rate");
        try {
            List<String> currencies = fundDataService.getAllFunds().stream().map(FundInfo::getCurrency).distinct().toList();
            loadFxRateTimer.record(()->loadCurrencies(currencies).forEach(fxRateInfo -> fxRateMap.put(fxRateInfo.currency(), fxRateInfo)));
            fxRateDao.saveFxRates(fxRateMap.values());
        } catch (Exception e) {
            log.error("update fx rate failed", e);
        }
        log.info("end to update fx rate");
    }

    @Nonnull
    protected List<FxRateInfo> loadCurrencies(List<String> currencies) {
        try {
            List<FxRateInfo> ret = new ArrayList<>();
            LocalDate from = LocalDate.now(Constants.ZONE_ID).minusWeeks(1);
            LocalDate to = LocalDate.now(Constants.ZONE_ID);
            String url = FX_RATE_URL.formatted(from.format(Constants.DATE_FORMAT2), to.format(Constants.DATE_FORMAT2));
            log.info("load fx rate from {}", url);
            HttpHeaders headers = new HttpHeaders(MultiValueMap.fromSingleValue(
                    Map.of("Host", "query.sse.com.cn", "Referer", "https://www.sse.com.cn/")));
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(null, headers), String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                String content = response.getBody();
                ret.addAll(extractFxRate(content));
            } else {
                log.error("load fx rate failed with code {}", response.getStatusCode());
                throw new EggFundException("load fund value failed with code " + response.getStatusCode());
            }
            return ret;
        } catch (RestClientException e) {
            throw new EggFundException("load fx rate failed", e);
        }
    }

    protected List<FxRateInfo> extractFxRate(String content) {
        log.info("load fx rate content: {}", content);
        List<FxRateInfo> ret = new ArrayList<>();
        try {
            List<Map<String, Object>> data = JsonPath.read(content, "$.result");
            data.stream().map(this::map2FxRate).filter(Objects::nonNull).
                    max(Comparator.comparing(FxRateInfo::asOfTime)).stream().findAny().ifPresent(ret::add);
        } catch (Exception e) {
            log.error("load fx rate failed", e);
        }
        ret.add(FxRateInfo.RMB);
        return ret;
    }

    protected FxRateInfo map2FxRate(Map<String, Object> map) {
        try {
            String currency = (String) map.get("currencyType");
            double fxRate = Double.parseDouble((String) map.get("buyPrice"));
            String asOfTime = (String) map.get("updateDate");
            return new FxRateInfo(currency, fxRate, LocalDate.parse(asOfTime, Constants.DATE_FORMAT2).format(Constants.DATE_FORMAT));
        } catch (Exception e) {
            log.error("map to fx rate failed", e);
            return null;
        }
    }
}