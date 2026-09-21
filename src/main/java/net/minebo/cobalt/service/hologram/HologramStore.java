package net.minebo.cobalt.service.hologram;

import net.minebo.cobalt.service.hologram.registry.HologramRegistry;
import net.minebo.cobalt.service.store.MemoryConfiguration;
import net.minebo.cobalt.service.store.StoreService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;

public final class HologramStore {

    private static final String COLLECTION = "holograms";

    private final StoreService store;
    private final HologramRegistry registry;

    public HologramStore(StoreService store, HologramRegistry registry) {
        this.store = store;
        this.registry = registry;
    }

    public void save(Hologram hologram) {
        YamlConfiguration data = MemoryConfiguration.empty();
        data.set("id", hologram.id());
        data.set("internal", hologram.internal());
        data.set("lineSpacing", hologram.lineSpacing());
        data.set("viewDistance", hologram.viewDistance());
        Location location = hologram.location();
        if (location != null && location.getWorld() != null) {
            data.set("world", location.getWorld().getName());
            data.set("x", location.getX());
            data.set("y", location.getY());
            data.set("z", location.getZ());
        }
        data.set("lines", List.copyOf(hologram.lines()));
        store.save(COLLECTION, hologram.id(), data);
    }

    public void delete(Hologram hologram) {
        store.delete(COLLECTION, hologram.id());
    }

    public void loadAll() {
        for (String key : store.keys(COLLECTION)) {
            ConfigurationSection section = store.load(COLLECTION, key);
            if (section == null) {
                continue;
            }
            Hologram hologram = read(section);
            if (hologram != null) {
                registry.register(hologram);
            }
        }
    }

    private Hologram read(ConfigurationSection section) {
        try {
            String id = section.getString("id");
            if (id == null || id.isBlank()) {
                return null;
            }
            int entityId = registry.nextBaseEntityId();
            World world = Bukkit.getWorld(section.getString("world", "world"));
            Location location = new Location(
                    world,
                    section.getDouble("x"),
                    section.getDouble("y"),
                    section.getDouble("z")
            );
            Hologram hologram = new Hologram(id, entityId, location);
            hologram.setInternal(section.getBoolean("internal", false));
            hologram.setLineSpacing(section.getDouble("lineSpacing", 0.28D));
            hologram.setViewDistance(section.getDouble("viewDistance", 32.0D));
            List<String> lines = section.getStringList("lines");
            hologram.setLines(lines == null ? new ArrayList<>() : lines);
            return hologram;
        } catch (Exception exception) {
            return null;
        }
    }
}