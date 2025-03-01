package io.github.lujian213.eggfund.utils;

import java.time.LocalDate;

public class LocalDateUtil {
    private LocalDateUtil() {
    }

    public static LocalDate parse(String date) {
        return (date == null ? null : LocalDate.parse(date, Constants.DATE_FORMAT));
    }
}