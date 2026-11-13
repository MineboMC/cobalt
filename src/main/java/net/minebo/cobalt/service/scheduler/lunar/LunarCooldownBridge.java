package net.minebo.cobalt.service.scheduler.lunar;

import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.module.cooldown.Cooldown;
import com.lunarclient.apollo.module.cooldown.CooldownModule;
import com.lunarclient.apollo.common.icon.ItemStackIcon;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Every Apollo reference lives in {@link ApolloCooldowns}, which the JVM only loads once Apollo is
 * known to be installed. Referencing Apollo here directly throws NoClassDefFoundError without it.
 */
public final class LunarCooldownBridge {

    private LunarCooldownBridge() {
    }

    private static boolean hasApollo() {
        return Bukkit.getPluginManager().getPlugin("Apollo-Bukkit") != null;
    }

    public static void send(Player player, String id, Material icon, long durationMillis) {
        if (player == null || id == null || icon == null || !hasApollo()) {
            return;
        }
        try {
            ApolloCooldowns.send(player, id, icon, durationMillis);
        } catch (LinkageError ignored) {
            // an Apollo version without these classes
        }
    }

    public static void clear(Player player, String id) {
        if (player == null || id == null || !hasApollo()) {
            return;
        }
        try {
            ApolloCooldowns.clear(player, id);
        } catch (LinkageError ignored) {
        }
    }

    private static final class ApolloCooldowns {

        static void send(Player player, String id, Material icon, long durationMillis) {
            Apollo.getPlayerManager().getPlayer(player.getUniqueId()).ifPresent(apolloPlayer -> {
                CooldownModule module = Apollo.getModuleManager().getModule(CooldownModule.class);
                module.displayCooldown(apolloPlayer, Cooldown.builder()
                        .name(id)
                        .duration(java.time.Duration.ofMillis(durationMillis))
                        .icon(ItemStackIcon.builder().itemName(icon.name()).build())
                        .build());
            });
        }

        static void clear(Player player, String id) {
            Apollo.getPlayerManager().getPlayer(player.getUniqueId()).ifPresent(apolloPlayer -> {
                CooldownModule module = Apollo.getModuleManager().getModule(CooldownModule.class);
                module.removeCooldown(apolloPlayer, id);
            });
        }
    }
}
