package net.minebo.cobalt.timer;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minebo.cobalt.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.TimeUnit;

public abstract class GlobalTimer implements Listener {

    protected final int durationSeconds;
    protected final Plugin plugin;
    private BukkitTask bukkitTask;
    private Task activeTask;

    public GlobalTimer(int durationSeconds, Plugin plugin) {
        this.durationSeconds = durationSeconds;
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void start() {
        // Cancel any existing global timer
        if (activeTask != null) {
            Bukkit.getScheduler().cancelTask(activeTask.getTaskId());
        }

        onStart();

        final int[] secondsLeft = { durationSeconds };

        bukkitTask = new Scheduler(plugin)
                .sync(() -> {
                    if (!onTick(secondsLeft[0])) {
                        bukkitTask.cancel();
                        activeTask = null;
                        return;
                    }
                    secondsLeft[0]--;
                    if (secondsLeft[0] == 0) {
                        activeTask = null;
                        onComplete();
                        bukkitTask.cancel();
                    }
                })
                .repeat(20L, 20L)
                .run();

        activeTask = new Task(bukkitTask.getTaskId(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(durationSeconds));
    }

    public void cancel() {
        if (activeTask != null) {
            Bukkit.getScheduler().cancelTask(activeTask.getTaskId());
            activeTask = null;
        }
    }

    public boolean isActive() {
        return activeTask != null;
    }

    public int getSecondsRemaining() {
        if (activeTask == null) {
            return 0;
        }
        long millisRemaining = activeTask.getTime() - System.currentTimeMillis();
        return (int) Math.max(0, TimeUnit. MILLISECONDS.toSeconds(millisRemaining));
    }

    /** Called right after starting the task. */
    protected abstract void onStart();

    /**
     * Called every tick before secondsLeft is decremented.
     * Return false if the task should be cancelled immediately.
     */
    protected boolean onTick(int secondsLeft) { return true; }

    /** Called when timer finishes and task is completed normally. */
    protected abstract void onComplete();

}