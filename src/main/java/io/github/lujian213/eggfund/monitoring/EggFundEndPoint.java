package io.github.lujian213.eggfund.monitoring;

import io.github.lujian213.eggfund.model.FundInfo;
import io.github.lujian213.eggfund.service.FundDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Endpoint(id = "eggfund")
public class EggFundEndPoint {
    private FundDataService fundDataService;

    @Autowired
    public void setFundDataService(FundDataService fundDataService) {
        this.fundDataService = fundDataService;
    }

    @ReadOperation
    public Map<String, Map<String, Object>> getFundRTValues() {
        Map<String, Map<String, Object>> ret = new HashMap<>();
        Map<String, FundInfo> fundMap = fundDataService.getAllFunds().stream().collect(Collectors.toMap(FundInfo::getId, Function.identity()));
        List<String> codes = fundMap.values().stream().map(FundInfo::getId).toList();
        fundDataService.getFundRTValues(codes).forEach((code, value) -> {
            Map<String, Object> map = new HashMap<>();
            FundInfo fund = fundMap.get(code);
            map.put("name", fund.getAlias() != null ? fund.getAlias() : fund.getName());
            map.put("unitValue", value.getUnitValue());
            map.put("asOfDate", value.getTime());
            ret.put(code, map);
        });
        //sort by fund's priority
        return ret.entrySet().stream().sorted(Comparator.comparingInt(e -> fundMap.get(e.getKey()).getPriority()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
}