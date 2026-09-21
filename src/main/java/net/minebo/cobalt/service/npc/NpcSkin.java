package net.minebo.cobalt.service.npc;

public final class NpcSkin {

    private final String value;
    private final String signature;
    private final String sourceName;

    public NpcSkin(String value, String signature, String sourceName) {
        this.value = value == null ? "" : value;
        this.signature = signature == null ? "" : signature;
        this.sourceName = sourceName == null ? "" : sourceName;
    }

    public boolean isEmpty() { return value.isEmpty(); }
    public String value() { return value; }
    public String signature() { return signature; }
    public String sourceName() { return sourceName; }
}