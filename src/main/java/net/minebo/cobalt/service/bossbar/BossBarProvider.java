package net.minebo.cobalt.service.bossbar;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.EnumSet;

public interface BossBarProvider {

    Component getTitle(Player player);

    float getHealth(Player player);

    BossBar.Color getColor(Player player);

    default BossBar.Overlay getOverlay(Player player) {
        return BossBar.Overlay.PROGRESS;
    }

    default EnumSet<BossBar.Flag> getFlags(Player player) {
        return EnumSet.noneOf(BossBar.Flag.class);
    }
}