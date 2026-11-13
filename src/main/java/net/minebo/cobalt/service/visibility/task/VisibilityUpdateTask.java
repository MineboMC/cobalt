package net.minebo.cobalt.service.visibility.task;

import net.minebo.cobalt.service.visibility.VisibilityService;
import org.bukkit.scheduler.BukkitRunnable;

public final class VisibilityUpdateTask extends BukkitRunnable {

    public static final long PERIOD_TICKS = 20L;

    private final VisibilityService service;

    public VisibilityUpdateTask(VisibilityService service) {
        this.service = service;
    }

    @Override
    public void run() {
        service.refreshAll();
    }
}
