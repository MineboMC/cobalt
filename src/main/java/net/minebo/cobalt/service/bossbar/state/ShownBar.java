package net.minebo.cobalt.service.bossbar.state;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minebo.cobalt.service.bossbar.BossBarProvider;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.Objects;

public final class ShownBar {

    private static final Component EMPTY = Component.empty();

    private final Component title;
    private final float health;
    private final BossBar.Color color;
    private final BossBar.Overlay overlay;
    private final EnumSet<BossBar.Flag> flags;

    public ShownBar(
            Component title,
            float health,
            BossBar.Color color,
            BossBar.Overlay overlay,
            EnumSet<BossBar.Flag> flags
    ) {
        this.title = title == null ? EMPTY : title;
        this.health = clamp(health);
        this.color = color == null ? BossBar.Color.PURPLE : color;
        this.overlay = overlay == null ? BossBar.Overlay.PROGRESS : overlay;
        this.flags = flags == null || flags.isEmpty()
                ? EnumSet.noneOf(BossBar.Flag.class)
                : EnumSet.copyOf(flags);
    }

    public static ShownBar from(BossBarProvider provider, Player player) {
        return new ShownBar(
                provider.getTitle(player),
                provider.getHealth(player),
                provider.getColor(player),
                provider.getOverlay(player),
                provider.getFlags(player)
        );
    }

    public static float clamp(float health) {
        if (Float.isNaN(health) || health < 0.0F) {
            return 0.0F;
        }
        return Math.min(1.0F, health);
    }

    public Component title() {
        return title;
    }

    public float health() {
        return health;
    }

    public BossBar.Color color() {
        return color;
    }

    public BossBar.Overlay overlay() {
        return overlay;
    }

    public EnumSet<BossBar.Flag> flags() {
        return flags.isEmpty()
                ? EnumSet.noneOf(BossBar.Flag.class)
                : EnumSet.copyOf(flags);
    }

    public boolean sameTitle(ShownBar other) {
        return other != null && Objects.equals(title, other.title);
    }

    public boolean sameHealth(ShownBar other) {
        return other != null && Float.compare(health, other.health) == 0;
    }

    public boolean sameStyle(ShownBar other) {
        return other != null && color == other.color && overlay == other.overlay;
    }

    public boolean sameFlags(ShownBar other) {
        return other != null && flags.equals(other.flags);
    }

    public boolean sameVisuals(ShownBar other) {
        return sameTitle(other) && sameHealth(other) && sameStyle(other) && sameFlags(other);
    }
}