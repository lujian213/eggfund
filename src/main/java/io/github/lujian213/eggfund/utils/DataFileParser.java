package io.github.lujian213.eggfund.utils;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import io.github.lujian213.eggfund.model.Invest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static io.github.lujian213.eggfund.utils.Constants.DATE_FORMAT;
import static io.github.lujian213.eggfund.utils.Constants.DATE_FORMAT2;

public class DataFileParser {
    public static class CSVData {
        private String day;
        private String code;
        private String name;
        private String stockName;
        private int share;
        private double price;
        private double fee;
        private double tax;
        private double amount;

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getStockName() {
            return stockName;
        }

        public void setStockName(String stockName) {
            this.stockName = stockName;
        }

        public int getShare() {
            return share;
        }

        public void setShare(int share) {
            this.share = share;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public double getFee() {
            return fee;
        }

        public void setFee(double fee) {
            this.fee = fee;
        }

        public double getTax() {
            return tax;
        }

        public void setTax(double tax) {
            this.tax = tax;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public Invest toInvest() {
            Invest invest = new Invest();
            invest.setDay(DATE_FORMAT.format(DATE_FORMAT2.parse(day)));
            invest.setCode(code);
            invest.setShare(share);
            invest.setUnitPrice(price);
            invest.setFee(fee);
            invest.setTax(tax);
            return invest;
        }
    }
    private static final CsvSchema CSV_SCHEMA = CsvSchema.builder()
            .addColumn("day")
            .addColumn("code")
            .addColumn("name")
            .addColumn("stockName")
            .addColumn("share")
            .addColumn("price")
            .addColumn("fee")
            .addColumn("tax")
            .addColumn("amount")
            .setUseHeader(true)
            .setColumnSeparator('|')
            .build().withHeader().withColumnSeparator('|');
    private static final CsvMapper CSV_MAPPER = new CsvMapper();

    public List<Invest> parseInvestFile(InputStream is, String fileName) throws IOException {
        if (fileName.toLowerCase().endsWith("csv")) {
            return CSV_MAPPER.readerFor(CSVData.class).with(CSV_SCHEMA).readValues(is).readAll()
                    .stream().map(obj -> ((CSVData)obj).toInvest()).toList();
        } else if (fileName.toLowerCase().endsWith("json")) {
            return Constants.MAPPER.readerForListOf(Invest.class).readValue(is);
        } else {
            throw new IOException("Unsupported file type: " + fileName);
        }
    }
}
