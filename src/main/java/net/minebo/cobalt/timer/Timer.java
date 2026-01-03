package net.minebo.cobalt.timer;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minebo.cobalt.scheduler.Scheduler;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class Timer implements Listener {

    protected final int durationSeconds;
    protected final Map<UUID, Task> taskMap;
    protected final Plugin plugin;
    private BukkitTask bukkitTask;

    public Timer(int durationSeconds, Plugin plugin) {
        this.durationSeconds = durationSeconds;
        this.taskMap = new HashMap<>();
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void start(Player player) {
        // Cancel any existing
        if (taskMap.containsKey(player.getUniqueId())) {
            Bukkit.getScheduler().cancelTask(taskMap.get(player.getUniqueId()).getTaskId());
        }
        onStart(player);

        final int[] secondsLeft = { durationSeconds };

        bukkitTask = new Scheduler(plugin)
            .sync(() -> {
                if (!onTick(player, secondsLeft[0])) {
                    bukkitTask.cancel();
                    return;
                }
                secondsLeft[0]--;
                if (secondsLeft[0] == 0) {
                    if (taskMap.containsKey(player.getUniqueId())) {
                        taskMap.remove(player.getUniqueId());
                        onComplete(player);
                        bukkitTask.cancel();
                    }
                }
            })
            .repeat(20L, 20L)
            .run();

        taskMap.put(player.getUniqueId(), new Task(bukkitTask.getTaskId(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(durationSeconds)));
    }

    public boolean hasTimer(UUID uuid) {
        return taskMap.containsKey(uuid);
    }

    public String getRemaining(Player player) {
        long millisLeft = taskMap.get(player.getUniqueId()).getTime() - System.currentTimeMillis();
        if (millisLeft <= 0) return "0";

        if (millisLeft >= TimeUnit.MINUTES.toMillis(1)) {
            return DurationFormatUtils.formatDuration(millisLeft, "mm:ss");
        }

        if (millisLeft >= TimeUnit.HOURS.toMillis(1)) {
            return DurationFormatUtils.formatDuration(millisLeft, "hh:mm:ss");
        }

        // Show seconds with one decimal (e.g., 15.3s)
        double seconds = millisLeft / 1000.0;
        return String.format("%.1fs", seconds);
    }

    /** Called right after starting the task. */
    protected abstract void onStart(Player player);

    /**
     * Called every tick before secondsLeft is decremented.
     * Return false if the task should be cancelled immediately.
     */
    protected boolean onTick(Player player, int secondsLeft) { return true; }

    /** Called when timer finishes and task is completed normally. */
    protected abstract void onComplete(Player player);

}