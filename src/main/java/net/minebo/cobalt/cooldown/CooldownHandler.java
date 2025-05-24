package net.minebo.cobalt.cooldown;

import net.minebo.cobalt.cooldown.construct.Cooldown;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class CooldownHandler {

    public Map<String, Cooldown> cooldownMap;
    public JavaPlugin plugin;

    public CooldownHandler(JavaPlugin plugin) {
        cooldownMap = new HashMap<>();
        this.plugin = plugin;
    }

    public void registerCooldown(String id, Cooldown cooldown) {
        cooldownMap.put(id.toLowerCase(), cooldown);
        Bukkit.getPluginManager().registerEvents(cooldown, plugin);
    }

    public Cooldown getCooldown(String id) {
        return cooldownMap.get(id.toLowerCase());
    }
}
