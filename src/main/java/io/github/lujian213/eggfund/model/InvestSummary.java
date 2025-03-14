package io.github.lujian213.eggfund.model;

import io.github.lujian213.eggfund.clearance.ClearanceAlg;
import io.github.lujian213.eggfund.clearance.FIFOClearanceAlg;
import io.github.lujian213.eggfund.clearance.FILOClearanceAlg;
import io.github.lujian213.eggfund.exception.EggFundException;
import io.github.lujian213.eggfund.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.github.lujian213.eggfund.utils.CommonUtil.isZero;

public class InvestSummary {
    public record PriceTableItem(double rate, double price) {
    }

    private static final Logger log = LoggerFactory.getLogger(InvestSummary.class);

    private String fundId;
    private double totalLongAmt = 0;
    private double totalShortAmt = 0;
    private double totalLongQuota = 0;
    private double totalShortQuota = 0;
    private double totalDividendAmt = 0;
    private FundValue lastFundValue;
    private double raiseRate;
    private double estPrice;
    private double totalFee = 0;
    private final List<InvestSummaryItem> items = new ArrayList<>();
    private final Map<Double, Double> estPriceTable = new LinkedHashMap<>();
    private final Map<String, List<InvestSummaryItem>> clearanceMap = new LinkedHashMap<>();

    InvestSummary() {
        //for testing friendly
    }

    public InvestSummary(FundInfo fundInfo, List<FundValue> originFundValues, List<Invest> invests, FundRTValue rtValue, LocalDate endDate) {
        if (originFundValues.size() < 2) {
            throw new EggFundException("fund records are too few, at least 2, but was: " + originFundValues.size());
        }
        this.fundId = fundInfo.getId();
        List<FundValue> fundValues = prepareFundValues(originFundValues, endDate, rtValue);
        Map<String, FundValue> valueMap = fundValues.stream().collect(Collectors.toMap(FundValue::getDay, Function.identity()));
        invests.forEach(invest -> {
            FundValue value = valueMap.get(invest.getDay());
            double price;
            if (fundInfo.isEtf()) {
                price = invest.getUnitPrice();
            } else {
                price = (value == null ? -1 : value.getUnitValue());
            }
            if (Invest.TYPE_DIVIDEND.equals(invest.getType())) {
                totalDividendAmt += invest.amount(price);
            } else {
                if (invest.getShare() > 0) {
                    totalLongAmt += invest.amount(price);
                    totalLongQuota += invest.getShare();
                } else {
                    totalShortAmt += invest.amount(price);
                    totalShortQuota += invest.getShare();
                }
            }
            totalFee += invest.getFee();
        });
        totalLongAmt = Math.abs(totalLongAmt);
        totalShortAmt = Math.abs(totalShortAmt);
        totalShortQuota = Math.abs(totalShortQuota);
        totalDividendAmt = Math.abs(totalDividendAmt);

        prepareItems(fundInfo, invests, fundValues);
        prepareClearanceMap(items);
        estPriceTable.put(-0.05, lastFundValue.getUnitValue() * 0.95);
        estPriceTable.put(-0.04, lastFundValue.getUnitValue() * 0.96);
        estPriceTable.put(-0.02, lastFundValue.getUnitValue() * 0.98);
        estPriceTable.put(-0.01, lastFundValue.getUnitValue() * 0.99);
        estPriceTable.put(0.01, lastFundValue.getUnitValue() * 1.01);
        estPriceTable.put(0.02, lastFundValue.getUnitValue() * 1.02);
        estPriceTable.put(0.04, lastFundValue.getUnitValue() * 1.04);
        estPriceTable.put(0.05, lastFundValue.getUnitValue() * 1.05);
    }

    void prepareClearanceMap(List<InvestSummaryItem> items) {
        ClearanceAlg.ALL_ALGS.forEach(alg -> clearanceMap.put(alg.getAlgName(), alg.clear(items)));
    }

    List<FundValue> prepareFundValues(List<FundValue> originFundValues, LocalDate endDate, FundRTValue rtValue) {
        lastFundValue = originFundValues.get(originFundValues.size() - 1);
        List<FundValue> fundValues = new ArrayList<>(originFundValues);
        if (endDate.isAfter(lastFundValue.date()) && rtValue.date().isAfter(lastFundValue.date())) {
            raiseRate = rtValue.getIncreaseRate();
            estPrice = lastFundValue.getUnitValue() * (1 + raiseRate);
            FundValue estValue = new FundValue().setDay(endDate.format(Constants.DATE_FORMAT)).setIncreaseRate(raiseRate).setUnitValue(estPrice);
            fundValues.add(estValue);
        } else {
            log.info("already have fund record for end date: {}, use it directly", endDate);
            raiseRate = lastFundValue.getIncreaseRate();
            estPrice = lastFundValue.getUnitValue();
            lastFundValue = fundValues.get(fundValues.size() - 2);
        }
        return fundValues;
    }

    private void prepareItems(FundInfo fundInfo, List<Invest> invests, List<FundValue> fundValues) {
        if (fundInfo.isEtf()) {
            invests.forEach(invest -> items.add(new InvestSummaryItem(invest, estPrice)));
        } else {
            //group invests by day
            Map<String, List<Invest>> investDayMap = invests.stream().collect(Collectors.groupingBy(Invest::getDay));

            boolean hasInvest = false;
            for (FundValue fundValue : fundValues) {
                List<Invest> investList = investDayMap.get(fundValue.getDay());
                if (investList != null || hasInvest) {
                    hasInvest = true;
                    if (investList == null) {
                        items.add(new InvestSummaryItem(fundValue, null, estPrice));
                    } else {
                        investList.stream().sorted(Comparator.comparing(Invest::getUserIndex)).forEach(
                                invest -> items.add(new InvestSummaryItem(fundValue, invest, estPrice))
                        );
                    }
                }
            }
        }
    }

    public String getFundId() {
        return fundId;
    }

    public double getTotalShortQuota() {
        return totalShortQuota;
    }

    public double getTotalLongQuota() {
        return totalLongQuota;
    }

    public double getTotalShortAmt() {
        return totalShortAmt;
    }

    public double getTotalLongAmt() {
        return totalLongAmt;
    }

    public double getNetAmt() {
        return totalShortAmt - totalLongAmt;
    }

    public double getNetQuota() {
        return totalLongQuota - totalShortQuota;
    }

    public double getEstPrice() {
        return estPrice;
    }

    public double getRaiseRate() {
        return raiseRate;
    }

    public double getLastUnitValue() {
        return this.lastFundValue.getUnitValue();
    }

    public String getLastFundDate() {
        return this.lastFundValue.getDay();
    }

    public double getPredictedValue() {
        return getEstPrice() * getNetQuota();
    }

    public double getTotalFee() {
        return totalFee;
    }

    public double getEarning() {
        return getPredictedValue() + getNetAmt();
    }

    public double getEarningRate() {
        if (getNetQuota() > 0 && !isZero(getTotalLongAmt(), 0.001)) {
            return getEarning() / getTotalLongAmt();
        } else {
            return 0;
        }
    }

    public double getGrossEarning() {
        return getEarning() + getTotalFee();
    }

    public double getGrossEarningRate() {
        if (getNetQuota() > 0 && !isZero(getTotalLongAmt(), 0.001)) {
            return getGrossEarning() / getTotalLongAmt();
        } else {
            return 0;
        }
    }

    public double getAveragePrice() {
        if (!isZero(totalLongQuota, 0.001)) {
            return totalLongAmt / totalLongQuota;
        }
        return 0;
    }

    public double getAverageUnitValue() {
        if (!isZero(getNetQuota(), 0.001)) {
            return Math.abs(getNetAmt()) / getNetQuota();
        }
        return 0;
    }

    public List<InvestSummaryItem> getItems() {
        return items;
    }

    public Map<String, List<InvestSummaryItem>> getClearanceMap() {
        return clearanceMap;
    }

    public Map<Double, Double> getEstPriceTable() {
        return estPriceTable;
    }

    public List<PriceTableItem> getEstPriceTableItems() {
        return estPriceTable.entrySet().stream().map(entry -> new PriceTableItem(entry.getKey(), entry.getValue())).toList();
    }

    public double getTotalDividendAmt() {
        return totalDividendAmt;
    }
}
