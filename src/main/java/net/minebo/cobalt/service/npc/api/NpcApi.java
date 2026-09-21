package net.minebo.cobalt.service.npc.api;

import net.minebo.cobalt.service.npc.Npc;
import net.minebo.cobalt.service.npc.NpcAction;
import net.minebo.cobalt.service.npc.NpcActionType;
import net.minebo.cobalt.service.npc.NpcPose;
import net.minebo.cobalt.service.npc.NpcService;
import net.minebo.cobalt.service.npc.NpcSkin;
import net.minebo.cobalt.service.npc.NpcSkinMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.UUID;

public final class NpcApi {

    private final NpcService service;

    public NpcApi(NpcService service) {
        this.service = service;
    }

    public Npc create(Location location, EntityType type, String name) {
        return service.create(location, type, name);
    }

    public Npc createPlayer(Location location, String name, NpcSkin skin) {
        Npc npc = service.create(location, EntityType.PLAYER, name);
        if (skin != null && !skin.isEmpty()) service.setSkin(npc, skin);
        return npc;
    }

    public Npc createPlayer(Location location, String name, String textureValue, String textureSignature) {
        return createPlayer(location, name, new NpcSkin(textureValue, textureSignature, name));
    }

    public void setSkin(Npc npc, NpcSkin skin) { service.setSkin(npc, skin); }

    public void setSkin(Npc npc, String textureValue, String textureSignature) {
        service.setSkin(npc, new NpcSkin(textureValue, textureSignature, npc.name()));
    }

    public void setSkinFromUsername(Npc npc, String username) {
        service.setSkinFromUsername(npc, username, null);
    }

    public void setSkinMode(Npc npc, NpcSkinMode mode) { service.setSkinMode(npc, mode); }

    public void setEquipment(Npc npc, ItemStack helmet, ItemStack chest, ItemStack legs, ItemStack boots, ItemStack hand, ItemStack offHand) {
        npc.equipment().setHelmet(helmet);
        npc.equipment().setChestplate(chest);
        npc.equipment().setLeggings(legs);
        npc.equipment().setBoots(boots);
        npc.equipment().setHand(hand);
        npc.equipment().setOffHand(offHand);
        service.refreshEquipment(npc);
    }

    public void setPose(Npc npc, NpcPose pose) { service.setPose(npc, pose); }

    public void addAction(Npc npc, NpcActionType type, String value) {
        npc.actions().add(new NpcAction(type, value));
        service.save(npc);
    }

    public void removeAction(Npc npc, int index) {
        npc.removeAction(index);
        service.save(npc);
    }

    public void clearActions(Npc npc) {
        npc.actions().clear();
        service.save(npc);
    }

    public void setDisplayName(Npc npc, String displayName) { service.setDisplayName(npc, displayName); }
    public void setHideName(Npc npc, boolean hide) { service.setHideName(npc, hide); }

    public void setLookAtPlayers(Npc npc, boolean enabled, double distance) {
        npc.setLookAtPlayers(enabled);
        npc.setLookDistance(distance);
        service.save(npc);
    }

    public void setViewDistance(Npc npc, double blocks) {
        npc.setViewDistance(blocks);
        service.save(npc);
    }

    public void teleport(Npc npc, Location location) { service.teleport(npc, location); }
    public void delete(Npc npc) { service.delete(npc); }
    public Npc byName(String name) { return service.byName(name); }
    public Npc byId(UUID id) { return service.registry().get(id); }
    public Collection<Npc> all() { return service.registry().all(); }
    public boolean isNpcProfile(UUID profileId) { return service.isNpcProfile(profileId); }
    public boolean isNpcEntity(int entityId) { return service.registry().byEntityId(entityId) != null; }
    public void show(Player viewer, Npc npc) { service.show(viewer, npc); }
    public void hide(Player viewer, Npc npc) { service.hide(viewer, npc); }
}