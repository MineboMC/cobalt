package net.minebo.cobalt.service.deathmessage.event;

import net.minebo.cobalt.service.deathmessage.DeathContext;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired after a player's death has been analyzed, carrying the full damage
 * contribution breakdown. Other plugins can listen for this to grant assist
 * rewards, log kill statistics, etc. This event does not control the death
 * message itself — register a
 * {@code net.minebo.cobalt.service.death.DeathMessageProvider} for that.
 */
public final class PlayerDeathContributionEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final DeathContext context;

    public PlayerDeathContributionEvent(DeathContext context) {
        this.context = context;
    }

    public DeathContext context() {
        return context;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}