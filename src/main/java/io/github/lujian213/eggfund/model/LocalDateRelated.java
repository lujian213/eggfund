package io.github.lujian213.eggfund.model;

import io.github.lujian213.eggfund.utils.Constants;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public abstract class LocalDateRelated<T extends LocalDateRelated<?>> {
    private LocalDate date = null;
    @Schema(description = "The value of the example", example = "2024-09-30")
    private String day;

    protected LocalDateRelated() {
    }

    protected LocalDateRelated(String day) {
        this.day = day;
        this.date = (day == null ? null : LocalDate.parse(day, Constants.DATE_FORMAT));
    }

    public LocalDate date() {
        return date;
    }

    public String getDay() {
        return day;
    }

    @SuppressWarnings("unchecked")
    public T setDay(String day) {
        this.day = day;
        this.date = LocalDate.parse(day, Constants.DATE_FORMAT);
        return (T) this;
    }
}