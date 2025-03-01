package io.github.lujian213.eggfund.dao;

import io.github.lujian213.eggfund.model.InvestAudit;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface InvestAuditDao {
    List<InvestAudit> loadInvestAudits(String date) throws IOException;

    void saveInvestAudits(Collection<InvestAudit> investAudits) throws IOException;
}