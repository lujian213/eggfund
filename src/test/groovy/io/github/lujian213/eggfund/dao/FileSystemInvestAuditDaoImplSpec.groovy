package io.github.lujian213.eggfund.dao

import spock.lang.Specification
import io.github.lujian213.eggfund.model.Invest
import io.github.lujian213.eggfund.model.InvestAudit
import io.github.lujian213.eggfund.utils.Constants
import java.time.LocalDate

class FileSystemInvestAuditDaoImplSpec extends FileSystemDaoSpec {

    FileSystemInvestAuditDaoImpl dao

    def setup() {
        dao = Spy(FileSystemInvestAuditDaoImpl, constructorArgs: [testDir]) {
            getTodaysDate() >> LocalDate.of(2025, 2, 1).format(Constants.DATE_FORMAT2)
        }
    }

    @Override
    File getTestDir() {
        new File("dummyAuditFolder")
    }

    def "saveInvestAudits & loadInvestAudits"() {
        given:
        def audit1 = new InvestAudit("2025-02-01", new Invest(day: "2025-02-01", id: "investl", share: 10), null)
        def audit2 = new InvestAudit("2025-02-01", null, new Invest(day: "2025-02-01", id: "invest2", share: 20))
        when:
        dao.saveInvestAudits([audit1, audit2])
        then:
        def auditList = dao.loadInvestAudits("20250201")
        auditList.size() == 2
        when:
        dao = new FileSystemInvestAuditDaoImpl(testDir)
        then:
        !dao.loadInvestAudits(dao.getTodaysDate())
    }
}