package io.github.lujian213.eggfund.model;

import java.util.Objects;

public class FundInfo {
    private String id;
    private String name;
    private boolean etf = false;
    private int priority = 10;
    private String url;
    private String category;
    private String alias;

    public FundInfo() {
    }

    public FundInfo(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public FundInfo setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public FundInfo setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isEtf() {
        return etf;
    }

    public FundInfo setEtf(boolean etf) {
        this.etf = etf;
        return this;
    }

    public int getPriority() {
        return priority;
    }

    public FundInfo setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public FundInfo setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public FundInfo setCategory(String category) {
        this.category = category;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public FundInfo setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public void update(FundInfo other) {
        this.etf = other.etf;
        this.priority = other.priority;
        this.url = other.url;
        this.category = other.category;
        this.alias = other.alias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FundInfo fundInfo = (FundInfo) o;
        return Objects.equals(id, fundInfo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}