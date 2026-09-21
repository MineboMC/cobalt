package net.minebo.cobalt.service.nametag.task;

import net.minebo.cobalt.service.nametag.NametagService;
import org.bukkit.scheduler.BukkitRunnable;

public final class NametagUpdateTask extends BukkitRunnable {

    public static final long PERIOD_TICKS = 5L;

    private final NametagService service;

    public NametagUpdateTask(NametagService service) {
        this.service = service;
    }

    @Override
    public void run() {
        service.refreshAll();
    }
}