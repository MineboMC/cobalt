package net.minebo.cobalt.service.store;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Set;

public interface StoreBackend {

    String getName();

    void save(String collection, String key, ConfigurationSection data);

    ConfigurationSection load(String collection, String key);

    Set<String> keys(String collection);

    void delete(String collection, String key);

    void reload();

    void flush();
}