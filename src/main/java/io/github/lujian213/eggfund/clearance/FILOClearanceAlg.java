package io.github.lujian213.eggfund.clearance;

import io.github.lujian213.eggfund.model.InvestSummaryItem;

import java.util.Comparator;

public class FILOClearanceAlg extends FIFOClearanceAlg {
    @Override
    protected Comparator<InvestSummaryItem> getComparator() {
        return Comparator.comparing(InvestSummaryItem::getDay).reversed().
                thenComparing(Comparator.comparing(InvestSummaryItem::getIndex).reversed());
    }
}
