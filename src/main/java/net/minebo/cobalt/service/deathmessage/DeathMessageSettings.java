package net.minebo.cobalt.service.deathmessage;

public final class DeathMessageSettings {

    private boolean hideWeapons = false;
    private boolean broadcastGlobally = true;
    private boolean showAssists = true;
    private long assistWindowMillis = 30_000L;
    private double minimumAssistDamage = 1.0D;

    public boolean hideWeapons() { return hideWeapons; }
    public void setHideWeapons(boolean hideWeapons) { this.hideWeapons = hideWeapons; }

    public boolean broadcastGlobally() { return broadcastGlobally; }
    public void setBroadcastGlobally(boolean broadcastGlobally) { this.broadcastGlobally = broadcastGlobally; }

    public boolean showAssists() { return showAssists; }
    public void setShowAssists(boolean showAssists) { this.showAssists = showAssists; }

    /** How far back to look for contributing damage when a player dies. */
    public long assistWindowMillis() { return assistWindowMillis; }
    public void setAssistWindowMillis(long assistWindowMillis) { this.assistWindowMillis = Math.max(0L, assistWindowMillis); }

    /** Minimum damage a player must have dealt within the window to count as an assist. */
    public double minimumAssistDamage() { return minimumAssistDamage; }
    public void setMinimumAssistDamage(double minimumAssistDamage) { this.minimumAssistDamage = Math.max(0.0D, minimumAssistDamage); }
}