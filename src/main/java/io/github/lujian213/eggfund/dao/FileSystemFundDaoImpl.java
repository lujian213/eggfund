package io.github.lujian213.eggfund.dao;

import io.github.lujian213.eggfund.model.FundInfo;
import io.github.lujian213.eggfund.model.FundValue;
import io.github.lujian213.eggfund.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
public class FileSystemFundDaoImpl extends FileSystemDaoImpl implements FundDao {
    private static final Logger log = LoggerFactory.getLogger(FileSystemFundDaoImpl.class);
    private static final Pattern FUND_VALUE_FILE_PATTERN = Pattern.compile("([^-]*)-(\\d{6}).json");

    public FileSystemFundDaoImpl(@Value("${repo.folder}") File repoFile) {
        super(repoFile);
    }

    @Override
    public List<FundInfo> loadFundInfo() throws IOException {
        File fundsFile = new File(repoFile, Constants.FUNDS_FILE_NAME);
        if (fundsFile.isFile()) {
            return Constants.MAPPER.readerForListOf(FundInfo.class).readValue(fundsFile);
        }
        return Collections.emptyList();
    }

    @Override
    public Map<String, List<FundValue>> loadFundValues(String code) throws IOException {
        Map<String, List<FundValue>> ret = new HashMap<>();
        File[] files = repoFile.listFiles(file -> isQualifiedFundValueFile(file, code));
        if (files == null) {
            files = new File[0];
        }
        Stream.of(files).forEach(file -> {
            Matcher matcher = FUND_VALUE_FILE_PATTERN.matcher(file.getName());
            if (matcher.matches()) {
                String month = matcher.group(2);
                try {
                    List<FundValue> fundValues = Constants.MAPPER.readerForListOf(FundValue.class).readValue(file);
                    log.info("load fund value from file {} success with {} records", file, fundValues.size());
                    ret.put(month, fundValues);
                } catch (IOException e) {
                    log.error("load fund value from file {} failed", file, e);
                }
            }
        });
        return ret;
    }

    @Override
    public void saveFundValues(String code, String month, List<FundValue> values) throws IOException {
        File fundValueFile = getFundValueFile(code, month);
        values.sort(Comparator.comparing(FundValue::date));
        Constants.MAPPER.writerFor(List.class).writeValue(fundValueFile, values);
    }
    @Override
    public void saveFunds(Collection<FundInfo> funds) throws IOException {
        File fundsFile = new File(repoFile, Constants.FUNDS_FILE_NAME);
        List<FundInfo> fundList = new ArrayList<>(funds);
        fundList.sort(Comparator.comparing(FundInfo::getId));
        Constants.MAPPER.writeValue(fundsFile, funds);
    }

    @Override
    public void deleteFundValues(String code) throws IOException {
        File[] files = repoFile.listFiles(file -> isQualifiedFundValueFile(file, code));
        if (files != null) {
            for (File file : files) {
                Files.delete(file.toPath());
            }
        }
    }

    protected File getFundValueFile(String code, String month) {
        return new File(repoFile, code + "-" + month + ".json");
    }

    protected boolean isQualifiedFundValueFile(File file, String code) {
        if (!file.isFile()) {
            return false;
        }
        return isQualifiedFundValueFile(file.getName(), code);
    }

    protected boolean isQualifiedFundValueFile(String fileName, String code) {
        Matcher matcher = FUND_VALUE_FILE_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            String fundCode = matcher.group(1);
            return fundCode.equals(code);
        }
        return false;
    }
}
