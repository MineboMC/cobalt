package net.minebo.cobalt.service.reboot.provider;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minebo.cobalt.service.bossbar.BossBarProvider;
import net.minebo.cobalt.service.reboot.RebootService;
import net.minebo.cobalt.service.scheduler.TimeFormat;
import net.minebo.cobalt.util.Coloring;
import org.bukkit.entity.Player;

public final class RebootBossBarProvider implements BossBarProvider {

    private final RebootService service;

    public RebootBossBarProvider(RebootService service) {
        this.service = service;
    }

    @Override
    public Component getTitle(Player player) {
        long remaining = Math.max(0L, service.getTimeUntilReboot());
        // Round up so the bar reads 00:01 during the last second, not 00:00.
        long rounded = (remaining + 999L) / 1000L * 1000L;
        return Coloring.toComponent("<#FF6347><bold>Server Reboot</bold> <dark_gray>» <white>" + TimeFormat.minutes(rounded));
    }

    @Override
    public float getHealth(Player player) {
        return (float) Math.max(0L, service.getTimeUntilReboot()) / RebootService.BOSSBAR_WINDOW;
    }

    @Override
    public BossBar.Color getColor(Player player) {
        return BossBar.Color.RED;
    }
}
