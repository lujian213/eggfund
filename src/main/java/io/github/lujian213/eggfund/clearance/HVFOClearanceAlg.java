package io.github.lujian213.eggfund.clearance;

import io.github.lujian213.eggfund.model.InvestSummaryItem;

import java.util.Comparator;

public class HVFOClearanceAlg extends FIFOClearanceAlg {
    @Override
    protected Comparator<InvestSummaryItem> getComparator() {
        return Comparator.comparing(InvestSummaryItem::getPrice).reversed();
    }
}
