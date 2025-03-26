package io.github.lujian213.eggfund.dao;

import io.github.lujian213.eggfund.model.Invest;
import io.github.lujian213.eggfund.model.Investor;
import io.github.lujian213.eggfund.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Component
public class FileSystemInvestDaoImpl extends FileSystemDaoImpl implements InvestDao {

    public static final String ADMIN = "admin";
    private PasswordEncoder passwordEncoder;

    public FileSystemInvestDaoImpl(@Value("${repo.folder}") File repoFile) {
        super(repoFile);
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<Investor> loadInvestors() throws IOException {
        List<Investor> ret;
        File investorsFile = new File(repoFile, Constants.INVESTORS_FILE_NAME);
        if (investorsFile.isFile()) {
            ret =  Constants.MAPPER.readerForListOf(Investor.class).readValue(investorsFile);
        } else {
            ret = new ArrayList<>();
        }
        complementIfMissing(ret);
        saveInvestors(ret);
        return  ret;
    }

    private void complementIfMissing(List<Investor> investors) {
        Optional<Investor> admin = investors.stream().filter(investor -> ADMIN.equals(investor.getId())).findFirst();
        if (admin.isEmpty()) {
            investors.add(new Investor(ADMIN, ADMIN, null, passwordEncoder.encode(ADMIN), List.of(ADMIN)));
        }
        investors.stream()
                .filter(investor -> investor.getPassword() == null)
                .forEach(investor -> {
                    investor.setPassword(passwordEncoder.encode(investor.getId()));
                    investor.setRoles(List.of("user"));
                });
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