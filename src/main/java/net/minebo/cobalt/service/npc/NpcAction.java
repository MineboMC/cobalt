package net.minebo.cobalt.service.npc;

public final class NpcAction {

    private final NpcActionType type;
    private final String value;

    public NpcAction(NpcActionType type, String value) {
        this.type = type;
        this.value = value == null ? "" : value;
    }

    public NpcActionType type() { return type; }
    public String value() { return value; }
}