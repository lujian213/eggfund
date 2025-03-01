package io.github.lujian213.eggfund.dao;

import io.github.lujian213.eggfund.model.InvestAudit;
import io.github.lujian213.eggfund.utils.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class FileSystemInvestAuditDaoImpl extends FileSystemDaoImpl implements InvestAuditDao {
    public FileSystemInvestAuditDaoImpl(@Value("${repo.folder}") File repoFile) {
        super(repoFile);
    }

    @Override
    public List<InvestAudit> loadInvestAudits(String date) throws IOException {
        File investAuditFile = getAuditFileName(date);
        if (investAuditFile.isFile()) {
            return Constants.MAPPER.readerForListOf(InvestAudit.class).readValue(investAuditFile);
        }
        return Collections.emptyList();
    }

    @Override
    public void saveInvestAudits(Collection<InvestAudit> investAudits) throws IOException {
        String date = getTodaysDate();
        File auditFile = getAuditFileName(date);
        Constants.MAPPER.writeValue(auditFile, investAudits);
    }

    protected File getAuditFileName(String date) {
        return new File(repoFile, Constants.INVEST_AUDIT_FILE_NAME_PATTERN.formatted(date));
    }

    protected String getTodaysDate() {
        return LocalDate.now(Constants.ZONE_ID).format(Constants.DATE_FORMAT2);
    }
}