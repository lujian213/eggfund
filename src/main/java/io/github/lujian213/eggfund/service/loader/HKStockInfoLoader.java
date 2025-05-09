package io.github.lujian213.eggfund.service.loader;

import com.jayway.jsonpath.JsonPath;
import io.github.lujian213.eggfund.exception.EggFundException;
import io.github.lujian213.eggfund.model.FundInfo;
import io.github.lujian213.eggfund.model.FundRTValue;
import io.github.lujian213.eggfund.model.FundValue;
import io.github.lujian213.eggfund.utils.Constants;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class HKStockInfoLoader implements FundInfoLoader {
    private static final Logger log = LoggerFactory.getLogger(HKStockInfoLoader.class);
    private static final String STOCK_INFO_FILTER = "(SECURITY_CODE=\"%s\")(HOLD_DATE>='%s')(HOLD_DATE<='%s')";
    private static final String STOCK_INFO_URL = """
            https://datacenter-web.eastmoney.com/api/data/v1/get?sortColumns=HOLD_DATE&\
            sortTypes=-1&\
            pageSize=500&\
            pageNumber=1&\
            reportName=RPT_MUTUAL_HOLD_DET&\
            columns=ALL&\
            source=WEB&\
            client=WEB&\
            filter=%s""";
    private static final String STOCK_RT_VALUE_URL = "http://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHKStockData?page={page}&num={num}&sort={sort}&asc={asc}&node={node}&_s_r_a={sra}";
    static final long MAX_LOAD_INTERVAL = 3 * 60 * 1000;
    static final int MAX_PARALLEL_TASKS = 5;
    private RestTemplate restTemplate;
    private Executor executor;

    final Map<String, FundRTValue> rtValueMap = new ConcurrentHashMap<>();
    volatile long lastFundRTValueLodeTime = -1;

    public HKStockInfoLoader() {
        FundInfoLoaderFactory.getInstance().registerLoader(FundInfo.FundType.HK_STOCK, this);
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Autowired
    public void setExecutor(@Qualifier("fundValueRetrieveExecutor") Executor executor) {
        this.executor = executor;
    }

    @Override
    public String loadFundName(String code) {
        String to = LocalDateTime.now(Constants.ZONE_ID).format(Constants.DATE_FORMAT);
        String from = LocalDateTime.now(Constants.ZONE_ID).minusMonths(1).format(Constants.DATE_FORMAT);
        String filter = STOCK_INFO_FILTER.formatted(code, from, to);
        String url = STOCK_INFO_URL.formatted(URLEncoder.encode(filter, StandardCharsets.UTF_8));
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
        List<Map<String, Object>> items = extractStockItems(content);
        return (String) items.stream().findFirst().map(map -> map.get("SECURITY_NAME_ABBR")).orElse(null);
    }

    protected List<Map<String, Object>> extractStockItems(String content) {
        log.info("load stock info content: {}", content);
        try {
            return JsonPath.read(content, "$.result.data");
        } catch (Exception e) {
            log.error("load stock info failed", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<FundValue> loadFundValue(String code, LocalDate from, LocalDate to) throws EggFundException {
        try {
            List<FundValue> ret = new ArrayList<>();
            String filter = STOCK_INFO_FILTER.formatted(code, from.format(Constants.DATE_FORMAT), to.format(Constants.DATE_FORMAT));
            String url = STOCK_INFO_URL.formatted(URLEncoder.encode(filter, StandardCharsets.UTF_8));
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
        } catch (URISyntaxException | RestClientException e) {
            throw new EggFundException("load fund value failed", e);
        }
    }

    protected FundValue map2FundValue(Map<String, Object> map) {
        try {
            String date = Optional.ofNullable(map.get("HOLD_DATE")).map(v -> Constants.DATE_FORMAT.format(Constants.SECOND_FORMAT.parse((String) v))).orElse(null);
            Number unitValue = (Number) map.get("CLOSE_PRICE");
            Number increaseRate = (Number) map.get("CHANGE_RATE");
            if (date != null && unitValue != null && increaseRate != null) {
                return new FundValue(date, unitValue.doubleValue(), unitValue.doubleValue(), increaseRate.doubleValue());
            }
        } catch (Exception e) {
            log.error("map to fund value failed", e);
        }
        return null;
    }

    protected List<FundValue> extractFundValue(String content) {
        return new ArrayList<>(extractStockItems(content).stream().
                map(this::map2FundValue).
                filter(Objects::nonNull).
                collect(Collectors.toMap(
                        FundValue::getDay, // 以日期作为键
                        Function.identity(),
                        (existing, replacement) -> existing) // 如果有重复的日期，保留第一个)
                ).values());
    }

    @Override
    public FundRTValue getFundRTValue(String code) {
        if (System.currentTimeMillis() - lastFundRTValueLodeTime > MAX_LOAD_INTERVAL) {
            executor.execute(this::loadFundRTValues);
        }
        return rtValueMap.get(code);
    }

    protected void loadFundRTValues() {
        try {
            log.info("load fund real time value from {}", STOCK_RT_VALUE_URL);
            int page = 1;
            boolean allDone = false;
            while (!allDone) {
                Future<Integer>[] results = new Future[MAX_PARALLEL_TASKS];
                for (int i = 1; i <= MAX_PARALLEL_TASKS; i++) {
                    int finalPage = page++;
                    results[i - 1] = ((AsyncTaskExecutor) executor).submit(() -> loadFundRTValuesForPage(finalPage));
                }
                for (int i = 1; i <= MAX_PARALLEL_TASKS; i++) {
                    if (results[i - 1].get() == 0) {
                        allDone = true;
                    }
                }
            }
            lastFundRTValueLodeTime = System.currentTimeMillis();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.error("load fund real time value failed", e);
        }
    }

    protected int loadFundRTValuesForPage(int page) {
        Map<String, String> params = new HashMap<>(Map.of("page", "" + page,
                "num", "3000",
                "sort", "symbol",
                "asc", "1",
                "node", "qbgg_hk",
                "sra", "page"));
        log.info("load page {}", page);
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(STOCK_RT_VALUE_URL, String.class, params);
            if (response.getStatusCode() == HttpStatus.OK) {
                String content = response.getBody();
                Map<String, FundRTValue> rtValues = extractFundRTValue(content);
                rtValueMap.putAll(rtValues);
                return rtValues.size();
            } else {
                log.error("load fund real time value failed with code {}", response.getStatusCode());
                return 0;
            }
        } catch (Exception e) {
            log.error("load fund real time value failed", e);
            return 0;
        }
    }

    protected Map<String, FundRTValue> extractFundRTValue(String content) {
        List<Map<String, String>> records;
        try {
            records = Constants.MAPPER.readerForListOf(Map.class).readValue(content);
        } catch (Exception e) {
            log.error("extract fund real time value failed: {}", content, e);
            records = Collections.emptyList();
        }
        return records.stream()
                .map(item -> new Object[] {item.get("symbol"), this.map2FundRTValue(item)})
                .filter(entry -> entry[0] != null && entry[1] != null)
                .collect(Collectors.toMap(entry->(String)entry[0], entry->(FundRTValue)entry[1]));
    }

    protected FundRTValue map2FundRTValue(Map<String, String> map) {
        try {
            Double value = Optional.ofNullable(map.get("lasttrade")).map(Double::parseDouble).orElse(null);
            Double change = Optional.ofNullable(map.get("changepercent")).map(Double::parseDouble).orElse(null);
            String time = Optional.ofNullable(map.get("ticktime")).map(v -> Constants.MINUTE_FORMAT.format(Constants.SECOND_FORMAT2.parse(v))).orElse(null);
            if (time != null && value != null && change != null) {
                return new FundRTValue(time, value, change);
            }
        } catch (Exception e) {
            log.error("map to fund real time value failed: {}", map, e);
        }
        return null;
    }
}
