package io.github.lujian213.eggfund.service.loader;

import io.github.lujian213.eggfund.model.FundInfo;
import io.github.lujian213.eggfund.model.FundInfo.FundType;

import java.util.EnumMap;

public class FundInfoLoaderFactory {
    static final FundInfoLoaderFactory INSTANCE = new FundInfoLoaderFactory();
    final EnumMap<FundType, FundInfoLoader> loaderMap = new EnumMap<>(FundType.class);

    FundInfoLoaderFactory() {
        // Private constructor to prevent instantiation
    }
    public static FundInfoLoaderFactory getInstance() {
        return INSTANCE;
    }

    public FundInfoLoader getFundInfoLoader(FundInfo fundInfo) {
        return loaderMap.computeIfAbsent(fundInfo.getType(), type -> {
            throw new IllegalArgumentException("Unknown fund type: " + type);
        });
    }

    public void registerLoader(FundType type, FundInfoLoader loader) {
        loaderMap.put(type, loader);
    }
}
