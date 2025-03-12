package io.github.lujian213.eggfund.clearance;

import io.github.lujian213.eggfund.model.InvestSummaryItem;

import java.util.List;

public interface ClearanceAlg {
    List<InvestSummaryItem> clear(List<InvestSummaryItem> items);
}
