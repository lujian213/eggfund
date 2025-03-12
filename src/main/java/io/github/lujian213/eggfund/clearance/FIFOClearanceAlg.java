package io.github.lujian213.eggfund.clearance;

import io.github.lujian213.eggfund.model.InvestSummaryItem;
import io.github.lujian213.eggfund.model.InvestSummaryItemExt;
import io.github.lujian213.eggfund.utils.CommonUtil;

import java.util.*;
import java.util.stream.Collectors;

public class FIFOClearanceAlg implements ClearanceAlg {
    @Override
    public List<InvestSummaryItem> clear(List<InvestSummaryItem> items) {
        Map<Integer, List<InvestSummaryItem>> batchMap = items.stream().collect(Collectors.groupingBy(
                        InvestSummaryItem::getBatch,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream()
                                        .sorted(Comparator.comparing(InvestSummaryItem::getDay)
                                                .thenComparing(InvestSummaryItem::getIndex))
                                        .toList()
                        )
                ));
        return batchMap.values().stream().map(this::clearBatch).
                flatMap(List::stream).sorted(Comparator.comparing(InvestSummaryItem::getDay))
                .toList();
    }

    protected List<InvestSummaryItem> clearBatch(List<InvestSummaryItem> items) {
        List<InvestSummaryItem> ret = new ArrayList<>();
        for (InvestSummaryItem item : items) {
            InvestSummaryItemExt currentItem = new InvestSummaryItemExt(item);
            currentItem.setEnabled(true);
            if (item.getType() == null || item.getQuota() > 0) {
                ret.add(currentItem);
            } else {
                doClear(ret, currentItem);
            }
        }
        List<InvestSummaryItemExt> toBeAddedList = new ArrayList<>();
        for (InvestSummaryItem item : ret) {
            InvestSummaryItemExt extItem = (InvestSummaryItemExt) item;
            if (extItem.getQuota() > 0 && extItem.isEnabled() && extItem.getQuota() != extItem.leftQuota()) {
                toBeAddedList.add(new InvestSummaryItemExt(extItem).setQuota(extItem.leftQuota()));
                extItem.setQuota(extItem.getQuota() - extItem.leftQuota()).setEnabled(false);
            }
        }
        ret.addAll(toBeAddedList);
        return ret;
    }

    protected Comparator<InvestSummaryItem> getComparator() {
        return Comparator.comparing(InvestSummaryItem::getDay).thenComparing(InvestSummaryItem::getIndex);
    }

    protected void doClear(List<InvestSummaryItem> itemList, InvestSummaryItemExt currentItem) {
        List<InvestSummaryItem> tempItemList = new ArrayList<>(itemList);
        Iterator<InvestSummaryItem> it = tempItemList.stream().
                sorted(getComparator()).iterator();
        while (it.hasNext()) {
            InvestSummaryItemExt item = (InvestSummaryItemExt) it.next();
            if (item.leftQuota() > 0) {
                double left = item.clear(currentItem.leftQuota());
                if (left > 0 || CommonUtil.isZero(left, 0.001)) {
                    currentItem.setLeftQuota(0);
                    currentItem.setEnabled(false);
                    break;
                } else {
                    currentItem.setLeftQuota(left);
                }
            }
        }
        if (currentItem.leftQuota() < 0 && !CommonUtil.isZero(currentItem.leftQuota(), 0.001)) {
            itemList.add(new InvestSummaryItemExt(currentItem).
                    setQuota(currentItem.getQuota() - currentItem.leftQuota()).setEnabled(false));
            currentItem.setQuota(currentItem.leftQuota());
        }
        itemList.add(currentItem);
    }
}
