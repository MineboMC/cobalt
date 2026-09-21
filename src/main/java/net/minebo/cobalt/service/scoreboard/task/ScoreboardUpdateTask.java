package net.minebo.cobalt.service.scoreboard.task;

import net.minebo.cobalt.service.scoreboard.ScoreboardService;
import org.bukkit.scheduler.BukkitRunnable;

public final class ScoreboardUpdateTask extends BukkitRunnable {

    public static final long PERIOD_TICKS = 2L;

    private final ScoreboardService service;

    public ScoreboardUpdateTask(ScoreboardService service) {
        this.service = service;
    }

    @Override
    public void run() {
        service.refreshAll();
    }
}