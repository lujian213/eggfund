package io.github.lujian213.eggfund.model;

import io.github.lujian213.eggfund.utils.Constants;

import java.time.LocalDateTime;

public class FundRTValue extends FundValue {
    private final String time;

    public FundRTValue(String time, double unitValue, double increaseRate) {
        super(getDay(time), unitValue, -1, increaseRate);
        this.time = time;

    }

    protected static String getDay(String time) {
        return LocalDateTime.parse(time, Constants.MINUTE_FORMAT).toLocalDate().format(Constants.DATE_FORMAT);
    }

    public String getTime() {
        return time;
    }
}