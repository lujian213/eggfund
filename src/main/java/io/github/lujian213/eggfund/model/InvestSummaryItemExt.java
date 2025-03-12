package io.github.lujian213.eggfund.model;

import io.github.lujian213.eggfund.utils.CommonUtil;

public class InvestSummaryItemExt extends InvestSummaryItem {
    private double leftQuota;

    public InvestSummaryItemExt(InvestSummaryItem item) {
        super(item);
        this.leftQuota = item.getQuota();
    }

    public double leftQuota() {
        return leftQuota;
    }

    public InvestSummaryItemExt setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public InvestSummaryItemExt setQuota(double quota) {
        this.investAmt = this.investAmt/this.quota * quota;
        this.fee = this.fee/this.quota * quota;
        this.earning = this.earning/this.quota * quota;
        this.quota = quota;
        return this;
    }

    public InvestSummaryItemExt setLeftQuota(double leftQuota) {
        this.leftQuota = leftQuota;
        return this;
    }

    public double clear(double quota) {
        double left = this.leftQuota + quota;
        if (left < 0 || CommonUtil.isZero(left, 0.001)) {
            this.leftQuota = 0;
            this.enabled = false;
        } else {
            this.leftQuota = left;
        }
        return left;
    }

    @Override
    public String toString() {
        return "InvestSummaryItemExt{" +
                super.toString() +
                ", leftQuota=" + leftQuota +
                '}';
    }
}
