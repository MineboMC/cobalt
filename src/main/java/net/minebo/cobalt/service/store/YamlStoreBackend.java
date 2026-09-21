package net.minebo.cobalt.service.store;

import net.minebo.cobalt.Cobalt;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class YamlStoreBackend implements StoreBackend {

    private final File folder;
    private final Map<String, YamlConfiguration> files = new ConcurrentHashMap<>();

    public YamlStoreBackend(File folder) {
        this.folder = folder;
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    @Override
    public String getName() {
        return "yaml";
    }

    @Override
    public void save(String collection, String key, ConfigurationSection data) {
        YamlConfiguration yaml = file(collection);
        yaml.set(key, null);
        ConfigurationSection section = yaml.createSection(key);
        copy(data, section);
        write(collection, yaml);
    }

    @Override
    public ConfigurationSection load(String collection, String key) {
        return file(collection).getConfigurationSection(key);
    }

    @Override
    public Set<String> keys(String collection) {
        return file(collection).getKeys(false);
    }

    @Override
    public void delete(String collection, String key) {
        YamlConfiguration yaml = file(collection);
        yaml.set(key, null);
        write(collection, yaml);
    }

    @Override
    public void reload() {
        files.clear();
    }

    @Override
    public void flush() {
        for (String collection : files.keySet()) {
            write(collection, file(collection));
        }
    }

    private YamlConfiguration file(String collection) {
        return files.computeIfAbsent(collection, name -> {
            File file = new File(folder, name + ".yml");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException exception) {
                    Cobalt.getInstance().getLogger().warning("[Store] Could not create " + file.getName());
                }
            }
            return YamlConfiguration.loadConfiguration(file);
        });
    }

    private void write(String collection, YamlConfiguration yaml) {
        try {
            yaml.save(new File(folder, collection + ".yml"));
        } catch (IOException exception) {
            Cobalt.getInstance().getLogger().warning("[Store] Could not save " + collection + ".yml");
        }
    }

    private static void copy(ConfigurationSection from, ConfigurationSection to) {
        if (from == null) {
            return;
        }
        for (String key : from.getKeys(false)) {
            to.set(key, from.get(key));
        }
    }
}