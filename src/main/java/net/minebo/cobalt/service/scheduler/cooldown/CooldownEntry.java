package net.minebo.cobalt.service.scheduler.cooldown;

import java.util.UUID;

public final class CooldownEntry {

    private final UUID playerId;
    private final String cooldownId;
    private final long expireAt;

    public CooldownEntry(UUID playerId, String cooldownId, long expireAt) {
        this.playerId = playerId;
        this.cooldownId = cooldownId;
        this.expireAt = expireAt;
    }

    public UUID playerId() {
        return playerId;
    }

    public String cooldownId() {
        return cooldownId;
    }

    public long expireAt() {
        return expireAt;
    }

    public long remainingMillis() {
        return Math.max(0L, expireAt - System.currentTimeMillis());
    }

    public boolean expired() {
        return System.currentTimeMillis() >= expireAt;
    }
}