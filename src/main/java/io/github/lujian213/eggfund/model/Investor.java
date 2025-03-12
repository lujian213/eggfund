package io.github.lujian213.eggfund.model;

import jakarta.annotation.Nonnull;

import java.util.Objects;

public class Investor {
    private String id;
    private String name;
    private String icon;

    public Investor() {
    }

    public Investor(@Nonnull String id, @Nonnull String name, String icon) {
        if (id.trim().isEmpty() || name.trim().isEmpty()) {
            throw new IllegalArgumentException("id and name should not be empty");
        }
        this.id = id.trim();
        this.name = name.trim();
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void update(Investor other) {
        this.name = other.name;
        this.icon = other.icon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Investor investor = (Investor) o;
        return Objects.equals(id, investor.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}