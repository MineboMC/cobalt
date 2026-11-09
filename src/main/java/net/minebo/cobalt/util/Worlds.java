package net.minebo.cobalt.util;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class Worlds {

    public static World loadWorld(JavaPlugin plugin, String worldName) {

        World existing = Bukkit.getWorld(worldName);
        if (existing != null) {
            return existing;
        }

        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        if (!worldFolder.exists()) {
            plugin.getLogger().warning("World folder '" + worldName + "' not found. Generating a new one.");
            World world;
        }

        plugin.getLogger().info("Preparing to load world '" + worldName + "'...");

        try {
            World world = Bukkit.createWorld(new WorldCreator(worldName));
            plugin.getLogger().info("World '" + worldName + "' loaded successfully.");
            return world;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load world '" + worldName + "': " + e.getMessage());
            return null;
        }
    }

    public static CompletableFuture<World> loadWorldAsync(JavaPlugin plugin, String worldName) {
        CompletableFuture<World> future = new CompletableFuture<>();

        World existing = Bukkit.getWorld(worldName);
        if (existing != null) {
            future.complete(existing);
            return future;
        }

        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        if (!worldFolder.exists()) {
            plugin.getLogger().warning("World folder '" + worldName + "' not found. Generating a new one.");
            World world = Bukkit.createWorld(new WorldCreator(worldName));
            future.complete(world);
            return future;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getLogger().info("Preparing to load world '" + worldName + "'...");

            // Worlds can only be created on the main thread.
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    World world = Bukkit.createWorld(new WorldCreator(worldName));
                    future.complete(world);
                    plugin.getLogger().info("World '" + worldName + "' loaded successfully.");
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to load world '" + worldName + "': " + e.getMessage());
                    future.completeExceptionally(e);
                }
            });
        });

        return future;
    }
}
