package net.minebo.cobalt.service.store;

import org.bukkit.configuration.file.YamlConfiguration;

public final class MemoryConfiguration {

    private MemoryConfiguration() {
    }

    public static YamlConfiguration empty() {
        return new YamlConfiguration();
    }
}