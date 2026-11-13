package net.minebo.cobalt.service.reboot.task;

import net.minebo.cobalt.service.reboot.RebootService;
import org.bukkit.scheduler.BukkitRunnable;

public final class RebootTask extends BukkitRunnable {

    public static final long PERIOD_TICKS = 20L;

    private final RebootService service;

    public RebootTask(RebootService service) {
        this.service = service;
    }

    @Override
    public void run() {
        service.tick();
    }
}
