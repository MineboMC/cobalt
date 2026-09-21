package net.minebo.cobalt.service.scheduler.example;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.service.scheduler.cooldown.Cooldown;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import java.util.concurrent.TimeUnit;

public final class EnderpearlCooldown extends Cooldown {

    @Override
    public String getId() {
        return "enderpearl";
    }

    @Override
    public Material getItem() {
        return Material.ENDER_PEARL;
    }

    @Override
    public void onApply(Player player) {
        player.sendMessage(Component.text("Enderpearl cooldown started.", NamedTextColor.RED));
    }

    @Override
    public void onExpire(Player player) {
        player.sendMessage(Component.text("You can pearl again.", NamedTextColor.GREEN));
    }

    @EventHandler
    public void onPearl(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof EnderPearl)) {
            return;
        }
        if (!(event.getEntity().getShooter() instanceof Player player)) {
            return;
        }
        if (scheduler().onCooldown(player, this)) {
            event.setCancelled(true);
            long seconds = Math.max(1L, scheduler().getRemaining(player, this) / 1000L);
            player.sendMessage(Component.text("Pearl cooldown: " + seconds + "s", NamedTextColor.RED));
            return;
        }

        Bukkit.getScheduler().runTask(Cobalt.getInstance(), () -> {
            if (!player.isOnline()) {
                return;
            }
            scheduler().applyCooldown(player, this, 16, TimeUnit.SECONDS);
        });
    }
}