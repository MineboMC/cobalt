package net.minebo.cobalt.service.scheduler.example;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minebo.cobalt.service.scheduler.timer.ScheduledTimer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class SaleTimer extends ScheduledTimer {

    @Override
    public String getId() {
        return "sale";
    }

    @Override
    public boolean isGlobal() {
        return true;
    }

    @Override
    public void onStart(Player player) {
        Bukkit.broadcast(Component.text("Store sale started!", NamedTextColor.GOLD));
    }

    @Override
    public void onTick(Player player, long remainingMillis) {
    }

    @Override
    public void onComplete(Player player) {
        Bukkit.broadcast(Component.text("Store sale ended.", NamedTextColor.RED));
    }

    @Override
    public void onCancel(Player player) {
        Bukkit.broadcast(Component.text("Store sale cancelled.", NamedTextColor.RED));
    }
}