package net.minebo.cobalt.service.store;

import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.service.CService;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.Set;

public final class StoreService extends CService {

    private StoreBackend backend;

    @Override
    public String getName() {
        return "Store";
    }

    @Override
    public void onEnable() {
        backend = new YamlStoreBackend(Cobalt.getInstance().getDataFolder());
    }

    @Override
    public void onDisable() {
        if (backend != null) {
            backend.flush();
        }
    }

    public StoreBackend backend() {
        return backend;
    }

    public void setBackend(StoreBackend backend) {
        if (this.backend != null) {
            this.backend.flush();
        }
        this.backend = backend;
    }

    public void save(String collection, String key, ConfigurationSection data) {
        backend.save(collection, key, data);
    }

    public ConfigurationSection load(String collection, String key) {
        return backend.load(collection, key);
    }

    public Set<String> keys(String collection) {
        return backend.keys(collection);
    }

    public void delete(String collection, String key) {
        backend.delete(collection, key);
    }
}