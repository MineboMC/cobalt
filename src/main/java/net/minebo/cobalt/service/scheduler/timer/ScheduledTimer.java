package net.minebo.cobalt.service.scheduler.timer;

import net.minebo.cobalt.service.scheduler.SchedulerService;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class ScheduledTimer implements Listener {

    private SchedulerService scheduler;

    public abstract String getId();

    public boolean isGlobal() {
        return false;
    }

    public void onStart(Player player) {
    }

    public void onTick(Player player, long remainingMillis) {
    }

    public void onComplete(Player player) {
    }

    public void onCancel(Player player) {
    }

    public SchedulerService scheduler() {
        return scheduler;
    }

    public void attach(SchedulerService scheduler) {
        this.scheduler = scheduler;
    }
}