package io.github.lujian213.eggfund.model;

import io.github.lujian213.eggfund.utils.CommonUtil;

import java.util.List;

public class InvestorSummary {

    private final String investorId;
    private double totalLongAmt;
    private double totalShortAmt;
    private double totalFee;
    private double totalTax;
    private double earning;
    private double grossEarning;
    private double netAmt;
    private double predictedValue;
    private final List<InvestSummary> investSummaryList;

    public InvestorSummary(String investorId, List<InvestSummary> investSummaryList) {
        investSummaryList.forEach(a -> a.getItems().clear());
        investSummaryList.forEach(a -> a.getEstPriceTable().clear());
        this.investorId = investorId;
        this.investSummaryList = investSummaryList;
        for (InvestSummary investSummary : investSummaryList) {
            totalLongAmt += investSummary.getTotalLongAmt();
            totalShortAmt += investSummary.getTotalShortAmt();
            totalFee += investSummary.getTotalFee();
            totalTax += investSummary.getTotalTax();
            earning += investSummary.getEarning();
            grossEarning += investSummary.getGrossEarning();
            netAmt += investSummary.getNetAmt();
            predictedValue += investSummary.getPredictedValue();
        }
    }

    public String getInvestorId() {
        return investorId;
    }

    public double getTotalLongAmt() {
        return totalLongAmt;

    }

    public double getTotalShortAmt() {
        return totalShortAmt;
    }

    public double getTotalFee() {
        return totalFee;
    }

    public double getTotalTax() {
        return totalTax;
    }

    public double getEarning() {
        return earning;
    }

    public double getEarningRate() {
        if (!CommonUtil.isZero(getTotalLongAmt(), 0.001)) {
            return getEarning() / getTotalLongAmt();
        } else {
            return 0;
        }
    }

    public double getGrossEarning() {
        return grossEarning;
    }

    public double getGrossEarningRate() {
        if (!CommonUtil.isZero(getTotalLongAmt(), 0.001)) {
            return getGrossEarning() / getTotalLongAmt();
        } else {
            return 0;
        }
    }

    public double getNetAmt() {
        return netAmt;
    }

    public double getPredictedValue() {
        return predictedValue;
    }

    public List<InvestSummary> getInvestSummaryList() {
        return investSummaryList;
    }
}