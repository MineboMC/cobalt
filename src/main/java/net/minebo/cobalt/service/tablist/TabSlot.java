package net.minebo.cobalt.service.tablist;

import net.kyori.adventure.text.Component;

import java.util.Objects;

public final class TabSlot {

    public static final TabSlot EMPTY = new TabSlot(Component.empty(), TabSkin.DEFAULT, 0);

    private final Component text;
    private final TabSkin skin;
    private final int ping;

    public TabSlot(Component text, TabSkin skin, int ping) {
        this.text = text == null ? Component.empty() : text;
        this.skin = skin == null ? TabSkin.DEFAULT : skin;
        this.ping = Math.max(0, ping);
    }

    public static TabSlot of(Component text) {
        return new TabSlot(text, TabSkin.DEFAULT, 0);
    }

    public static TabSlot of(Component text, TabSkin skin) {
        return new TabSlot(text, skin, 0);
    }

    public static TabSlot of(Component text, TabSkin skin, int ping) {
        return new TabSlot(text, skin, ping);
    }

    public Component text() {
        return text;
    }

    public TabSkin skin() {
        return skin;
    }

    public int ping() {
        return ping;
    }

    public TabSlot withText(Component text) {
        return new TabSlot(text, skin, ping);
    }

    public TabSlot withSkin(TabSkin skin) {
        return new TabSlot(text, skin, ping);
    }

    public TabSlot withPing(int ping) {
        return new TabSlot(text, skin, ping);
    }

    public boolean sameVisuals(TabSlot other) {
        return other != null
                && Objects.equals(text, other.text)
                && Objects.equals(skin, other.skin)
                && ping == other.ping;
    }
}