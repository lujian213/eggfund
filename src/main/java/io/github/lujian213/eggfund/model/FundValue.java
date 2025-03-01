package io.github.lujian213.eggfund.model;

public class FundValue extends LocalDateRelated <FundValue> {
    private double unitValue = -1;
    private double accumulatedValue = -1;
    private double increaseRate = 0;

    public FundValue() {
    }

    public FundValue(String day, double unitValue, double accumulatedValue, double increaseRate) {
        super(day);
        this.unitValue = unitValue;
        this.accumulatedValue = accumulatedValue;
        this.increaseRate = increaseRate;
    }

    public double getUnitValue() {
        return unitValue;
    }

    public FundValue setUnitValue(double unitValue) {
        this.unitValue = unitValue;
        return this;
    }

    public double getIncreaseRate() {
        return increaseRate;
    }

    public FundValue setIncreaseRate(double increaseRate) {
        this.increaseRate = increaseRate;
        return this;
    }

    public double getAccumulatedValue() {
        return accumulatedValue;
    }

    public FundValue setAccumulatedValue(double accumulatedValue) {
        this.accumulatedValue = accumulatedValue;
        return this;
    }
}