package net.minebo.cobalt.service.npc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Npc {

    private final UUID id;
    private final int entityId;
    private final UUID profileId;
    private String name;
    private String displayName;
    private EntityType type;
    private String worldName;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private NpcPose pose = NpcPose.STANDING;
    private NpcSkinMode skinMode = NpcSkinMode.NONE;
    private NpcSkin skin = new NpcSkin("", "", "");
    private boolean hideName = true;
    private boolean lookAtPlayers = false;
    private double lookDistance = 8.0D;
    private double viewDistance = 30.0D;
    private final NpcEquipment equipment = new NpcEquipment();
    private final List<NpcAction> actions = new ArrayList<>();
    private final List<String> hologramLines = new ArrayList<>();

    public Npc(UUID id, int entityId, String name, EntityType type, Location location) {
        this(id, entityId, UUID.randomUUID(), name, type, location);
    }

    public Npc(UUID id, int entityId, UUID profileId, String name, EntityType type, Location location) {
        this.id = id;
        this.entityId = entityId;
        this.profileId = profileId == null ? UUID.randomUUID() : profileId;
        this.name = name == null || name.isEmpty() ? "NPC" : name;
        this.displayName = this.name;
        this.hologramLines.add(this.displayName);
        this.type = type == null ? EntityType.PLAYER : type;
        if (!isPlayerNpc()) {
            this.skinMode = NpcSkinMode.NONE;
        }
        setLocation(location);
    }

    public UUID id() { return id; }
    public int entityId() { return entityId; }
    public UUID profileId() { return profileId; }
    public String name() { return name; }

    public void setName(String name) {
        this.name = name == null || name.isEmpty() ? "NPC" : name;
    }

    public String displayName() {
        return displayName == null || displayName.isEmpty() ? name : displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName == null || displayName.isEmpty() ? name : displayName;
        if (hologramLines.isEmpty()) {
            hologramLines.add(this.displayName);
        }
    }

    public List<String> hologramLines() { return hologramLines; }

    public void setHologramLines(List<String> lines) {
        hologramLines.clear();
        if (lines != null) {
            hologramLines.addAll(lines);
        }
        if (!hologramLines.isEmpty()) {
            this.displayName = hologramLines.get(0);
        }
    }

    public EntityType type() { return type; }

    public void setType(EntityType type) {
        this.type = type == null ? EntityType.PLAYER : type;
        if (!isPlayerNpc()) {
            this.skinMode = NpcSkinMode.NONE;
            this.skin = new NpcSkin("", "", "");
        }
    }

    public boolean isPlayerNpc() { return type == EntityType.PLAYER; }

    public Location location() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        return new Location(world, x, y, z, yaw, pitch);
    }

    public void setLocation(Location location) {
        if (location == null || location.getWorld() == null) return;
        this.worldName = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    public float yaw() { return yaw; }
    public float pitch() { return pitch; }
    public NpcPose pose() { return pose; }
    public void setPose(NpcPose pose) { this.pose = pose == null ? NpcPose.STANDING : pose; }

    public NpcSkinMode skinMode() { return isPlayerNpc() ? skinMode : NpcSkinMode.NONE; }

    public void setSkinMode(NpcSkinMode skinMode) {
        if (!isPlayerNpc()) {
            this.skinMode = NpcSkinMode.NONE;
            return;
        }
        this.skinMode = skinMode == null ? NpcSkinMode.VIEWER : skinMode;
    }

    public NpcSkin skin() { return skin; }

    public void setSkin(NpcSkin skin) {
        if (!isPlayerNpc()) {
            this.skin = new NpcSkin("", "", "");
            this.skinMode = NpcSkinMode.NONE;
            return;
        }
        this.skin = skin == null ? new NpcSkin("", "", "") : skin;
        if (this.skin.isEmpty()) {
            this.skinMode = NpcSkinMode.VIEWER;
        } else if (this.skinMode == NpcSkinMode.NONE) {
            this.skinMode = NpcSkinMode.FIXED;
        }
    }

    public boolean hideName() { return hideName; }
    public void setHideName(boolean hideName) { this.hideName = hideName; }
    public boolean lookAtPlayers() { return lookAtPlayers; }
    public void setLookAtPlayers(boolean lookAtPlayers) { this.lookAtPlayers = lookAtPlayers; }
    public double lookDistance() { return lookDistance; }
    public void setLookDistance(double lookDistance) { this.lookDistance = Math.max(1.0D, lookDistance); }
    public double viewDistance() { return viewDistance; }
    public void setViewDistance(double viewDistance) { this.viewDistance = Math.max(1.0D, viewDistance); }
    public NpcEquipment equipment() { return equipment; }
    public List<NpcAction> actions() { return actions; }

    public boolean removeAction(int index) {
        if (index < 0 || index >= actions.size()) return false;
        actions.remove(index);
        return true;
    }
}