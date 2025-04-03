package io.github.lujian213.eggfund.service;

import io.github.lujian213.eggfund.dao.InvestAuditDao;
import io.github.lujian213.eggfund.dao.InvestDao;
import io.github.lujian213.eggfund.exception.EggFundException;
import io.github.lujian213.eggfund.model.*;
import io.github.lujian213.eggfund.utils.Constants;
import io.github.lujian213.eggfund.utils.LocalDateUtil;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class InvestService {
    private static final Logger log = LoggerFactory.getLogger(InvestService.class);
    final Map<Investor, Map<String, Invest>> investMap = new HashMap<>();
    Map<String, Investor> investorMap = new HashMap<>();
    List<InvestAudit> investAuditList = new LinkedList<>();
    boolean investDataChanged = false;

    private InvestDao investDao;
    private InvestAuditDao investAuditDao;
    private FundDataService fundDataService;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public void setInvestDao(InvestDao investDao) {
        this.investDao = investDao;
    }

    @Autowired
    public void setInvestAuditDao(InvestAuditDao investAuditDao) {
        this.investAuditDao = investAuditDao;
    }

    @Autowired
    public void setFundDataService(FundDataService fundDataService) {
        this.fundDataService = fundDataService;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    protected void init() throws IOException {
        investorMap.putAll(investDao.loadInvestors().stream().collect(Collectors.toMap(Investor::getId, Function.identity())));
        investAuditList.addAll(investAuditDao.loadInvestAudits(LocalDate.now(Constants.ZONE_ID).format(Constants.DATE_FORMAT2)));
        try {
            Set<String> fundCodeSet = fundDataService.getAllFunds().stream().map(FundInfo::getId).collect(Collectors.toSet());
            investorMap.values().forEach(investor -> {
                try {
                    List<Invest> invests = investDao.loadInvests(investor.getId());
                    investMap.put(investor, invests.stream().filter(invest -> fundCodeSet.contains(invest.getCode())).map(this::updateInvestPrice).collect(Collectors.toMap(Invest::getId, Function.identity())));
                    if (investDataChanged) {
                        investDao.saveInvests(investor.getId(), investMap.get(investor).values());
                        investDataChanged = false;
                    }
                } catch (Exception e) {
                    throw new EggFundException("load fund value for %s failed".formatted(investor.getId()), e);
                }
            });
            log.info("init fund data retriever with {} investors", investMap.size());
        } catch (EggFundException e) {
            throw new IOException(e);
        }
    }

    public List<InvestAudit> getInvestAudits(String date) {
        synchronized (investMap) {
            if (date == null) {
                return new ArrayList<>(checkInvestAudit());
            } else {
                try {
                    return investAuditDao.loadInvestAudits(date);
                } catch (IOException e) {
                    log.error("load invest audit for {} failed", date, e);
                    return new ArrayList<>();
                }
            }
        }
    }

    protected Invest updateInvestPrice(Invest invest) {
        if (invest.getUnitPrice() < 0) {
            log.info("find invalid invest unit price for {} in {}, try to load from fund value", invest.getCode(), invest.getDay());
            FundInfo fund = fundDataService.checkFund(invest.getCode());
            if (!fund.isEtf()) {
                FundValue value = fundDataService.getFundValue(fund.getId(), invest.date());
                if (value != null) {
                    invest.setUnitPrice(value.getUnitValue());
                    investDataChanged = true;
                }
            }
        }
        return invest;
    }

    public List<Invest> getInvests(String investorId, String code, DateRange range, int batch) {
        synchronized (this) {
            Investor investor = checkInvestor(investorId);
            Map<String, Invest> invests = investMap.computeIfAbsent(investor, k -> new HashMap<>());
            return invests.values().stream().filter(invest -> invest.getCode().equals(code) && range.inRange(invest.date()) && (batch < 0 || invest.getBatch() == batch)).
                    sorted(Invest::compare).toList();
        }
    }

    public List<FundInfo> getUserInvestedFunds(String investorId) {
        synchronized (this) {
            Investor investor = checkInvestor(investorId);
            Map<String, Invest> invests = investMap.computeIfAbsent(investor, k -> new HashMap<>());
            return invests.values().stream().collect(Collectors.groupingBy(Invest::getCode)).keySet().stream()
                    .map(fundDataService::findFund).filter(Objects::nonNull).sorted(Comparator.comparing(FundInfo::getPriority)).
                    toList();
        }
    }

    public List<Investor> getAllInvestors() {
        synchronized (this) {
            List<Investor> ret = new ArrayList<>(investorMap.values());
            ret.sort(Comparator.comparing(Investor::getName));
            return ret.stream().map(Investor::new).toList();
        }
    }

    public List<Investor> getInvestors(String code) {
        synchronized (this) {
            List<Investor> ret = new ArrayList<>();
            investMap.forEach((investor, invests) -> invests.values().stream().filter(invest -> invest.getCode().equals(code)).findAny().ifPresent(invest -> ret.add(investor)));
            ret.sort(Comparator.comparing(Investor::getName));
            return ret.stream().map(Investor::new).toList();
        }
    }

    public Investor addNewInvestor(Investor investor) {
        synchronized (this) {
            if (investorMap.containsKey(investor.getId())) {
                throw new EggFundException("Investor already exists: " + investor.getId());
            }
            try {
                Map<String, Investor> newInvestorMap = new HashMap<>(investorMap);
                // Set default password and roles
                Investor newInvestor = new Investor(investor.getId(), investor.getName(), investor.getIcon(), passwordEncoder.encode(investor.getId()), Constants.DEFAULT_ROLE_USER);
                newInvestorMap.put(investor.getId(), newInvestor);
                investDao.saveInvestors(newInvestorMap.values());
                investorMap.put(investor.getId(), newInvestor);
                // hide password
                return new Investor(newInvestor);
            } catch (IOException e) {
                throw new EggFundException("add new investor error: " + investor.getId(), e);
            }
        }
    }

    public Investor updateInvestor(Investor investor) {
        synchronized (this) {
            Investor existingInvestor = checkInvestor(investor.getId());
            try {
                Map<String, Investor> newInvestorMap = new HashMap<>(investorMap);
                Investor newInvestor = investor.mergeInvestor(passwordEncoder, existingInvestor);
                newInvestorMap.put(investor.getId(), newInvestor);
                investDao.saveInvestors(newInvestorMap.values());
                investorMap.put(investor.getId(), newInvestor);
                return new Investor(newInvestor);
            } catch (IOException e) {
                throw new EggFundException("update investor error: " + investor.getId(), e);
            }
        }
    }

    public void deleteInvestor(String investorId) {
        synchronized (this) {
            Investor existingInvestor = checkInvestor(investorId);
            try {
                Map<String, Investor> newInvestorMap = new HashMap<>(investorMap);
                newInvestorMap.remove(investorId);
                investDao.saveInvestors(newInvestorMap.values());
                investorMap.remove(investorId);
                investMap.remove(existingInvestor);
            } catch (IOException e) {
                throw new EggFundException("delete investor error: %s".formatted(investorId), e);
            }
        }
    }

    protected Map<String, Invest> generateInvestMap(Investor investor, FundInfo fund, List<Invest> invests, boolean overwrite, final Map<String, Invest> userInvestMap, final Map<String, Invest> newUserInvestMap, final List<InvestAudit> auditList) {
        invests.forEach(invest -> {
            if (!fund.getId().equals(invest.getCode())) {
                throw new EggFundException("Invest code not match: %s,%s".formatted(fund.getId(), invest.getCode()));
            }
            resetInvestPrice(fund, invest);
        });
        Map<String, Invest> newInvestMap = invests.stream().
                map(invest -> invest.setId(genInvestId(investor.getId(), fund.getId())).setCode(fund.getId())).map(this::updateInvestPrice).
                collect(Collectors.toMap(Invest::getId, Function.identity()));
        if (overwrite) {
            newUserInvestMap.putAll(overwriteInvests(fund, userInvestMap, newInvestMap));
            auditList.addAll(prepareAuditList(userInvestMap, newUserInvestMap));
        } else {
            newUserInvestMap.putAll(userInvestMap);
            newUserInvestMap.putAll(newInvestMap);
            newInvestMap.values().forEach(invest -> auditList.add(new InvestAudit(null, invest)));
        }
        resetInvestUserIndex(userInvestMap, newUserInvestMap.values());
        return newInvestMap;
    }

    public List<Invest> addInvests(String investorId, FundInfo fund, List<Invest> invests, boolean overwrite) {
        Investor investor = checkInvestor(investorId);

        synchronized (investMap) {
            try {
                Map<String, Invest> newUserInvestMap = new HashMap<>();
                List<InvestAudit> auditList = new ArrayList<>();
                Map<String, Invest> userInvestMap = investMap.computeIfAbsent(investor, key -> new HashMap<>());
                Map<String, Invest> newInvestMap = generateInvestMap(investor, fund, invests, overwrite, userInvestMap, newUserInvestMap, auditList);
                investDao.saveInvests(investorId, newUserInvestMap.values());
                List<InvestAudit> newAuditList = new LinkedList<>(checkInvestAudit());
                newAuditList.addAll(auditList);
                investAuditDao.saveInvestAudits(newAuditList);
                userInvestMap.clear();
                userInvestMap.putAll(newUserInvestMap);
                investAuditList.addAll(auditList);
                return new ArrayList<>(newInvestMap.values());
            } catch (IOException e) {
                throw new EggFundException("add invests error: " + investorId, e);
            }
        }
    }

    public List<Invest> addInvests(String investorId, List<Invest> invests, boolean overwrite) {
        Investor investor = checkInvestor(investorId);
        synchronized (investMap) {
            try {
                List<InvestAudit> auditList = new ArrayList<>();
                Map<String, Invest> userInvestMap = investMap.computeIfAbsent(investor, key -> new HashMap<>());
                Map<String, Invest> newInvestMap = new HashMap<>();
                Map<String, Invest> newUserInvestMap = new HashMap<>(userInvestMap);
                Map<String, Invest> temp = userInvestMap;
                for (Map.Entry<String, List<Invest>> entry: invests.stream().collect(Collectors.groupingBy(Invest::getCode)).entrySet()) {
                    String code = entry.getKey();
                    List<Invest> investList = entry.getValue();
                    FundInfo fund = fundDataService.checkFund(code);
                    newUserInvestMap = new HashMap<>();
                    newInvestMap.putAll(generateInvestMap(investor, fund, investList, overwrite, temp, newUserInvestMap, auditList));
                    temp = newUserInvestMap;
                }
                investDao.saveInvests(investorId, newUserInvestMap.values());
                List<InvestAudit> newAuditList = new LinkedList<>(checkInvestAudit());
                newAuditList.addAll(auditList);
                investAuditDao.saveInvestAudits(newAuditList);
                userInvestMap.clear();
                userInvestMap.putAll(newUserInvestMap);
                investAuditList.addAll(auditList);
                return new ArrayList<>(newInvestMap.values());
            } catch (IOException e) {
                throw new EggFundException("add invests error: " + investorId, e);
            }
        }
    }

    protected List<InvestAudit> prepareAuditList(Map<String, Invest> userInvestMap, Map<String, Invest> newUserInvestMap) {
        List<InvestAudit> auditList = new LinkedList<>();
        userInvestMap.values().forEach(invest -> {
            Invest newInvest = newUserInvestMap.get(invest.getId());
            if (newInvest == null) {
                auditList.add(new InvestAudit(invest, null));
            } else {
                auditList.add(new InvestAudit(invest, newInvest));
            }
        });
        newUserInvestMap.values().forEach(invest -> {
            if (!userInvestMap.containsKey(invest.getId())) {
                auditList.add(new InvestAudit(null, invest));
            }
        });
        return auditList;
    }

    protected Invest resetInvestPrice(FundInfo fund, Invest invest) {
        if (!fund.isEtf() && Invest.TYPE_TRADE.equals(invest.getType())) {
            invest.setUnitPrice(-1);
        }
        return invest;
    }

    protected void resetInvestUserIndex(Map<String, Invest> investMap, Collection<Invest> invests) {
        //for each invest, find the max userIndex in same day with same code in investMap, set userIndex as the max userIndex plus one
        Map<String, Integer> maxIndexMap = new HashMap<>();
        invests.forEach(invest -> {
            String key = invest.getDay() + ":" + invest.getCode();
            int maxIndex = maxIndexMap.computeIfAbsent(key, k ->
                    investMap.values().stream().filter(invest1 -> invest1.getDay().equals(invest.getDay()) && invest1.getCode().equals(invest.getCode())).mapToInt(Invest::getUserIndex).max().orElse(-1));
            maxIndex++;
            maxIndexMap.put(key, maxIndex);
            invest.setUserIndex(maxIndex);
        });
    }

    protected Map<String, Invest> overwriteInvests(FundInfo fund, Map<String, Invest> investMap, Map<String, Invest> newInvestMap) {
        Set<String> dateSet = newInvestMap.values().stream().map(Invest::getDay).collect(Collectors.toSet());
        Map<String, Invest> newUserInvestMap = investMap.values().stream().
                filter(invest -> !dateSet.contains(invest.getDay()) || !fund.getId().equals(invest.getCode())).
                collect(Collectors.toMap(Invest::getId, Function.identity()));
        newUserInvestMap.putAll(newInvestMap);
        return newUserInvestMap;
    }

    public Invest updateInvest(String investorId, Invest invest) {
        Investor investor = checkInvestor(investorId);
        FundInfo fundInfo = fundDataService.checkFund(invest.getCode());
        resetInvestPrice(fundInfo, invest);

        updateInvestPrice(invest);
        synchronized (investMap) {
            try {
                Map<String, Invest> userInvestMap = investMap.computeIfAbsent(investor, key -> new HashMap<>());
                Invest existingInvest = userInvestMap.get(invest.getId());
                if (existingInvest != null) {
                    invest.setUserIndex(existingInvest.getUserIndex());
                    Map<String, Invest> newUserInvestMap = new HashMap<>(userInvestMap);
                    Invest oldInvest = newUserInvestMap.put(invest.getId(), invest);
                    List<InvestAudit> newAuditList = new LinkedList<>(checkInvestAudit());
                    newAuditList.add(new InvestAudit(oldInvest, invest));
                    investDao.saveInvests(investorId, newUserInvestMap.values());
                    investAuditDao.saveInvestAudits(newAuditList);
                    userInvestMap.put(invest.getId(), invest);
                    investAuditList.add(new InvestAudit(oldInvest, invest));
                    return invest;
                } else {
                    throw new EggFundException("Invest not found: %s".formatted(invest.getId()));
                }
            } catch (IOException e) {
                throw new EggFundException("update invest error: " + investorId + "," + invest.getId(), e);
            }
        }
    }

    public Invest disableInvest(String investorId, String investId, boolean enabled) {
        Investor investor = checkInvestor(investorId);
        synchronized (investMap) {
            try {
                Map<String, Invest> userInvestMap = investMap.computeIfAbsent(investor, key -> new HashMap<>());
                Invest invest = userInvestMap.get(investId);
                if (invest != null) {
                    Invest newInvest = new Invest(invest).setEnabled(enabled);
                    Map<String, Invest> newUserInvestMap = new HashMap<>(userInvestMap);
                    newUserInvestMap.put(investId, newInvest);
                    investDao.saveInvests(investorId, newUserInvestMap.values());
                    invest.setEnabled(enabled);
                    return invest;
                } else {
                    throw new EggFundException("Invest not found: " + investId);
                }
            } catch (IOException e) {
                throw new EggFundException("disable/enable invest error: " + investorId + "," + investId, e);
            }
        }
    }

    public void deleteInvests(String investorId, List<String> investIds) {
        Investor investor = checkInvestor(investorId);
        synchronized (investMap) {
            try {
                Map<String, Invest> userInvestMap = investMap.computeIfAbsent(investor, key -> new HashMap<>());
                Map<String, Invest> newUserInvestMap = new HashMap<>(userInvestMap);
                List<InvestAudit> newAuditList = new LinkedList<>(checkInvestAudit());
                investIds.stream().map(String::trim).forEach(investId -> {
                    if (userInvestMap.containsKey(investId)) {
                        Invest oldInvest = newUserInvestMap.remove(investId);
                        newAuditList.add(new InvestAudit(oldInvest, null));
                    } else {
                        log.warn("Invest {} not found, skip. ", investId);
                    }
                });
                investDao.saveInvests(investorId, newUserInvestMap.values());
                investAuditDao.saveInvestAudits(newAuditList);
                userInvestMap.clear();
                userInvestMap.putAll(newUserInvestMap);
                investAuditList.clear();
                investAuditList.addAll(newAuditList);
            } catch (IOException e) {
                throw new EggFundException("delete invest error: " + investorId, e);
            }
        }
    }

    public InvestorSummary generateInvestorSummary(String investorId, String from, String to) {
        List<String> codes = getUserInvestedFunds(investorId).stream().map(FundInfo::getId).toList();
        List<InvestSummary> allInvests = fundDataService.getFundRTValues(codes).entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .map(entry -> generateSummary(investorId, entry.getKey(), from, to, -1, entry.getValue()))
                .toList();
        return new InvestorSummary(investorId, allInvests);
    }

    public InvestSummary generateSummary(String investorId, String code, String from, String to, int batch, float increaseRate) {
        FundRTValue rtValue = new FundRTValue(LocalDateTime.now(Constants.ZONE_ID).format(Constants.MINUTE_FORMAT), -1, increaseRate);
        return generateSummary(investorId, code, from, to, batch, rtValue);
    }

    public InvestSummary generateSummary(String investorId, String code, String from, String to, int batch, FundRTValue rtValue) {
        checkInvestor(investorId);
        FundInfo fundInfo = fundDataService.checkFund(code);
        DateRange range = new DateRange(LocalDateUtil.parse(from), LocalDateUtil.parse(to));
        List<FundValue> fundValues = fundDataService.getFundValues(code, range);
        synchronized (investMap) {
            List<Invest> invests = getInvests(investorId, code, range, batch);
            return new InvestSummary(fundInfo, fundValues, invests, rtValue, range.to() == null ? LocalDate.now(Constants.ZONE_ID) : range.to());
        }
    }

    protected Investor checkInvestor(String investorId) {
        synchronized (investorMap) {
            Investor investor = investorMap.get(investorId);
            if (investor == null) {
                throw new EggFundException("Investor not found: " + investorId);
            }
            return investor;
        }
    }

    public Investor securedInvestor(String investorId) {
        Investor investor = checkInvestor(investorId);
        return new Investor(investor);
    }

    public UserDetails getUser(String investorId) {
        Investor investor = checkInvestor(investorId);
        return convertToSpringUser(investor);
    }

    private UserDetails convertToSpringUser(Investor investor) {
        return new User(
                investor.getId(),
                investor.getPassword(),
                mapRolesToAuthorities(investor.getRoles())
        );
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<String> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .toList();
    }

    protected String genInvestId(String investorId, String code) {
        return investorId + "-" + code + "-" + UUID.randomUUID();
    }

    protected List<InvestAudit> checkInvestAudit() {
        LocalDate now = LocalDate.now(Constants.ZONE_ID);
        investAuditList.stream().findAny().ifPresent(investAudit -> {
            if (!investAudit.day().equals(now.format(Constants.DATE_FORMAT))) {
                investAuditList.clear();
            }
        });
        return investAuditList;
    }
}