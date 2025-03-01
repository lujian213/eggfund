package io.github.lujian213.eggfund.model;

import java.time.LocalDate;

public record DateRange(LocalDate from, LocalDate to) {
    public boolean inRange(LocalDate date) {
        return (from == null || !date.isBefore(from)) && (to == null || !date.isAfter(to));
    }
}