package io.github.lujian213.eggfund.service.loader

import io.github.lujian213.eggfund.model.FundInfo
import spock.lang.Specification

class FundInfoLoaderFactorySpec extends Specification {
    def "registerLoader and getLoader"() {
        given:
        def factory = new FundInfoLoaderFactory()
        def localFundLoader = Mock(FundInfoLoader)
        def hkStockLoader = Mock(FundInfoLoader)
        factory.registerLoader(FundInfo.FundType.LOCAL_FUND, localFundLoader)
        factory.registerLoader(FundInfo.FundType.HK_STOCK, hkStockLoader)

        expect:
        factory.loaderMap.size() == 2
        factory.getFundInfoLoader(new FundInfo(type: FundInfo.FundType.LOCAL_FUND)) == localFundLoader
        factory.getFundInfoLoader(new FundInfo(type: FundInfo.FundType.HK_STOCK)) == hkStockLoader
    }
}
