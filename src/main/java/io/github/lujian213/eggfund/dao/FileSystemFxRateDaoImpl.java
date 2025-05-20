package io.github.lujian213.eggfund.dao;

import io.github.lujian213.eggfund.model.FxRateInfo;
import io.github.lujian213.eggfund.utils.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class FileSystemFxRateDaoImpl extends FileSystemDaoImpl implements FxRateDao {

    public FileSystemFxRateDaoImpl(@Value("${repo.folder}") File repoFile) {
        super(repoFile);
    }

    @Override
    public List<FxRateInfo> loadFxRates() throws IOException {
        List<FxRateInfo> ret;
        File fxRatesFile = new File(repoFile, Constants.FX_RATES_FILE_NAME);
        if (fxRatesFile.isFile()) {
            ret =  Constants.MAPPER.readerForListOf(FxRateInfo.class).readValue(fxRatesFile);
        } else {
            ret = new ArrayList<>();
        }
        return ret;
    }

    @Override
    public void saveFxRates(Collection<FxRateInfo> fxRates) throws IOException {
        File fxRatesFile = new File(repoFile, Constants.FX_RATES_FILE_NAME);
        Constants.MAPPER.writeValue(fxRatesFile, fxRates);
    }
}