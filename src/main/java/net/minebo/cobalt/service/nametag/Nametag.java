package net.minebo.cobalt.service.nametag;

import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Objects;

public final class Nametag {

    public static final Nametag EMPTY = new Nametag("", "", true);

    private final String prefix;
    private final String suffix;
    private final boolean visible;
    private final NamedTextColor color;
    private final String belowName;

    public Nametag(String prefix, String suffix, boolean visible) {
        this(prefix, suffix, visible, NamedTextColor.WHITE);
    }

    public Nametag(String prefix, String suffix, boolean visible, NamedTextColor color) {
        this(prefix, suffix, visible, color, "");
    }

    public Nametag(String prefix, String suffix, boolean visible, NamedTextColor color, String belowName) {
        this.prefix = prefix == null ? "" : prefix;
        this.suffix = suffix == null ? "" : suffix;
        this.visible = visible;
        this.color = color == null ? NamedTextColor.WHITE : color;
        this.belowName = belowName == null ? "" : belowName;
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
     * A line under the name, Minecraft's own below-name score: it hides with the name for invisible players,
     * fades when they sneak and never shows on your own. Empty for none.
     */
    public String belowName() {
        return belowName;
    }

    public Nametag withBelowName(String belowName) {
        return new Nametag(prefix, suffix, visible, color, belowName);
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
                && Objects.equals(belowName, other.belowName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix, suffix, visible, color, belowName);
    }
}