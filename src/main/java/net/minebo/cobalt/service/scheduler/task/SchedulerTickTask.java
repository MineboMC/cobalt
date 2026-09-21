package net.minebo.cobalt.service.scheduler.task;

import net.minebo.cobalt.service.scheduler.SchedulerService;
import org.bukkit.scheduler.BukkitRunnable;

public final class SchedulerTickTask extends BukkitRunnable {

    public static final long PERIOD_TICKS = 1L;

    private final SchedulerService service;

    public SchedulerTickTask(SchedulerService service) {
        this.service = service;
    }

    @Override
    public void run() {
        service.tick();
    }
}