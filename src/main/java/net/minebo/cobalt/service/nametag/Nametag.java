package net.minebo.cobalt.service.nametag;

import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.Objects;

public final class Nametag {

    public static final Nametag EMPTY = new Nametag("", "", true);

    private final String prefix;
    private final String suffix;
    private final boolean visible;
    private final NamedTextColor color;
    private final List<String> lines;

    public Nametag(String prefix, String suffix, boolean visible) {
        this(prefix, suffix, visible, NamedTextColor.WHITE);
    }

    public Nametag(String prefix, String suffix, boolean visible, NamedTextColor color) {
        this(prefix, suffix, visible, color, List.of());
    }

    public Nametag(String prefix, String suffix, boolean visible, NamedTextColor color, List<String> lines) {
        this.prefix = prefix == null ? "" : prefix;
        this.suffix = suffix == null ? "" : suffix;
        this.visible = visible;
        this.color = color == null ? NamedTextColor.WHITE : color;
        this.lines = lines == null ? List.of() : List.copyOf(lines);
    }

    public static Nametag of(String prefix, String suffix) {
        return new Nametag(prefix, suffix, true);
    }

    /**
     * The color of the name itself, which a prefix can't change: the client only takes it from the team.
     */
    public static Nametag colored(NamedTextColor color, String prefix, String suffix) {
        return new Nametag(prefix, suffix, true, color);
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

    public NamedTextColor color() {
        return color;
    }

    /**
     * Extra lines shown above the name, top line first.
     */
    public List<String> lines() {
        return lines;
    }

    public Nametag withLines(String... lines) {
        return withLines(List.of(lines));
    }

    public Nametag withLines(List<String> lines) {
        return new Nametag(prefix, suffix, visible, color, lines);
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
                && Objects.equals(suffix, other.suffix)
                && Objects.equals(color, other.color)
                && Objects.equals(lines, other.lines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix, suffix, visible, color, lines);
    }
}