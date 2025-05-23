package io.github.lujian213.eggfund.controller;

import io.github.lujian213.eggfund.model.*;
import io.github.lujian213.eggfund.model.graphql.FundRTValueMapEntry;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin
@PreAuthorize("hasRole('USER')")
public class EggFundController {
    private final EggFundService eggFundService;

    public EggFundController(EggFundService eggFundService) {
        this.eggFundService = eggFundService;
    }

    @QueryMapping
    public List<FundInfo> getAllFunds() {
        return eggFundService.getAllFunds();
    }

    @QueryMapping
    public List<String> getAllFundTypes() {
        return Arrays.stream(eggFundService.getAllFundTypes()).map(Enum::toString).toList();
    }

    @QueryMapping
    public List<FundInfo> getAllUserInvestedFunds(@Argument String id) {
        return eggFundService.getAllUserInvestedFunds(id);
    }

    @QueryMapping
    public List<Investor> getAllInvestors() {
        return eggFundService.getAllInvestors();
    }

    @QueryMapping
    public List<Investor> getInvestorsByFund(@Argument String code) {
        return eggFundService.getInvestors(code);
    }

    @QueryMapping
    public List<FundValue> getFundValues(@Argument String code, @Argument String from, @Argument String to) {
        return eggFundService.getFundValues(code, from, to);
    }

    @QueryMapping
    public List<FundRTValueMapEntry> getFundRTValue(@Argument String codes) {
        Map<String, FundRTValue> fundRTValueMap = eggFundService.getFundRTValue(codes);
        return fundRTValueMap.entrySet().stream()
                .map(entry -> new FundRTValueMapEntry(entry.getKey(), entry.getValue()))
                .toList();
    }

    @QueryMapping
    public List<FundRTValue> getFundRTValueHistory(@Argument String code) {
        return eggFundService.getFundRTValueHistory(code);
    }

    @QueryMapping
    public List<Invest> getInvests(@Argument String id, @Argument String code,
                                   @Argument String from,
                                   @Argument String to,
                                   @Argument int batch) {
        return eggFundService.getInvests(id, code, from, to, batch);
    }

    @QueryMapping
    public List<InvestAudit> getInvestAudits(@Argument String date) {
        return eggFundService.getInvestAudits(date);
    }

    @MutationMapping
    public FundInfo addNewFund(@Argument FundInfo fundInfo) {
        return eggFundService.addNewFund(fundInfo.getId(), fundInfo);
    }

    @MutationMapping
    public Investor addNewInvestor(@Argument String id, @Argument String name, @Argument String icon) {
        return eggFundService.addNewInvestor(id, name, icon);
    }

    @MutationMapping
    public List<Invest> addNewInvest(@Argument String id, @Argument String code, @Argument List<Invest> invests, @Argument boolean overwrite) {
        return eggFundService.addNewInvest(id, code, invests, overwrite);
    }

    @MutationMapping
    public List<Invest> addNewInvests(@Argument String id, @Argument List<Invest> invests, @Argument boolean overwrite) {
        return eggFundService.addNewInvests(id, invests, overwrite);
    }

    @MutationMapping
    public FundInfo updateFund(@Argument FundInfo fundInfo) {
        return eggFundService.updateFund(fundInfo.getId(), fundInfo);
    }

    @MutationMapping
    public boolean updateFundValues(@Argument String code, @Argument String from, @Argument String to) {
        eggFundService.updateFundValues(code, from, to);
        return true;
    }

    @MutationMapping
    public Investor updateInvestor(@Argument String id, @Argument String name, @Argument String icon, @Argument String password) {
        return eggFundService.updateInvestor(id, name, icon, password);
    }

    @MutationMapping
    public Invest updateInvest(@Argument String id, @Argument Invest invest) {
        return eggFundService.updateInvest(id, invest);
    }

    @MutationMapping
    public boolean deleteInvest(@Argument String id, @Argument String investId) {
        eggFundService.deleteInvest(id, investId);
        return true;
    }

    @MutationMapping
    public boolean deleteInvests(@Argument String id, @Argument List<String> investIds) {
        eggFundService.deleteInvests(id, investIds);
        return true;
    }

    @MutationMapping
    public boolean deleteInvestor(@Argument String id) {
        eggFundService.deleteInvestor(id);
        return true;
    }

    @MutationMapping
    public boolean deleteFund(@Argument String code) {
        eggFundService.deleteFund(code);
        return true;
    }

    @MutationMapping
    public Invest disableInvest(@Argument String id, @Argument String investId, @Argument boolean enabled) {
        return eggFundService.disableInvest(id, investId, enabled);
    }

    @QueryMapping
    public InvestSummary generateInvestSummary(@Argument String id,
                                               @Argument String code,
                                               @Argument String from,
                                               @Argument String to,
                                               @Argument int batch,
                                               @Argument float raiseRate,
                                               @Argument float fxRate) {
        return eggFundService.generateInvestSummary(id, code, from, to, batch, raiseRate, fxRate);
    }

    @QueryMapping
    public InvestorSummary generateInvestorSummary(@Argument String id,
                                                   @Argument String from,
                                                   @Argument String to) {
        return eggFundService.generateInvestorSummary(id, from, to);
    }

    @QueryMapping
    public ResponseEntity<String> exportInvests(@Argument String id,
                                                @Argument String code,
                                                @Argument String from,
                                                @Argument String to,
                                                @Argument int batch) {
        return eggFundService.exportInvests(id, code, from, to, batch);
    }
}