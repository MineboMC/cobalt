package net.minebo.cobalt.service.scheduler.lunar;

import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.module.cooldown.Cooldown;
import com.lunarclient.apollo.module.cooldown.CooldownModule;
import com.lunarclient.apollo.common.icon.ItemStackIcon;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class LunarCooldownBridge {

    private LunarCooldownBridge() {
    }

    public static void send(Player player, String id, Material icon, long durationMillis) {
        if (Bukkit.getPluginManager().getPlugin("Apollo-Bukkit") == null) return;

        if (player == null || id == null) {
            return;
        }

        if (icon == null) return;

        Apollo.getPlayerManager().getPlayer(player.getUniqueId()).ifPresent(apolloPlayer -> {
            CooldownModule module = Apollo.getModuleManager().getModule(CooldownModule.class);
            Cooldown.CooldownBuilder builder = Cooldown.builder()
                    .name(id)
                    .duration(java.time.Duration.ofMillis(durationMillis));
            if (icon != null) {
                builder.icon(ItemStackIcon.builder()
                        .itemName(icon.name())
                        .build());
            }
            module.displayCooldown(apolloPlayer, builder.build());
        });
    }

    public static void clear(Player player, String id) {
        if (Bukkit.getPluginManager().getPlugin("Apollo-Bukkit") == null) return;

        if (player == null || id == null) {
            return;
        }

        Apollo.getPlayerManager().getPlayer(player.getUniqueId()).ifPresent(apolloPlayer -> {
            CooldownModule module = Apollo.getModuleManager().getModule(CooldownModule.class);
            module.removeCooldown(apolloPlayer, id);
        });
    }
}