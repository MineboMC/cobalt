package net.minebo.cobalt.service.actionbar.state;

import net.kyori.adventure.text.Component;

import java.util.UUID;

public final class PlayerActionBarState {

    private final UUID playerId;

    private Component lastSent;
    private long lastSentAt;

    private Component temporary;
    private long temporaryUntil;

    public PlayerActionBarState(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID playerId() {
        return playerId;
    }

    public Component lastSent() {
        return lastSent;
    }

    public long lastSentAt() {
        return lastSentAt;
    }

    public void markSent(Component component, long now) {
        this.lastSent = component;
        this.lastSentAt = now;
    }

    public boolean isShowing() {
        return lastSent != null;
    }

    public void setTemporary(Component component, long until) {
        this.temporary = component;
        this.temporaryUntil = until;
    }

    public void clearTemporary() {
        this.temporary = null;
        this.temporaryUntil = 0L;
    }

    /** The temporary message if it hasn't expired yet, clearing it once it has. */
    public Component activeTemporary(long now) {
        if (temporary != null && now >= temporaryUntil) {
            clearTemporary();
        }
        return temporary;
    }
}
