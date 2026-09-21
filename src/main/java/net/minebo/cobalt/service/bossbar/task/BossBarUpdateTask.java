package net.minebo.cobalt.service.bossbar.task;

import net.minebo.cobalt.service.bossbar.BossBarService;
import org.bukkit.scheduler.BukkitRunnable;

public final class BossBarUpdateTask extends BukkitRunnable {

    public static final long PERIOD_TICKS = 5L;

    private final BossBarService service;

    public BossBarUpdateTask(BossBarService service) {
        this.service = service;
    }

    @Override
    public void run() {
        service.refreshAll();
    }
}