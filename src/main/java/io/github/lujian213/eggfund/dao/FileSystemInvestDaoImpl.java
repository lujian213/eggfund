package io.github.lujian213.eggfund.dao;

import io.github.lujian213.eggfund.model.Invest;
import io.github.lujian213.eggfund.model.Investor;
import io.github.lujian213.eggfund.utils.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class FileSystemInvestDaoImpl extends FileSystemDaoImpl implements InvestDao {
    public FileSystemInvestDaoImpl(@Value("${repo.folder]") File repoFile) {
        super(repoFile);
    }

    @Override
    public List<Investor> loadInvestors() throws IOException {
        File investorsFile = new File(repoFile, Constants.INVESTORS_FILE_NAME);
        if (investorsFile.isFile()) {
            return Constants.MAPPER.readerForListOf(Investor.class).readValue(investorsFile);
        }
        return Collections.emptyList();
    }

    @Override
    public List<Invest> loadInvests(String investorId) throws IOException {
        File investFile = getInvestFile(investorId);
        if (investFile.isFile()) {
            return Constants.MAPPER.readerForListOf(Invest.class).readValue(investFile);
        }
        return new ArrayList<>();
    }

    @Override
    public void saveInvestors(Collection<Investor> investors) throws IOException {
        File investFile = new File(repoFile, Constants.INVESTORS_FILE_NAME);
        Constants.MAPPER.writeValue(investFile, investors);
    }

    @Override
    public void saveInvests(String investorId, Collection<Invest> invests) throws IOException {
        File investFile = getInvestFile(investorId);
        List<Invest> inVestList = new ArrayList<>(invests);
        inVestList.sort(Invest::compare);
        Constants.MAPPER.writeValue(investFile, inVestList);
    }

    protected File getInvestFile(String investorId) {
        return new File(repoFile, Constants.INVEST_FILE_NAME_PATTERN.formatted(investorId));
    }
}