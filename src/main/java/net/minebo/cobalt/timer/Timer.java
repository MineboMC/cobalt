package net.minebo.cobalt.timer;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minebo.cobalt.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class Timer implements Listener {
    protected final Player player;
    protected final int durationSeconds;
    protected final Map<UUID, Task> taskMap;
    protected final Plugin plugin;
    private BukkitTask bukkitTask;

    public Timer(Player player, int durationSeconds, Map<UUID, Task> taskMap, Plugin plugin) {
        this.player = player;
        this.durationSeconds = durationSeconds;
        this.taskMap = taskMap;
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void start() {
        // Cancel any existing
        if (taskMap.containsKey(player.getUniqueId())) {
            Bukkit.getScheduler().cancelTask(taskMap.get(player.getUniqueId()).getTaskId());
        }
        onStart();

        final int[] secondsLeft = { durationSeconds };

        bukkitTask = new Scheduler(plugin)
            .sync(() -> {
                if (!onTick(secondsLeft[0])) {
                    bukkitTask.cancel();
                    return;
                }
                secondsLeft[0]--;
                if (secondsLeft[0] == 0) {
                    if (taskMap.containsKey(player.getUniqueId())) {
                        taskMap.remove(player.getUniqueId());
                        onComplete();
                        bukkitTask.cancel();
                    }
                }
            })
            .repeat(20L, 20L)
            .run();

        taskMap.put(player.getUniqueId(), new Task(bukkitTask.getTaskId(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(durationSeconds)));
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


    @Data
    @AllArgsConstructor
    public static class Task {

        private final int taskId;
        private final long time;
    }
}