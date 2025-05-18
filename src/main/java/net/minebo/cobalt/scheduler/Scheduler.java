package net.minebo.cobalt.scheduler;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class Scheduler {
    @Getter public Plugin owner;
    private boolean async;
    private Runnable task;
    private RunCycle cycle;
    private long delay;
    private long interval;

    public Scheduler(Plugin owner) {
        this.owner = owner;
        this.cycle = RunCycle.ONCE;
    }

    /**
     * Creates a sync task
     * @param task Task
     * @return Sync Task
     */
    public Scheduler sync(Runnable task) {
        this.task = task;
        this.async = false;
        return this;
    }

    /**
     * Creates an asynchronous task
     * @param task Asynchronous task
     * @return Asynchronous Task
     */
    public Scheduler async(Runnable task) {
        this.task = task;
        this.async = true;
        return this;
    }

    /**
     * Adds a delay to the task
     * @param milliseconds Delay in milliseconds
     * @return Task
     */
    public Scheduler delay(long milliseconds) {
        this.cycle = RunCycle.DELAYED;
        this.delay = milliseconds;
        return this;
    }

    /**
     * Makes the task repeat
     * @param delay Delay before starting
     * @param interval Repeat interval
     * @return Task
     */
    public Scheduler repeat(long delay, long interval) {
        this.cycle = RunCycle.REPEATING;
        this.delay = delay;
        this.interval = interval;
        return this;
    }

    /**
     * Starts the task
     * @return BukkitTask instance
     */
    public BukkitTask run() {
        if (task == null) {
            throw new NullPointerException("Task can not be null");
        }

        return switch (this.cycle) {
            case ONCE ->
                    (async) ? Bukkit.getScheduler().runTaskAsynchronously(owner, task) : Bukkit.getScheduler().runTask(owner, task);
            case DELAYED ->
                    (async) ? Bukkit.getScheduler().runTaskLaterAsynchronously(owner, task, delay) : Bukkit.getScheduler().runTaskLater(owner, task, delay);
            case REPEATING ->
                    (async) ? Bukkit.getScheduler().runTaskTimerAsynchronously(owner, task, delay, interval) : Bukkit.getScheduler().runTaskTimer(owner, task, delay, interval);
        };
    }

    public enum RunCycle {
        /**
         * Runs one time only
         */
        ONCE,
        /**
         * Runs after the provided delay
         */
        DELAYED,
        /**
         * Repeats based on the provided interval
         */
        REPEATING
    }
}