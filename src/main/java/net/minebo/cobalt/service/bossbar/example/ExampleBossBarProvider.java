package net.minebo.cobalt.service.bossbar.example;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minebo.cobalt.service.bossbar.BossBarProvider;
import net.minebo.cobalt.util.Coloring;
import org.bukkit.entity.Player;

public final class ExampleBossBarProvider implements BossBarProvider {

    @Override
    public Component getTitle(Player player) {
        int current = (int) Math.ceil(player.getHealth());
        int max = (int) Math.ceil(player.getMaxHealth());
        return Coloring.toComponent(player.getName() + "<gray> • <red>" + current + "/" + max + " HP");
    }

    @Override
    public float getHealth(Player player) {
        double max = player.getMaxHealth();
        if (max <= 0.0D) {
            return 0.0F;
        }
        return (float) (player.getHealth() / max);
    }

    @Override
    public BossBar.Color getColor(Player player) {
        return BossBar.Color.BLUE;
    }

    @Override
    public BossBar.Overlay getOverlay(Player player) {
        return BossBar.Overlay.NOTCHED_10;
    }
}