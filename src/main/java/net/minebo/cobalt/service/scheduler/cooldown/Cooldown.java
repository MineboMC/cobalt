package net.minebo.cobalt.service.scheduler.cooldown;

import net.minebo.cobalt.service.scheduler.SchedulerService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class Cooldown implements Listener {

    private SchedulerService scheduler;

    public abstract String getId();

    public Material getItem() {
        return null;
    }

    public boolean persist() {
        return false;
    }

    public void onApply(Player player) {
    }

    public void onExpire(Player player) {
    }

    public SchedulerService scheduler() {
        return scheduler;
    }

    public void attach(SchedulerService scheduler) {
        this.scheduler = scheduler;
    }
}