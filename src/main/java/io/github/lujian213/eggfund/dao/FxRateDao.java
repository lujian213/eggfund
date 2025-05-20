package io.github.lujian213.eggfund.dao;

import io.github.lujian213.eggfund.model.FxRateInfo;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface FxRateDao {
    List<FxRateInfo> loadFxRates() throws IOException;

    void saveFxRates(Collection<FxRateInfo> fxRates) throws IOException;
}