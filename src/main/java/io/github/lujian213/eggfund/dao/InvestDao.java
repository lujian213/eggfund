package io.github.lujian213.eggfund.dao;

import io.github.lujian213.eggfund.model.Invest;
import io.github.lujian213.eggfund.model.Investor;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface InvestDao {
    List<Investor> loadInvestors() throws IOException;

    List<Invest> loadInvests(String investId) throws IOException;

    void saveInvestors(Collection<Investor> investors) throws IOException;

    void saveInvests(String investorId, Collection<Invest> invests) throws IOException;
}