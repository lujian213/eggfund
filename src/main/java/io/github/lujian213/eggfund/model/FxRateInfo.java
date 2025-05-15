package io.github.lujian213.eggfund.model;

public record FxRateInfo(String currency, double fxRate, String asOfTime) {
    public static final FxRateInfo RMB = new FxRateInfo("RMB", 1, "");
}
