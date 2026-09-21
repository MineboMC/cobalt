package net.minebo.cobalt.service;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServiceManager {
    public static HashMap<JavaPlugin, List<CService>> services = new HashMap<>();

    public static void register(JavaPlugin plugin, CService... service) {
        for (CService s : service) {
            register(plugin, s);
        }
    }

    public static void register(JavaPlugin plugin, CService service) {
        services.computeIfAbsent(plugin, k -> new ArrayList<>()).add(service);
        service.onEnable();
    }

    public static List<CService> getServices(JavaPlugin plugin) {
        return services.get(plugin);
    }
}