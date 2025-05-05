package io.github.lujian213.eggfund.service.loader;

import io.github.lujian213.eggfund.exception.EggFundException;
import io.github.lujian213.eggfund.model.FundInfo;
import io.github.lujian213.eggfund.model.FundRTValue;
import io.github.lujian213.eggfund.model.FundValue;
import io.github.lujian213.eggfund.service.AsyncActionService;
import io.github.lujian213.eggfund.utils.Constants;
import io.github.lujian213.eggfund.utils.TableHelper;
import io.github.lujian213.eggfund.utils.WebElementHelper;
import jakarta.annotation.Nullable;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LocalFundInfoLoader implements FundInfoLoader {
    private static final Logger log = LoggerFactory.getLogger(LocalFundInfoLoader.class);
    private static final String FUND_INFO_URL = "https://fund.eastmoney.com/pingzhongdata/%s.js?v-%s";
    private static final Pattern FSNAME_PATTERN = Pattern.compile(".*fS_name\\s*=\\s*\"([^\"]*)\".*");
    private static final String FUND_VALUE_URL = "https://fundf10.eastmoney.com/F10DataApi.aspx?type=lsjz&code=%s&page=1&per=30&sdate=%s&edate=%s";
    private static final Pattern TABLE_PATTERN = Pattern.compile("[^<]*(<table.*</table>).*");
    private static final String FUND_RT_VALUE_URL = "https://fundgz.1234567.com.cn/js/%s.js?v=%s";
    private static final Pattern FUND_RT_VALUE_PATTERN = Pattern.compile(".*\"gsz\":\"([^\"]+)\".*\"gszzl\":\"([^\"]+)\".*\"gztime\":\"([^\"]+)\".*");

    private RestTemplate restTemplate;

    public LocalFundInfoLoader() {
        FundInfoLoaderFactory.getInstance().registerLoader(FundInfo.FundType.LOCAL_FUND, this);
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String loadFundName(String code) {
        String url = String.format(FUND_INFO_URL, code, LocalDateTime.now(Constants.ZONE_ID).format(Constants.DATE_TIME_FORMAT));
        log.info("load fund info from {}", url);
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(new URI(url), String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                String name = extractFundName(response.getBody());
                if (name != null) {
                    return name;
                }
            }
            log.error("load fund info failed with code {}", response.getStatusCode());
            throw new EggFundException("load fund info failed with code " + response.getStatusCode());
        } catch (URISyntaxException | RestClientException e) {
            throw new EggFundException("load fund info error: " + code, e);
        }
    }

    @Nullable
    protected String extractFundName(String content) {
        log.info("load fund info content: {}", content);
        Matcher matcher = FSNAME_PATTERN.matcher(content);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    @Override
    public List<FundValue> loadFundValue(String code, LocalDate from, LocalDate to) throws EggFundException {
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
            extractors.add(new TableHelper.ColumnDataExtractor<>("increaseRate", "//td[4]", new AsyncActionService.RateValueVisitor()));
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

    @Override
    public FundRTValue getFundRTValue(String code) {
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

}
