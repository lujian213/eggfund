package io.github.lujian213.eggfund.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("squid:S112")
public class TableHelper {
    public static class ColumnDataExtractor<T> {
        private String colName;
        private final By xpath;
        private final WebElementVisitor<T> visitor;

        public ColumnDataExtractor(String colName, String xpath, WebElementVisitor<T> visitor) {
            this.colName = colName;
            this.xpath = By.xpath(xpath);
            this.visitor = visitor;
        }

        public ColumnDataExtractor(String colName, By xpath, WebElementVisitor<T> visitor) {
            this.colName = colName;
            this.xpath = xpath;
            this.visitor = visitor;
        }

        public List<T> getColumnData(WebElement table) {
            List<T> ret = new ArrayList<>();
            List<WebElement> wes = table.findElements(xpath);
            for (WebElement we : wes) {
                ret.add(visitor.visit(we));
            }
            return ret;
        }

        public T getSingleColumnData(WebElement table) {
            WebElement we = table.findElement(xpath);
            return visitor.visit(we);
        }

        public String getColName() {
            return this.colName;
        }

        public void setColName(String colName) {
            this.colName = colName;
        }
    }

    public static class ColumnConfig {
        private String colName;
        private String xpath;

        public String getColName() {
            return colName;
        }

        public void setColName(String colName) {
            this.colName = colName;
        }

        public String getXpath() {
            return xpath;
        }

        public void setXpath(String xpath) {
            this.xpath = xpath;
        }

        public static ColumnConfig[] parse(String json) {
            try {
                return new ObjectMapper().readValue(json, ColumnConfig[].class);
            } catch (Exception e) {
                throw new RuntimeException("read config error", e);
            }
        }
    }

    public interface WebElementVisitor<T> {
        public T visit(WebElement we);
    }

    public interface ValueRetriever {
        public String retrieve(WebElement we);
    }

    public interface WebElementFilter {
        public boolean filter(WebElement we);
    }

    public static class TextValueRetriever implements ValueRetriever {
        @Override
        public String retrieve(WebElement we) {
            return we.getText();
        }
    }

    public abstract static class BaseValueVisitor<T> implements WebElementVisitor<T> {
        protected ValueRetriever retriever = new TextValueRetriever();

        protected BaseValueVisitor() {
        }

        protected BaseValueVisitor(ValueRetriever retriever) {
            this.retriever = retriever;
        }

        @Override
        public T visit(WebElement we) {
            return transform(retriever.retrieve(we));
        }

        protected abstract T transform(String str);
    }

    public static class StringValueVisitor extends BaseValueVisitor<String> {
        public StringValueVisitor() {
            super();
        }

        public StringValueVisitor(ValueRetriever retriever) {
            super(retriever);
        }

        @Override
        protected String transform(String str) {
            return str;
        }
    }

    public static class IntValueVisitor extends BaseValueVisitor<Integer> {
        public IntValueVisitor() {
            super();
        }

        public IntValueVisitor(ValueRetriever retriever) {
            super(retriever);
        }

        @Override
        protected Integer transform(String str) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    public static class FloatValueVisitor extends BaseValueVisitor<Float> {
        public FloatValueVisitor() {
            super();
        }

        public FloatValueVisitor(ValueRetriever retriever) {
            super(retriever);
        }

        @Override
        protected Float transform(String str) {
            try {
                return Float.parseFloat(str);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    public static class DoubleValueVisitor extends BaseValueVisitor<Double> {
        public DoubleValueVisitor() {
            super();
        }

        public DoubleValueVisitor(ValueRetriever retriever) {
            super(retriever);
        }

        @Override
        protected Double transform(String str) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    public static class DateValueVisitor extends BaseValueVisitor<Date> {
        private final String format;

        public DateValueVisitor(String format) {
            super();
            this.format = format;
        }

        public DateValueVisitor(String format, ValueRetriever retriever) {
            super(retriever);
            this.format = format;
        }

        @Override
        protected Date transform(String str) {
            SimpleDateFormat sf = new SimpleDateFormat(format);
            try {
                return sf.parse(str);
            } catch (ParseException e) {
                return null;
            }
        }
    }

    public static List<String> getTableColumn(WebElement table, String xpath) {
        return getTableColumn(table, xpath, new StringValueVisitor());
    }

    public static <T> List<T> getTableColumn(WebElement table, String xpath, WebElementVisitor<T> visitor) {
        ColumnDataExtractor<T> extractor = new ColumnDataExtractor<>("", xpath, visitor);
        return extractor.getColumnData(table);
    }

    public static <T> List<T> getTableColumn(WebElement table, ColumnDataExtractor<T> extractor) {
        return extractor.getColumnData(table);
    }

    public static List<WebElement> getTableRows(WebElement table, String colXPath, WebElementFilter filter, String rowXPath) {
        List<WebElement> cols = table.findElements(By.xpath(colXPath));
        List<WebElement> ret = new ArrayList<>();
        for (WebElement col : cols) {
            if (filter.filter(col)) {
                ret.add(col.findElement(By.xpath(rowXPath)));
            }
        }
        return ret;
    }

    public static List<Map<String, Object>> getTable(WebElement table, ColumnConfig[] columnConfigs) {
        List<ColumnDataExtractor<?>> extractors = new ArrayList<>();
        for (ColumnConfig config : columnConfigs) {
            extractors.add(new ColumnDataExtractor<>(config.getColName(), config.getXpath(), new StringValueVisitor()));
        }
        return getTable(table, extractors);
    }

    public static List<Map<String, Object>> getTable(WebElement table, List<ColumnDataExtractor<?>> extractors) {
        List<Map<String, Object>> ret = new ArrayList<>();
        for (int i = 1; i <= extractors.size(); i++) {
            ColumnDataExtractor<?> extractor = extractors.get(i - 1);
            String colName = extractor.getColName();
            List<?> columnData = extractor.getColumnData(table);
            for (int j = 1; j <= columnData.size(); j++) {
                Map<String, Object> row = null;
                if (i == 1) {
                    row = new LinkedHashMap<>();
                    ret.add(row);
                } else {
                    row = ret.get(j - 1);
                }
                row.put(colName, columnData.get(j - 1));
            }
        }
        return ret;
    }

    public static List<Map<String, Object>> getTable(List<WebElement> rows, List<ColumnDataExtractor<?>> extractors) {
        List<Map<String, Object>> ret = new ArrayList<>();
        for (WebElement row : rows) {
            ret.addAll(getTable(row, extractors));
        }
        return ret;
    }

    public static List<Map<String, Object>> getFlatTable(List<WebElement> rows, List<ColumnDataExtractor<?>> extractors) {
        List<Map<String, Object>> ret = new ArrayList<>();
        Map<String, Object> map = new LinkedHashMap<>();
        ret.add(map);
        int index = 1;
        for (WebElement row : rows) {
            ColumnDataExtractor<?> extractor = (index <= extractors.size() ? extractors.get(index - 1) : null);
            if (extractor == null) {
                break;
            }
            map.put(extractor.getColName(), extractor.getSingleColumnData(row));
            index++;
        }
        return ret;
    }

    public static List<Map<String, Object>> getFlatTable(List<WebElement> rows, String keyXPath, String valueXPath) {
        List<ColumnDataExtractor<?>> extractors = new ArrayList<>();
        for (WebElement row : rows) {
            WebElement keyWebElement = row.findElement(By.xpath(keyXPath));
            extractors.add(new ColumnDataExtractor<>(keyWebElement.getText(), valueXPath, new StringValueVisitor()));
        }
        return getFlatTable(rows, extractors);
    }
}