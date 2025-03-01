package io.github.lujian213.eggfund.model;

import io.github.lujian213.eggfund.utils.Constants;

import java.time.LocalDate;

public record InvestAudit(String day, Invest oldInvest, Invest newInvest) {
    public InvestAudit(Invest oldInvest, Invest newInvest) {
        this(LocalDate.now(Constants.ZONE_ID).format(Constants.DATE_FORMAT), oldInvest, newInvest);
    }
}