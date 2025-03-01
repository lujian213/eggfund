package io.github.lujian213.eggfund.service;

import io.github.lujian213.eggfund.model.FundValue;

import java.util.List;

public interface FundValueListener {
    void onFundValueChange(String fundCode, String month, List<FundValue> fundValues);
}