package net.minebo.cobalt.service.actionbar.task;

import net.minebo.cobalt.service.actionbar.ActionBarService;
import org.bukkit.scheduler.BukkitRunnable;

public final class ActionBarUpdateTask extends BukkitRunnable {

    public static final long PERIOD_TICKS = 5L;

    private final ActionBarService service;

    public ActionBarUpdateTask(ActionBarService service) {
        this.service = service;
    }

    @Override
    public void run() {
        service.refreshAll();
    }
}
