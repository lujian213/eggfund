package io.github.lujian213.eggfund.model;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class InvestSummaryItem extends LocalDateRelated<InvestSummaryItem> {
    private String type;
    protected double quota;
    private double price;
    private double increaseRate;
    protected double fee;
    protected double investAmt;
    private double _var;
    protected double earning;
    private int index = 0;
    private int batch = 0;
    protected boolean enabled = true;
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
        this.price = item.price;
        this.increaseRate = item.increaseRate;
        this.fee = item.fee;
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
        this.fee = invest.getFee();
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

    public double getPrice() {
        return price;
    }

    public double getIncreaseRate() {
        return increaseRate;
    }

    public double getFee() {
        return fee;
    }

    public double getInvestAmt() {
        return investAmt;
    }

    public double getVar() {
        return _var;
    }

    public double getEarning() {
        return earning;
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

    @Override
    public String toString() {
        return "InvestSummaryItem{" +
                "type='" + type + '\'' +
                ", quota=" + quota +
                ", price=" + price +
                ", increaseRate=" + increaseRate +
                ", fee=" + fee +
                ", investAmt=" + investAmt +
                ", var=" + _var +
                ", earning=" + earning +
                ", index=" + index +
                ", batch=" + batch +
                ", enabled=" + enabled +
                ", investId='" + investId + '\"' +
                ", comments='" + comments + '\"' +
                '}';
    }
}