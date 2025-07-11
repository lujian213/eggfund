package io.github.lujian213.eggfund.model;

import java.util.Objects;

public class Invest extends LocalDateRelated<Invest> {
    public static final String TYPE_TRADE = "trade";
    public static final String TYPE_DIVIDEND = "dividend";

    private String type = TYPE_TRADE;
    private String id;
    private String code;
    private double share;
    private double unitPrice;
    private double totalSpend;
    private double fee;
    private double tax;
    private double fxRate = 1.0;
    private int userIndex = 0;
    private boolean enabled = true;
    private int batch = 0;
    private String comments;

    public Invest() {
    }

    public Invest(Invest invest) {
        super(invest.getDay());
        this.type = invest.type;
        this.id = invest.id;
        this.code = invest.code;
        this.share = invest.share;
        this.unitPrice = invest.unitPrice;
        this.fee = invest.fee;
        this.tax = invest.tax;
        this.fxRate = invest.fxRate;
        this.userIndex = invest.userIndex;
        this.enabled = invest.enabled;
        this.batch = invest.batch;
        this.comments = invest.comments;

    }

    public String getType() {
        return type;
    }

    public Invest setType(String type) {
        this.type = type;
        return this;
    }

    public String getCode() {
        return code;
    }

    public Invest setCode(String code) {
        this.code = code;
        return this;
    }

    public double getShare() {
        return share;
    }

    public Invest setShare(double share) {
        this.share = share;
        return this;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public Invest setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        return this;
    }

    public double getFee() {
        return fee;
    }

    public Invest setFee(double fee) {
        this.fee = fee;
        return this;
    }

    public double getTax() {
        return tax;
    }

    public Invest setTax(double tax) {
        this.tax = tax;
        return this;
    }

    public double getFxRate() {
        return fxRate;
    }

    public Invest setFxRate(double fxRate) {
        this.fxRate = fxRate;
        return this;
    }

    public double amount(double price) {
        price = (price < 0 ? unitPrice : price);
        if (share < 0) {
            return Math.abs(share) * price * fxRate - fee - tax;
        } else {
            return -(share * price * fxRate + fee + tax);
        }
    }

    public double getAmount() {
        return amount(unitPrice);
    }

    public Invest setAmount(double amount) {
        //do nothing, it is just for json serialization
        return this;
    }

    public double getTotalSpend() {
        return totalSpend;
    }

    public Invest setTotalSpend(double totalSpend) {
        this.totalSpend = totalSpend;
        return this;
    }

    public boolean isMisMatchAlert() {
        return Math.abs(totalSpend - getAmount()) >= 0.1;
    }

    public Invest setMisMatchAlert(boolean misMatchAlert) {
        //do nothing, it is just for json serialization
        return this;
    }

    public String getId() {
        return id;
    }

    public Invest setId(String id) {
        this.id = id;
        return this;
    }

    public int getUserIndex() {
        return userIndex;
    }

    public Invest setUserIndex(int userIndex) {
        this.userIndex = userIndex;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Invest setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public int getBatch() {
        return batch;
    }

    public Invest setBatch(int batch) {
        this.batch = batch;
        return this;
    }

    public String getComments() {
        return comments;
    }

    public Invest setComments(String comments) {
        this.comments = comments;
        return this;
    }

    public static int compare(Invest o1, Invest o2) {
        int ret = o1.date().compareTo(o2.date());
        if (ret == 0) {
            return o1.userIndex - o2.userIndex;
        }
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Invest invest = (Invest) o;
        return Objects.equals(id, invest.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
