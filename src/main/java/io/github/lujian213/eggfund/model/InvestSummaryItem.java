package io.github.lujian213.eggfund.model;

import io.github.lujian213.eggfund.utils.CommonUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@SuppressWarnings({"squid:S100", "squid:S116"})
public class InvestSummaryItem extends LocalDateRelated<InvestSummaryItem> {
    private String type;
    private double quota;
    private double liquidatedQuota;
    private double price;
    private double increaseRate;
    private double fee;
    private double tax;
    private double fxRate;
    private double investAmt;
    private double _var;
    private double earning;
    private int index = 0;
    private int batch = 0;
    private boolean enabled = true;
    private String investId;
    private String comments;

    public InvestSummaryItem(@Nonnull FundValue fundValue, @Nullable Invest invest, double estPrice) {
        super(fundValue.getDay());
        if (invest != null) {
            copyFrom(invest, estPrice);
            this.investAmt = invest.amount(fundValue.getUnitValue());
        }
        this.price = fundValue.getUnitValue();
        this.increaseRate = fundValue.getIncreaseRate();
        if (quota > 0 && invest != null) {
            this._var = (estPrice - price) / price;
            this.earning = -invest.amount(fundValue.getUnitValue()) * _var;
        }
    }

    public InvestSummaryItem(@Nonnull Invest invest, double estPrice) {
        super(invest.getDay());
        copyFrom(invest, estPrice);
    }

    public InvestSummaryItem(InvestSummaryItem item) {
        super(item.getDay());
        this.type = item.type;
        this.quota = item.quota;
        this.liquidatedQuota = item.liquidatedQuota;
        this.price = item.price;
        this.increaseRate = item.increaseRate;
        this.fee = item.fee;
        this.tax = item.tax;
        this.fxRate = item.fxRate;
        this.investAmt = item.investAmt;
        this._var = item._var;
        this.earning = item.earning;
        this.index = item.index;
        this.batch = item.batch;
        this.enabled = item.enabled;
        this.investId = item.investId;
        this.comments = item.comments;
    }

    protected void copyFrom(Invest invest, double estPrice) {
        this.type = invest.getType();
        this.price = invest.getUnitPrice();
        this.quota = invest.getShare();
        this.liquidatedQuota = 0;
        this.fee = invest.getFee();
        this.tax = invest.getTax();
        this.fxRate = invest.getFxRate();
        this.batch = invest.getBatch();
        this.investAmt = invest.getAmount();
        if (quota > 0) {
            this._var = (estPrice - price) / price;
            this.earning = -investAmt * _var;
        }
        this.index = invest.getUserIndex();
        this.enabled = invest.isEnabled();
        this.investId = invest.getId();
        this.comments = invest.getComments();
    }

    public double getQuota() {
        return quota;
    }

    public double getLiquidatedQuota() {
        return liquidatedQuota;
    }

    public double getPrice() {
        return price;
    }

    public double getIncreaseRate() {
        return increaseRate;
    }

    public double getFee() {
        return fee;
    }

    public double getTax() {
        return tax;
    }

    public double getFxRate() {
        return fxRate;
    }

    public double getInvestAmt() {
        if (quota + liquidatedQuota == 0) {
            return 0;
        }
        return investAmt / (quota + liquidatedQuota) * quota;
    }

    public double getVar() {
        return _var;
    }

    public double getEarning() {
        if (quota + liquidatedQuota == 0) {
            return 0;
        }
        return earning / (quota + liquidatedQuota) * quota;
    }

    public double getPrice_2pct() {
        return price * 1.02;
    }

    public int getIndex() {
        return index;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public InvestSummaryItem setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public double getPrice_minus2pct() {
        return price * 0.98;
    }

    public String getInvestId() {
        return investId;
    }

    public String getType() {
        return type;
    }

    public int getBatch() {
        return batch;
    }

    public String getComments() {
        return comments;
    }

    public void liquidate(InvestSummaryItem sellItem) {
        if (!Invest.TYPE_TRADE.equals(sellItem.type) || sellItem.quota >= 0) {
            throw new IllegalArgumentException("liquidate quota should be negative");
        }
        if (!Invest.TYPE_TRADE.equals(type) || !enabled || quota <= 0) {
            return;
        }
        double diff = quota + sellItem.quota;
        if (diff > 0) {
            liquidatedQuota += -sellItem.quota;
            quota -= -sellItem.quota;
            sellItem.liquidatedQuota += sellItem.quota;
            sellItem.quota = 0;
            sellItem.enabled = false;
        } else {
            sellItem.liquidatedQuota -= quota;
            sellItem.quota += quota;
            if (CommonUtil.isZero(diff, 0.001)) {
                sellItem.enabled = false;
            }
            liquidatedQuota += quota;
            quota = 0;
            enabled = false;
        }
    }

    @Override
    public String toString() {
        return "InvestSummaryItem{" +
                "type='" + type + '\'' +
                ", quota=" + quota +
                ", liquidatedQuota=" + liquidatedQuota +
                ", price=" + price +
                ", increaseRate=" + increaseRate +
                ", fee=" + fee +
                ", tax=" + tax +
                ", fxRate=" + fxRate +
                ", investAmt=" + investAmt +
                ", _var=" + _var +
                ", earning=" + earning +
                ", index=" + index +
                ", batch=" + batch +
                ", enabled=" + enabled +
                ", investId='" + investId + '\'' +
                ", comments='" + comments + '\'' +
                '}';
    }
}