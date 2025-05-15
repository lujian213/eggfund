package io.github.lujian213.eggfund.service;

import io.github.lujian213.eggfund.dao.FundDao;
import io.github.lujian213.eggfund.exception.EggFundException;
import io.github.lujian213.eggfund.model.DateRange;
import io.github.lujian213.eggfund.model.FundInfo;
import io.github.lujian213.eggfund.model.FundRTValue;
import io.github.lujian213.eggfund.model.FundValue;
import io.github.lujian213.eggfund.service.loader.FundInfoLoaderFactory;
import io.github.lujian213.eggfund.utils.Constants;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class FundDataService implements FundValueListener {
    record FundRTValueHistory(String day, Map<String, FundRTValue> rtValueMap) {
        public FundRTValue getLatestRTValue() {
            return rtValueMap.values().stream().max(Comparator.comparing(FundRTValue::getTime)).orElse(null);
        }

        public void addRTValue(FundRTValue rtValue) {
            rtValueMap.put(rtValue.getTime(), rtValue);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(FundDataService.class);
    final Map<String, Map<String, List<FundValue>>> fundValueMap = new HashMap<>();
    Map<String, FundInfo> fundInfoMap = new HashMap<>();
    final Map<String, FundRTValueHistory> fundRTValueHistoryMap = new ConcurrentHashMap<>();

    private FundDao fundDao;
    private AsyncActionService asyncActionService;
    private Timer loadFundNameTimer;

    @Autowired
    public void setFundDao(FundDao fundDao) {
        this.fundDao = fundDao;
    }

    @Autowired
    public void setAsyncActionService(AsyncActionService asyncActionService) {
        this.asyncActionService = asyncActionService;
    }

    @Autowired
    public void setMeterRegistry(MeterRegistry meterRegistry) {
        loadFundNameTimer = meterRegistry.timer("eggfund. loadFundName");
    }

    @PostConstruct
    protected void init() throws IOException {
        fundInfoMap = fundDao.loadFundInfo().stream().collect(Collectors.toMap(FundInfo::getId, Function.identity()));
        try {
            fundInfoMap.values().forEach(fundInfo -> {
                try {
                    fundValueMap.put(fundInfo.getId(), fundDao.loadFundValues(fundInfo.getId()));
                } catch (Exception e) {
                    throw new EggFundException("load fund value for " + fundInfo.getId() + " failed", e);
                }
            });
            log.info("init fund data retriever with {} funds", fundInfoMap.size());
        } catch (EggFundException e) {
            throw new IOException(e);
        }
    }

    public List<FundValue> getFundValues(String code, DateRange range) {
        synchronized (this) {
            checkFund(code);
            Map<String, List<FundValue>> valueMap = fundValueMap.computeIfAbsent(code, k -> new HashMap<>());
            List<FundValue> ret = new ArrayList<>();
            valueMap.values().forEach(ret::addAll);
            return ret.stream().filter(value -> range.inRange(value.date())).
                    sorted(Comparator.comparing(FundValue::date)).
                    toList();
        }
    }

    public Map<String, FundRTValue> getFundRTValues(List<String> codes) {
        Map<String, FundRTValue> ret = new HashMap<>();
        synchronized (this) {
            codes.stream().filter(fundInfoMap::containsKey).forEach(code ->
                    ret.put(code, Optional.ofNullable(fundRTValueHistoryMap.get(code))
                            .map(FundRTValueHistory::getLatestRTValue)
                            .orElseGet(() -> getBackupFundRTValue(code)))
            );
            return ret;
        }
    }

    public FundValue getFundValue(String code, LocalDate date) {
        synchronized (this) {
            checkFund(code);
            Map<String, List<FundValue>> valueMap = fundValueMap.computeIfAbsent(code, k -> new HashMap<>());
            String month = Constants.MONTH_FORMAT.format(date);
            List<FundValue> values = valueMap.get(month);
            FundValue ret;
            if (values != null) {
                ret = values.stream().filter(value -> value.date().equals(date)).findFirst().orElse(null);
            } else {
                ret = null;
            }
            if (ret == null) {
                updateFundValues(code, new DateRange(date.withDayOfMonth(1), date));
            }
            return ret;
        }
    }

    public void updateFundValues(String code, DateRange range) {
        synchronized (this) {
            asyncActionService.updateFundValues(checkFund(code), range, this);
        }
    }

    public List<FundInfo> getAllFunds() {
        synchronized (this) {
            return fundInfoMap.values().stream().sorted(Comparator.comparing(FundInfo::getPriority)).toList();
        }
    }

    public FundInfo addNewFund(FundInfo fundInfo) {
        String code = fundInfo.getId();
        synchronized (this) {
            if (fundInfoMap.containsKey(code)) {
                throw new EggFundException("Fund code already exists: " + code);
            }
            try {
                fundInfo.setName(loadFundNameTimer.record(() -> loadFundName(fundInfo)));
                Map<String, FundInfo> newFundMap = new HashMap<>(fundInfoMap);
                newFundMap.put(code, fundInfo);
                fundDao.saveFunds(newFundMap.values());
                fundInfoMap.put(fundInfo.getId(), fundInfo);
                LocalDate now = LocalDate.now(Constants.ZONE_ID);
                asyncActionService.updateFundValues(fundInfo, new DateRange(now.minusMonths(2).withDayOfMonth(1), now), this);
                asyncActionService.getFundRTValueInBatch(fundInfo);
                return fundInfo;
            } catch (IOException e) {
                throw new EggFundException("add new fund error: " + code, e);
            }
        }
    }

    public FundInfo updateFund(FundInfo fundInfo) {
        String code = fundInfo.getId();
        synchronized (this) {
            FundInfo existing = checkFund(code);
            fundInfo.setName(existing.getName());
            try {
                Map<String, FundInfo> newFundMap = new HashMap<>(fundInfoMap);
                newFundMap.put(code, fundInfo);
                fundDao.saveFunds(newFundMap.values());
                existing.update(fundInfo);
                return existing;
            } catch (IOException e) {
                throw new EggFundException("update fund error: " + code, e);
            }
        }
    }

    public void deleteFund(String code) {
        synchronized (this) {
            checkFund(code);
            try {
                Map<String, FundInfo> newFundMap = new HashMap<>(fundInfoMap);
                newFundMap.remove(code);
                fundDao.saveFunds(newFundMap.values());
                fundDao.deleteFundValues(code);
                fundInfoMap.remove(code);
                fundValueMap.remove(code);
            } catch (IOException e) {
                throw new EggFundException("delete fund error: " + code, e);
            }
        }
    }

    public FundInfo checkFund(String code) {
        synchronized (this) {
            return Optional.ofNullable(fundInfoMap.get(code))
                    .orElseThrow(() -> new EggFundException("Fund code not found: " + code));
        }
    }

    public FundInfo findFund(String code) {
        synchronized (this) {
            return fundInfoMap.get(code);
        }
    }

    protected String loadFundName(FundInfo fundInfo) {
        return FundInfoLoaderFactory.getInstance().getFundInfoLoader(fundInfo).loadFundName(fundInfo.getId());
    }

    @Override
    public void onFundValueChange(String fundCode, String month, List<FundValue> fundValues) {
        log.info("fund value changed for {} in {}", fundCode, month);
        synchronized (this) {
            Map<String, List<FundValue>> valueMap = fundValueMap.computeIfAbsent(fundCode, k -> new HashMap<>());
            valueMap.put(month, fundValues);
        }
    }

    @Scheduled(cron = "0 0 */1 * * *")
    public void updateFundsValues() {
        log.info("update funds values in scheduled time");
        LocalDate now = LocalDate.now(Constants.ZONE_ID);
        synchronized (this) {
            fundInfoMap.values().forEach(fund ->
                    asyncActionService.updateFundValues(fund, new DateRange(now.minusMonths(1), now), this)
                            .exceptionally(e -> {
                                log.error("update fund values failed for {}", fund.getId(), e);
                                return null;
                            }));
        }
    }

    @Scheduled(cron = "0 */3 9-15 * * MON-FRI", zone = "${zone.id:Asia/Shanghai}")
    public void updateFundRTValues() {
        log.info("Scheduled task started at {}", LocalDateTime.now());
        try {
            List<FundInfo> fundList;
            synchronized (this) {
                fundList = new ArrayList<>(fundInfoMap.values());
            }
            List<FundInfo[]> fundBatches = groupToBatches(FundInfo.class, fundList, 5, 3);
            fundBatches.forEach(batch -> asyncActionService.getFundRTValueInBatch(batch).
                    thenApply(this::handleBatchRTValues).exceptionally(e -> {
                        log.error("Exception occurred while fetching fund real time value", e);
                        return null;
                    })
            );
        } catch (Exception e) {
            log.error("Scheduled task failed", e);
        }
        log.info("Scheduled task ended at {}", LocalDateTime.now());
    }

    Map<String, FundRTValue> handleBatchRTValues(Map<String, FundRTValue> batchResult) {
        synchronized (this) {
            batchResult.forEach((code, fundRTValue) -> {
                if (fundRTValue != null) {
                    log.info("fund real time value for {}: {}", code, fundRTValue.getTime());
                    Optional.ofNullable(fundRTValueHistoryMap.get(code)).ifPresentOrElse(
                            history -> {
                                if (history.day.equals(fundRTValue.getDay())) {
                                    history.addRTValue(fundRTValue);
                                } else {
                                    fundRTValueHistoryMap.put(code, new FundRTValueHistory(fundRTValue.getDay(), new HashMap<>(Map.of(fundRTValue.getTime(), fundRTValue))));
                                }
                            },
                            () -> fundRTValueHistoryMap.put(code, new FundRTValueHistory(fundRTValue.getDay(), new HashMap<>(Map.of(fundRTValue.getTime(), fundRTValue))))
                    );
                } else {
                    log.warn("Exception occurred while fetching fund real time value for {}, try to use latest fund value", code);
                }
            });
        }
        return batchResult;
    }

    FundRTValue getBackupFundRTValue(String code) {
        log.info("fall back to previous day's fund value for {}", code);
        return Optional.ofNullable(getLatestFundValue(code)).map(value -> new FundRTValue(value.getDay() + " 15:00", value.getUnitValue(), value.getIncreaseRate())).orElse(null);
    }

    FundValue getLatestFundValue(String code) {
        synchronized (this) {
            checkFund(code);
            Map<String, List<FundValue>> valueMap = fundValueMap.computeIfAbsent(code, k -> new HashMap<>());
            for (String month : valueMap.keySet().stream().sorted(Comparator.reverseOrder()).toList()) {
                List<FundValue> values = valueMap.get(month);
                if (values != null && !values.isEmpty()) {
                    return values.stream().max(Comparator.comparing(FundValue::date)).orElse(null);
                }
            }
            return null;
        }
    }

    static <T> List<T[]> groupToBatches(Class<T> clazz, List<T> objList, int maxBatches, int minBatchSize) {
        List<T[]> ret = new ArrayList<>();
        int batchSize = (int) Math.ceil(Math.max((double) objList.size() / maxBatches, minBatchSize));
        for (int i = 0; i < objList.size(); i += batchSize) {
            ret.add(objList.subList(i, Math.min(i + batchSize, objList.size())).toArray((T[])Array.newInstance(clazz, 0)));
        }
        return ret;
    }

    public List<FundRTValue> getFundRTValueHistory(String code) {
        checkFund(code);
        synchronized (this) {
            List<FundRTValue> ret = Optional.ofNullable(fundRTValueHistoryMap.get(code))
                    .map(FundRTValueHistory::rtValueMap)
                    .map(Map::values)
                    .map(ArrayList::new)
                    .orElse(new ArrayList<>());
            ret.sort(Comparator.comparing(FundRTValue::getTime));
            return ret;
        }
    }
}