package io.github.lujian213.eggfund.dao

import io.github.lujian213.eggfund.model.FxRateInfo

class FileSystemFxRateDaoImplSpec extends FileSystemDaoSpec {
    FileSystemFxRateDaoImpl dao

    def setup() {
        dao = new FileSystemFxRateDaoImpl(testDir)
    }

    @Override
    File getTestDir() {
        new File("dummyFxRateFolder")
    }

    def "saveFxRates & loadFxRates"() {
        given:
        def fxRate1 = new FxRateInfo("HKD", 0.95, "2025-05-06")
        def fxRate2 = new FxRateInfo("RMB", 1.0, "")
        def fxRates = [fxRate1, fxRate2]

        expect:
        dao.loadFxRates().isEmpty()

        when:
        dao.saveFxRates(fxRates)
        then:
        def fxRateList = dao.loadFxRates()
        fxRateList.size() == 2
        fxRateList[0] == fxRate1
        fxRateList[1] == fxRate2
    }
}