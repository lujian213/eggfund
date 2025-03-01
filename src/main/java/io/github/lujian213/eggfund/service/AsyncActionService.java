package io.github.lujian213.eggfund.service;

import io.github.lujian213.eggfund.dao.FundDao;
import io.github.lujian213.eggfund.exception.EggFundException;
import io.github.lujian213.eggfund.model.DateRange;
import io.github.lujian213.eggfund.model.FundInfo;
import io.github.lujian213.eggfund.model.FundRTValue;
import io.github.lujian213.eggfund.model.FundValue;
import io.github.lujian213.eggfund.utils.Constants;
import io.github.lujian213.eggfund.utils.TableHelper;
import io.github.lujian213.eggfund.utils.WebElementHelper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AsyncActionService {
    public static class RateValueVisitor extends TableHelper.DoubleValueVisitor {
        @Override
        protected Double transform(String str) {
            if (str.endsWith("%")) {
                str = str.substring(0, str.length() - 1);
            }
            return super.transform(str) / 100;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(AsyncActionService.class);
    private static final String FUND_VALUE_URL = "https://fundf10.eastmoney.com/F10DataApi.aspx?type=lsjz&code=%s&page=1&per=30&sdate=%s&edate=%s";
    private static final Pattern TABLE_PATTERN = Pattern.compile("[^<]*(<table .*< /table>) .* ");
    private static final String FUND_RT_VALUE_URL = "https://fundgz.1234567.com.cn/js/%s.js?v-%s";
    private static final Pattern FUND_RT_VALUE_PATTERN = Pattern.compile(".* \"gsz\":\"([^\"]+)\" .* \"gszzl\":\"([\"]+)\".*\"gztime\":\"([^\"]+)\".*");

    private FundDao fundDao;
    private Timer updateFundRTValueTimer;
    private Timer updateFundValueTimer;
    private RestTemplate restTemplate;

    @Autowired
    public void setFundDao(FundDao fundDao) {
        this.fundDao = fundDao;
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Autowired
    public void setMeterRegistry(MeterRegistry meterRegistry) {
        updateFundRTValueTimer = meterRegistry.timer("eggfund.updateFundRTValue");
        updateFundValueTimer = meterRegistry.timer("eggfund.updateFundValue");
    }

    @Async("fundValueRetrieveExecutor")
    public CompletableFuture<Void> updateFundValues(FundInfo fundInfo, DateRange range, FundValueListener listener) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            log.info("start update fund values for {}", fundInfo.getId());
            LocalDate current = range.from().withDayOfMonth(1);
            while (!current.isAfter(range.to())) {
                String month = current.format(Constants.MONTH_FORMAT);
                LocalDate date = current;
                List<FundValue> fundValues = updateFundValueTimer.record(() ->
                        loadFundValue(fundInfo.getId(), date, date.plusMonths(1).minusDays(1)));
                log.info("update {} fund values for {} in {}", Optional.ofNullable(fundValues).map(List::size).orElse(0), fundInfo.getId(), month);
                synchronized (this) {
                    fundDao.saveFundValues(fundInfo.getId(), month, fundValues);
                }
                listener.onFundValueChange(fundInfo.getId(), month, fundValues);
                current = current.plusMonths(1);
            }
            log.info("end update fund values for {}", fundInfo.getId());
            future.complete(null);
        } catch (Exception e) {
            future.completeExceptionally(new EggFundException("update fund value error", e));
        }
        return future;
    }

    @Async("fundValueRetrieveExecutor")
    public CompletableFuture<Map<String, FundRTValue>> getFundRTValueInBatch(String... codes) {
        if (log.isInfoEnabled()) {
            log.info("load fund real time value {}", Arrays.toString(codes));
        }
        Map<String, FundRTValue> ret = new HashMap<>();
        Arrays.stream(codes).forEach(code ->
                ret.put(code, updateFundRTValueTimer.record(
                        () -> getFundRTValue(code)
                ))
        );
        return CompletableFuture.completedFuture(ret);
    }

    protected FundRTValue getFundRTValue(String code) {
        try {
            String url = String.format(FUND_RT_VALUE_URL, code, LocalDateTime.now(Constants.ZONE_ID).format(Constants.DATE_TIME_FORMAT));
            log.info("load fund {} real time value from {}", code, url);
            ResponseEntity<String> response = restTemplate.getForEntity(new URI(url), String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                String content = response.getBody();
                return extractFundRTValue(content, code);
            }
            throw new IOException("load fund real time value failed with code " + response.getStatusCode());
        } catch (Exception e) {
            log.error("load fund {} real time value failed", code, e);
            return null;
        }
    }

    protected List<FundValue> loadFundValue(String code, LocalDate from, LocalDate to) throws EggFundException {
        try {
            List<FundValue> ret = new ArrayList<>();
            String url = String.format(FUND_VALUE_URL, code, from.format(Constants.DATE_FORMAT), to.format(Constants.DATE_FORMAT));
            log.info("load fund value from {}", url);
            ResponseEntity<String> response = restTemplate.getForEntity(new URI(url), String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                String content = response.getBody();
                ret.addAll(extractFundValue(content));
            } else {
                log.error("load fund value failed with code {}", response.getStatusCode());
                throw new EggFundException("load fund value failed with code " + response.getStatusCode());
            }
            return ret;
        } catch (URISyntaxException e) {
            throw new EggFundException("load fund value failed", e);
        }
    }

    protected FundRTValue extractFundRTValue(String content, String code) {
        Matcher matcher = FUND_RT_VALUE_PATTERN.matcher(content);
        if (matcher.matches()) {
            double value = Double.parseDouble(matcher.group(1));
            double change = Double.parseDouble(matcher.group(2)) / 100;
            String time = matcher.group(3);
            log.info("fund {} real time value found", code);
            return new FundRTValue(time, value, change);
        } else {
            log.warn("fund {} may not support real time value", code);
            return null;
        }
    }

    protected List<FundValue> extractFundValue(String content) {
        List<FundValue> ret = new ArrayList<>();
        Matcher matcher = TABLE_PATTERN.matcher(content);
        if (matcher.matches()) {
            String tableStr = matcher.group(1);
            WebElement element = WebElementHelper.load(tableStr);
            List<TableHelper.ColumnDataExtractor<?>> extractors = new ArrayList<>();
            extractors.add(new TableHelper.ColumnDataExtractor<>("date", "//td[1]", new TableHelper.StringValueVisitor()));
            extractors.add(new TableHelper.ColumnDataExtractor<>("unitValue", "//td[2]", new TableHelper.DoubleValueVisitor()));
            extractors.add(new TableHelper.ColumnDataExtractor<>("accumulatedValue", "//td[3]", new TableHelper.DoubleValueVisitor()));
            extractors.add(new TableHelper.ColumnDataExtractor<>("increaseRate", "//td[4]", new RateValueVisitor()));
            TableHelper.getTable(element, extractors).forEach(map -> {
                String date = (String) map.get("date");
                Double unitValue = (Double) map.get("unitValue");
                Double accumulatedValue = (Double) map.get("accumulatedValue");
                Double increaseRate = (Double) map.get("increaseRate");
                if (date != null && unitValue != null && accumulatedValue != null && increaseRate != null) {
                    ret.add(new FundValue(date, unitValue, accumulatedValue, increaseRate));
                }
            });
        }
        return ret;
    }
}