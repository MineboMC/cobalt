package net.minebo.cobalt.service.scheduler.timer;

import java.util.UUID;

public final class TimerEntry {

    private final String timerId;
    private final UUID playerId;
    private final long expireAt;
    private final boolean global;

    public TimerEntry(String timerId, UUID playerId, long expireAt, boolean global) {
        this.timerId = timerId;
        this.playerId = playerId;
        this.expireAt = expireAt;
        this.global = global;
    }

    public String timerId() {
        return timerId;
    }

    public UUID playerId() {
        return playerId;
    }

    public long expireAt() {
        return expireAt;
    }

    public boolean global() {
        return global;
    }

    public long remainingMillis() {
        return Math.max(0L, expireAt - System.currentTimeMillis());
    }

    public boolean expired() {
        return System.currentTimeMillis() >= expireAt;
    }
}