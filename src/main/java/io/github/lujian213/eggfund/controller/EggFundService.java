package io.github.lujian213.eggfund.controller;

import io.github.lujian213.eggfund.exception.EggFundException;
import io.github.lujian213.eggfund.model.*;
import io.github.lujian213.eggfund.service.FundDataService;
import io.github.lujian213.eggfund.service.InvestService;
import io.github.lujian213.eggfund.utils.Constants;
import io.github.lujian213.eggfund.utils.FileNameUtil;
import io.github.lujian213.eggfund.utils.LocalDateUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
@Tag(name = "EggFund Service", description = "EggFUnd Service")
public class EggFundService {
    public interface ThrowingRunnable<T> {
        T run() throws Throwable;
    }

    private static final Logger log = LoggerFactory.getLogger(EggFundService.class);
    private FundDataService fundDataService;
    private InvestService investService;

    @Autowired
    public void setInvestService(InvestService investService) {
        this.investService = investService;
    }

    @Autowired
    public void setFundDataService(FundDataService fundDataService) {
        this.fundDataService = fundDataService;
    }

    @Operation(summary = "get all funds")
    @GetMapping(value = "/funds", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FundInfo> getAllFunds() {
        return runWithExceptionHandling("get all funds error", () -> fundDataService.getAllFunds());
    }

    @Operation(summary = "get all user invested funds")
    @GetMapping(value = "/funds/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FundInfo> getAllUserInvestedFunds(@PathVariable String id) {
        return runWithExceptionHandling("get all user invested funds error", () -> investService.getUserInvestedFunds(id));
    }

    @Operation(summary = "get all investors")
    @GetMapping(value = "/investors", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Investor> getAllInvestors() {
        return runWithExceptionHandling("get all investors error", () -> investService.getAllInvestors());
    }

    @Operation(summary = "get all investors for a fund")
    @GetMapping(value = "/investors/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Investor> getInvestors(@PathVariable String code) {
        return runWithExceptionHandling("get all investors error", () -> {
            fundDataService.checkFund(code);
            return investService.getInvestors(code);
        });
    }

    @Operation(summary = "get fund value")
    @GetMapping(value = "/values/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FundValue> getFundValues(@PathVariable String code, @RequestParam(required = false) String from, @RequestParam(required = false) String to) {
        return runWithExceptionHandling("get fund value error: " + code, () ->
                fundDataService.getFundValues(code, new DateRange(LocalDateUtil.parse(from), LocalDateUtil.parse(to)))
        );
    }

    @Operation(summary = "get fund real time value")
    @GetMapping(value = "/rtvalues", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, FundRTValue> getFundRTValue(@RequestParam String codes) {
        return runWithExceptionHandling("get fund real time values error: " + codes, () -> {
            List<String> codeList = Arrays.stream(codes.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
            return fundDataService.getFundRTValues(codeList);
        });
    }

    @Operation(summary = "get fund real time value history")
    @GetMapping(value = "/rtvalues/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FundRTValue> getFundRTValueHistory(@RequestParam String code) {
        return runWithExceptionHandling("get fund real time value history error: " + code, () -> fundDataService.getFundRTValueHistory(code));
    }

    @Operation(summary = "get user invests by product code")
    @GetMapping(value = "/invests/{id}/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Invest> getInvests(@PathVariable String id, @PathVariable String code,
                                   @RequestParam(required = false) String from,
                                   @RequestParam(required = false) String to,
                                   @RequestParam(required = false, defaultValue = "-1") int batch) {
        return runWithExceptionHandling("get invests error: " + id + "," + code, () ->
                investService.getInvests(id, code, new DateRange(LocalDateUtil.parse(from), LocalDateUtil.parse(to)), batch)
        );
    }

    @Operation(summary = "get invest audits")
    @GetMapping(value = "/audit", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<InvestAudit> getInvestAudits(@RequestParam(required = false) String date) {
        return runWithExceptionHandling("get invests audit error", () ->
                investService.getInvestAudits(date)
        );
    }

    @Operation(summary = "add new fund")
    @PutMapping(value = "/fund/{code}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public FundInfo addNewFund(@PathVariable String code, @RequestBody FundInfo fundInfo) {
        return runWithExceptionHandling("add new fund error: " + code, () -> fundDataService.addNewFund(fundInfo.setId(code)));
    }

    @Operation(summary = "add new investor")
    @PutMapping(value = "/investor", produces = MediaType.APPLICATION_JSON_VALUE)
    public Investor addNewInvestor(@RequestParam String id, @RequestParam String name, @RequestParam(required = false) String icon) {
        return runWithExceptionHandling("add new investor error: " + id,
                () -> investService.addNewInvestor(new Investor(FileNameUtil.makeValidFileName(id), name, icon)));
    }

    @Operation(summary = "add new invests")
    @PutMapping(value = "/invest/{id}/{code}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Invest> addNewInvest(@PathVariable String id, @PathVariable String code, @RequestBody List<Invest> invests) {
        return runWithExceptionHandling("add new invest error: " + id, () -> {
            FundInfo fund = fundDataService.checkFund(code);
            return investService.addInvests(id, fund, invests, false);
        });
    }

    @Operation(summary = "update fund")
    @PostMapping(value = "/fund/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public FundInfo updateFund(@PathVariable String code, @RequestBody FundInfo fundInfo) {
        return runWithExceptionHandling("update investor error: " + code, () -> fundDataService.updateFund(fundInfo.setId(code)));
    }

    @Operation(summary = "update fund value")
    @PostMapping(value = "/value/{code}")
    public void updateFundValues(@PathVariable String code,
                                 @RequestParam @Nonnull @Schema(description = "The value of the example", example = "2024-09-30") String from,
                                 @RequestParam @Nonnull @Schema(description = "The value of the example", example = "2024-09-30") String to) {
        runWithExceptionHandling("update fund values error: " + code, () -> {
            fundDataService.updateFundValues(code, new DateRange(LocalDateUtil.parse(from), LocalDateUtil.parse(to)));
            return null;
        });
    }

    @Operation(summary = "update investor")
    @PostMapping(value = "/investor/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Investor updateInvestor(@PathVariable String id, @RequestParam String
            name, @RequestParam(required = false) String icon) {
        return runWithExceptionHandling("update investor error: " + id, () -> investService.updateInvestor(new Investor(id, name, icon)));
    }

    @Operation(summary = "update invest")
    @PostMapping(value = "/invest/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Invest updateInvest(@PathVariable String id, @RequestBody Invest invest) {
        return runWithExceptionHandling("update invest error: " + id, () -> {
            fundDataService.checkFund(invest.getCode());
            return investService.updateInvest(id, invest);
        });
    }

    @Operation(summary = "delete invest")
    @DeleteMapping(value = "/invest/{id}/{investId}")
    public void deleteInvest(@PathVariable String id, @PathVariable String investId) {
        runWithExceptionHandling("delete invest error: " + id + "," + investId, () -> {
            investService.deleteInvest(id, investId);
            return null;
        });
    }

    @Operation(summary = "delete investor")
    @DeleteMapping(value = "/investor/{id}")
    public void deleteInvestor(@PathVariable String id) {
        runWithExceptionHandling("delete investor error: " + id, () -> {
            investService.deleteInvestor(id);
            return null;
        });
    }

    @Operation(summary = "delete fund")
    @DeleteMapping(value = "/fund/{code}")
    public void deleteFund(@PathVariable String code) {
        runWithExceptionHandling("delete fund error: " + code, () -> {
            fundDataService.deleteFund(code);
            return null;
        });
    }

    @Operation(summary = "disable invest")
    @PostMapping(value = "/disableinvest/{id}/{investId}")
    public Invest disableInvest(@PathVariable String id, @PathVariable String investId,
                                @RequestParam(required = false, defaultValue = "true") boolean enabled) {
        return runWithExceptionHandling("disable/enable investor error: " + id + "," + investId,
                () -> investService.disableInvest(id, investId, enabled)
        );
    }

    @Operation(summary = "generate invest summary")
    @PostMapping(value = "/summary/{id}/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public InvestSummary generateInvestSummary(@PathVariable String id, @PathVariable String code,
                                               @RequestParam(required = false) String from,
                                               @RequestParam(required = false) String to,
                                               @RequestParam(required = false, defaultValue = "-1") int batch,
                                               @RequestParam(required = false, defaultValue = "0") float raiseRate) {
        return runWithExceptionHandling("generate invest summary error: " + id + ", " + code,
                () -> investService.generateSummary(id, code, from, to, batch, raiseRate));
    }

    @Operation(summary = "generate investor summary")
    @PostMapping(value = "/summary/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public InvestorSummary generateInvestorSummary(@PathVariable String id,
                                                   @RequestParam(required = false) String from,
                                                   @RequestParam(required = false) String to) {
        return runWithExceptionHandling("generate investor summary error: " + id,
                () -> investService.generateInvestorSummary(id, from, to));
    }

    @Operation(summary = "upload invests")
    @PostMapping(value = "/uploadinvests/{id}/{code}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Invest> uploadInvests(@PathVariable String id, @PathVariable String code, @RequestParam MultipartFile file) {
        return runWithExceptionHandling("upload invests error: " + id + ", " + code, () -> {
            FundInfo fund = fundDataService.checkFund(code);
            try (InputStream is = file.getInputStream()) {
                List<Invest> invests = Constants.MAPPER.readerForListOf(Invest.class).readValue(is);
                return investService.addInvests(id, fund, invests, true);
            }
        });
    }

    @Operation(summary = "export invests")
    @GetMapping(value = "/exportinvests/{id}/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> exportInvests(@PathVariable String id, @PathVariable String code,
                                                @RequestParam(required = false) String from,
                                                @RequestParam(required = false) String to,
                                                @RequestParam(required = false, defaultValue = "-1") int batch) {
        return runWithExceptionHandling("export invests error: " + id + "," + code, () -> {
            List<Invest> invests = investService.getInvests(id, code, new DateRange(LocalDateUtil.parse(from), LocalDateUtil.parse(to)), batch);
            String content = Constants.MAPPER.writeValueAsString(invests);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Content-Disposition", "attachment; filename-invests.json;")
                    .body(content);
        });
    }

    protected <T> T runWithExceptionHandling(String errMsg, ThrowingRunnable<T> runnable) {
        try {
            return runnable.run();
        } catch (Throwable e) {
            return handleException(errMsg, e);
        }
    }

    protected <T> T handleException(String msg, Throwable e) {
        log.error(msg, e);
        if (e instanceof EggFundException) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}