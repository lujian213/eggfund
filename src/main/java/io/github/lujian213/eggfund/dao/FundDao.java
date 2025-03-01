package io.github.lujian213.eggfund.dao;

import io.github.lujian213.eggfund.model.FundInfo;
import io.github.lujian213.eggfund.model.FundValue;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface FundDao {
    List<FundInfo> loadFundInfo() throws IOException;

    Map<String, List<FundValue>> loadFundValues(String code) throws IOException;

    void saveFundValues(String code, String month, List<FundValue> values) throws IOException;

    void saveFunds(Collection<FundInfo> funds) throws IOException;

    void deleteFundValues(String code) throws IOException;
}