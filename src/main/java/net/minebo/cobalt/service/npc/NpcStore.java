package net.minebo.cobalt.service.npc;

import net.minebo.cobalt.service.store.MemoryConfiguration;
import net.minebo.cobalt.service.store.StoreService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class NpcStore {

    private static final String COLLECTION = "npcs";

    private final StoreService store;
    private final NpcRegistry registry;

    public NpcStore(StoreService store, NpcRegistry registry) {
        this.store = store;
        this.registry = registry;
    }

    public void save(Npc npc) {
        YamlConfiguration data = MemoryConfiguration.empty();
        data.set("id", npc.id().toString());
        data.set("profileId", npc.profileId().toString());
        data.set("name", npc.name());
        data.set("displayName", npc.displayName());
        data.set("type", npc.type().name());
        Location location = npc.location();
        if (location != null && location.getWorld() != null) {
            data.set("world", location.getWorld().getName());
            data.set("x", location.getX());
            data.set("y", location.getY());
            data.set("z", location.getZ());
            data.set("yaw", location.getYaw());
            data.set("pitch", location.getPitch());
        }
        data.set("pose", npc.pose().name());
        data.set("hideName", true);
        data.set("hologram.lines", List.copyOf(npc.hologramLines()));
        data.set("lookAtPlayers", npc.lookAtPlayers());
        data.set("lookDistance", npc.lookDistance());
        data.set("viewDistance", npc.viewDistance());
        data.set("skinMode", npc.skinMode().name());
        data.set("skin.value", npc.skin().value());
        data.set("skin.signature", npc.skin().signature());
        data.set("skin.source", npc.skin().sourceName());
        data.set("equipment.helmet", npc.equipment().helmet());
        data.set("equipment.chestplate", npc.equipment().chestplate());
        data.set("equipment.leggings", npc.equipment().leggings());
        data.set("equipment.boots", npc.equipment().boots());
        data.set("equipment.hand", npc.equipment().hand());
        data.set("equipment.offHand", npc.equipment().offHand());
        int index = 0;
        for (NpcAction action : npc.actions()) {
            data.set("actions." + index + ".type", action.type().name());
            data.set("actions." + index + ".value", action.value());
            index++;
        }
        store.save(COLLECTION, npc.id().toString(), data);
    }

    public void delete(Npc npc) {
        store.delete(COLLECTION, npc.id().toString());
    }

    public void loadAll() {
        for (String key : store.keys(COLLECTION)) {
            ConfigurationSection section = store.load(COLLECTION, key);
            if (section == null) {
                continue;
            }
            Npc npc = read(section);
            if (npc != null) {
                registry.register(npc);
            }
        }
    }

    private Npc read(ConfigurationSection section) {
        try {
            UUID id = UUID.fromString(section.getString("id"));
            int entityId = registry.nextEntityId();
            UUID profileId = UUID.fromString(section.getString("profileId", UUID.randomUUID().toString()));
            String name = section.getString("name", "NPC");
            EntityType type = EntityType.valueOf(section.getString("type", "PLAYER"));
            World world = Bukkit.getWorld(section.getString("world", "world"));
            Location location = new Location(
                    world,
                    section.getDouble("x"),
                    section.getDouble("y"),
                    section.getDouble("z"),
                    (float) section.getDouble("yaw"),
                    (float) section.getDouble("pitch")
            );
            Npc npc = new Npc(id, entityId, profileId, name, type, location);
            npc.setDisplayName(section.getString("displayName", name));
            List<String> hologramLines = section.getStringList("hologram.lines");
            if (hologramLines == null || hologramLines.isEmpty()) {
                hologramLines = List.of(npc.displayName());
            }
            npc.setHologramLines(hologramLines);
            npc.setPose(NpcPose.valueOf(section.getString("pose", "STANDING")));
            npc.setHideName(section.getBoolean("hideName", true));
            if (type != EntityType.PLAYER) {
                npc.setSkinMode(NpcSkinMode.NONE);
            }
            npc.setLookAtPlayers(section.getBoolean("lookAtPlayers", false));
            npc.setLookDistance(section.getDouble("lookDistance", 8.0D));
            npc.setViewDistance(section.getDouble("viewDistance", 30.0D));
            npc.setSkinMode(NpcSkinMode.valueOf(section.getString("skinMode", "VIEWER").toUpperCase(Locale.ROOT)));
            npc.setSkin(new NpcSkin(
                    section.getString("skin.value", ""),
                    section.getString("skin.signature", ""),
                    section.getString("skin.source", "")
            ));
            npc.equipment().setHelmet(item(section, "equipment.helmet"));
            npc.equipment().setChestplate(item(section, "equipment.chestplate"));
            npc.equipment().setLeggings(item(section, "equipment.leggings"));
            npc.equipment().setBoots(item(section, "equipment.boots"));
            npc.equipment().setHand(item(section, "equipment.hand"));
            npc.equipment().setOffHand(item(section, "equipment.offHand"));
            ConfigurationSection actions = section.getConfigurationSection("actions");
            if (actions != null) {
                for (String actionKey : actions.getKeys(false)) {
                    ConfigurationSection action = actions.getConfigurationSection(actionKey);
                    if (action == null) {
                        continue;
                    }
                    npc.actions().add(new NpcAction(
                            NpcActionType.valueOf(action.getString("type", "MESSAGE")),
                            action.getString("value", "")
                    ));
                }
            }
            return npc;
        } catch (Exception exception) {
            return null;
        }
    }

    private static ItemStack item(ConfigurationSection section, String path) {
        Object value = section.get(path);
        return value instanceof ItemStack item ? item : null;
    }
}