package net.minebo.cobalt.service.tablist.task;

import net.minebo.cobalt.service.tablist.TabListService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public final class TabListUpdateTask extends BukkitRunnable {

    public static final long PERIOD_TICKS = 10L;

    private final TabListService service;

    public TabListUpdateTask(TabListService service) {
        this.service = service;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            service.refresh(player);
        }
    }
}