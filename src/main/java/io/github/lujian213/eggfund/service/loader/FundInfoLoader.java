package io.github.lujian213.eggfund.service.loader;

import io.github.lujian213.eggfund.model.FundInfo;
import io.github.lujian213.eggfund.model.FundRTValue;
import io.github.lujian213.eggfund.model.FundValue;

import java.time.LocalDate;
import java.util.List;

public interface FundInfoLoader {
    void loadFund(FundInfo fundInfo);
    List<FundValue> loadFundValue(String code, LocalDate from, LocalDate to);
    FundRTValue getFundRTValue(String code);
}
