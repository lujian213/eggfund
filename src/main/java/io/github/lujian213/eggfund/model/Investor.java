package io.github.lujian213.eggfund.model;

import jakarta.annotation.Nonnull;

import java.util.List;
import java.util.Objects;

public class Investor {
    private String id;
    private String name;
    private String icon;
    private String password;
    private List<String> roles;

    public Investor() {
    }

    public Investor(@Nonnull String id, @Nonnull String name, String icon) {
        this(id, name, icon, id, List.of("USER"));
    }

    public Investor(@Nonnull String id, @Nonnull String name, String icon, String password, List<String> roles) {
        if (id.trim().isEmpty() || name.trim().isEmpty()) {
            throw new IllegalArgumentException("id and name should not be empty");
        }
        this.id = id.trim();
        this.name = name.trim();
        this.icon = icon;
        this.password = password;
        this.roles = roles;
    }

    public Investor(Investor investor) {
        this.id = investor.id;
        this.name = investor.name;
        this.icon = investor.icon;
        this.password = "***";
        this.roles = investor.roles;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
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

    @Override
    public String toString() {
        return "Investor{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                ", password='" + "[PROTECTED]" + '\'' +
                ", roles=" + roles +
                '}';
    }
}