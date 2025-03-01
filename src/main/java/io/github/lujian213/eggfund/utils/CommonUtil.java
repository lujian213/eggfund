package io.github.lujian213.eggfund.utils;

public class CommonUtil {
    private CommonUtil() {
    }

    public static boolean isZero(double value, double delta) {
        return Math.abs(value) <= delta;
    }
}
