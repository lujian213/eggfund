package io.github.lujian213.eggfund.clearance;

import io.github.lujian213.eggfund.model.InvestSummaryItem;

import java.util.List;

public interface ClearanceAlg {
    static final List<ClearanceAlg> ALL_ALGS = List.of(new FIFOClearanceAlg(), new FILOClearanceAlg(), new HVFOClearanceAlg());
    String getAlgName();
    List<InvestSummaryItem> clear(List<InvestSummaryItem> items);
}
