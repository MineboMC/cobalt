package net.minebo.cobalt.service.hologram;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Hologram {

    private final String id;
    private final int baseEntityId;
    private String worldName;
    private double x;
    private double y;
    private double z;
    private double lineSpacing = 0.28D;
    private double viewDistance = 32.0D;
    private boolean internal;
    private final List<String> lines = new ArrayList<>();

    public Hologram(String id, int baseEntityId, Location location) {
        this.id = id;
        this.baseEntityId = baseEntityId;
        setLocation(location);
    }

    public String id() { return id; }
    public int baseEntityId() { return baseEntityId; }
    public int entityId(int line) { return baseEntityId + line; }

    public Location location() {
        World world = worldName == null ? null : Bukkit.getWorld(worldName);
        if (world == null) return null;
        return new Location(world, x, y, z);
    }

    public void setLocation(Location location) {
        if (location == null || location.getWorld() == null) return;
        this.worldName = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
    }

    public List<String> lines() { return lines; }

    public void setLines(List<String> lines) {
        this.lines.clear();
        if (lines != null) this.lines.addAll(lines);
    }

    public void addLine(String line) { lines.add(line == null ? "" : line); }

    public boolean setLine(int index, String line) {
        if (index < 0 || index >= lines.size()) return false;
        lines.set(index, line == null ? "" : line);
        return true;
    }

    public boolean removeLine(int index) {
        if (index < 0 || index >= lines.size()) return false;
        lines.remove(index);
        return true;
    }

    public double lineSpacing() { return lineSpacing; }
    public void setLineSpacing(double lineSpacing) { this.lineSpacing = Math.max(0.1D, lineSpacing); }
    public double viewDistance() { return viewDistance; }
    public void setViewDistance(double viewDistance) { this.viewDistance = Math.max(4.0D, viewDistance); }
    public boolean internal() { return internal; }
    public void setInternal(boolean internal) { this.internal = internal; }

    public UUID lineUuid(int line) {
        return UUID.nameUUIDFromBytes(("holo:" + id + ":" + line).getBytes());
    }
}