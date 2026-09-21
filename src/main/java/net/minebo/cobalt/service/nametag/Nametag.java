package net.minebo.cobalt.service.nametag;

import java.util.Objects;

public final class Nametag {

    public static final Nametag EMPTY = new Nametag("", "", true);

    private final String prefix;
    private final String suffix;
    private final boolean visible;

    public Nametag(String prefix, String suffix, boolean visible) {
        this.prefix = prefix == null ? "" : prefix;
        this.suffix = suffix == null ? "" : suffix;
        this.visible = visible;
    }

    public static Nametag of(String prefix, String suffix) {
        return new Nametag(prefix, suffix, true);
    }

    public static Nametag prefix(String prefix) {
        return new Nametag(prefix, "", true);
    }

    public static Nametag hidden() {
        return new Nametag("", "", false);
    }

    public String prefix() {
        return prefix;
    }

    public String suffix() {
        return suffix;
    }

    public boolean visible() {
        return visible;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Nametag other)) {
            return false;
        }
        return visible == other.visible
                && Objects.equals(prefix, other.prefix)
                && Objects.equals(suffix, other.suffix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix, suffix, visible);
    }
}