package net.minebo.cobalt.service.tablist.state;

import net.kyori.adventure.text.Component;
import net.minebo.cobalt.service.tablist.TabSlot;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

public final class PlayerTabState {

    public static final int MAX_SLOTS = 80;

    private final UUID playerId;
    private final UUID[] slotIds;
    private final TabSlot[] slots;
    private int slotCount;
    private Component header = Component.empty();
    private Component footer = Component.empty();
    private boolean applied;

    public PlayerTabState(UUID playerId) {
        this.playerId = playerId;
        this.slotIds = new UUID[MAX_SLOTS];
        this.slots = new TabSlot[MAX_SLOTS];
        for (int index = 0; index < MAX_SLOTS; index++) {
            this.slotIds[index] = UUID.nameUUIDFromBytes(
                    ("cobalt-tab:" + playerId + ":" + index).getBytes(StandardCharsets.UTF_8)
            );
            this.slots[index] = TabSlot.EMPTY;
        }
    }

    public UUID playerId() {
        return playerId;
    }

    public int slotCount() {
        return slotCount;
    }

    public void setSlotCount(int slotCount) {
        this.slotCount = Math.max(0, Math.min(MAX_SLOTS, slotCount));
    }

    public UUID slotId(int index) {
        return slotIds[index];
    }

    public TabSlot slot(int index) {
        return slots[index];
    }

    public void setSlot(int index, TabSlot slot) {
        slots[index] = slot == null ? TabSlot.EMPTY : slot;
    }

    public Component header() {
        return header;
    }

    public Component footer() {
        return footer;
    }

    public boolean headerFooterChanged(Component header, Component footer) {
        return !Objects.equals(this.header, header) || !Objects.equals(this.footer, footer);
    }

    public void setHeaderFooter(Component header, Component footer) {
        this.header = header == null ? Component.empty() : header;
        this.footer = footer == null ? Component.empty() : footer;
    }

    public boolean isApplied() {
        return applied;
    }

    public void setApplied(boolean applied) {
        this.applied = applied;
    }

    public boolean owns(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        for (int index = 0; index < slotCount; index++) {
            if (slotIds[index].equals(uuid)) {
                return true;
            }
        }
        return false;
    }
}