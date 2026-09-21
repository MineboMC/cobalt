package net.minebo.cobalt.service.nametag.state;

import net.minebo.cobalt.service.nametag.Nametag;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ViewerNametagState {

    private final UUID viewerId;
    private final Map<UUID, Nametag> sent = new ConcurrentHashMap<>();

    public ViewerNametagState(UUID viewerId) {
        this.viewerId = viewerId;
    }

    public UUID viewerId() {
        return viewerId;
    }

    public Nametag get(UUID targetId) {
        return sent.get(targetId);
    }

    public void put(UUID targetId, Nametag nametag) {
        sent.put(targetId, nametag);
    }

    public Nametag remove(UUID targetId) {
        return sent.remove(targetId);
    }

    public Map<UUID, Nametag> all() {
        return sent;
    }
}