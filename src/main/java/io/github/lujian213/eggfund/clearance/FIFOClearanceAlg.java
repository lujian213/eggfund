package io.github.lujian213.eggfund.clearance;

import io.github.lujian213.eggfund.model.Invest;
import io.github.lujian213.eggfund.model.InvestSummaryItem;

import java.util.*;
import java.util.stream.Collectors;

public class FIFOClearanceAlg implements ClearanceAlg {
    @Override
    public String getAlgName() {
        return "FIFO";
    }

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
            InvestSummaryItem currentItem = new InvestSummaryItem(item);
            currentItem.setEnabled(true);
            if (!Invest.TYPE_TRADE.equals(currentItem.getType()) || currentItem.getQuota() > 0) {
                ret.add(currentItem);
            } else {
                doClear(ret, currentItem);
            }
        }
        return ret;
    }

    protected Comparator<InvestSummaryItem> getComparator() {
        return Comparator.comparing(InvestSummaryItem::getDay).thenComparing(InvestSummaryItem::getIndex);
    }

    protected void doClear(List<InvestSummaryItem> itemList, InvestSummaryItem currentItem) {
        List<InvestSummaryItem> tempItemList = new ArrayList<>(itemList);
        Iterator<InvestSummaryItem> it = tempItemList.stream().sorted(getComparator()).iterator();
        while (it.hasNext()) {
            InvestSummaryItem item = it.next();
            if (item.isEnabled()) {
                item.liquidate(currentItem);
                if (!currentItem.isEnabled()) {
                    break;
                }
            }
        }
        itemList.add(currentItem.setEnabled(false));
    }
}
