package io.github.lujian213.eggfund.service;

import io.github.lujian213.eggfund.dao.FundDao;
import io.github.lujian213.eggfund.exception.EggFundException;
import io.github.lujian213.eggfund.model.DateRange;
import io.github.lujian213.eggfund.model.FundInfo;
import io.github.lujian213.eggfund.model.FundRTValue;
import io.github.lujian213.eggfund.model.FundValue;
import io.github.lujian213.eggfund.service.loader.FundInfoLoader;
import io.github.lujian213.eggfund.service.loader.FundInfoLoaderFactory;
import io.github.lujian213.eggfund.utils.Constants;
import io.github.lujian213.eggfund.utils.TableHelper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;

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

    private FundDao fundDao;
    private Timer updateFundRTValueTimer;
    private Timer updateFundValueTimer;

    @Autowired
    public void setFundDao(FundDao fundDao) {
        this.fundDao = fundDao;
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
                        getFundInfoLoader(fundInfo).loadFundValue(fundInfo.getId(), date, date.plusMonths(1).minusDays(1)));
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
    public CompletableFuture<Map<String, FundRTValue>> getFundRTValueInBatch(FundInfo... funds) {
        if (log.isInfoEnabled()) {
            log.info("load fund real time value {}", Arrays.toString(funds));
        }
        Map<String, FundRTValue> ret = new HashMap<>();
        Arrays.stream(funds).forEach(fund ->
                ret.put(fund.getId(), updateFundRTValueTimer.record(
                        () -> getFundInfoLoader(fund).getFundRTValue(fund.getId())
                ))
        );
        return CompletableFuture.completedFuture(ret);
    }

    protected FundInfoLoader getFundInfoLoader(FundInfo fundInfo) {
        return FundInfoLoaderFactory.getInstance().getFundInfoLoader(fundInfo);
    }
}