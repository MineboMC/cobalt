package net.minebo.cobalt.service.hologram.task;

import net.minebo.cobalt.CobaltAPI;
import org.bukkit.scheduler.BukkitRunnable;

public final class HologramUpdateTask extends BukkitRunnable {

    @Override public void run() {
        CobaltAPI.getHologramService().tickViewers();
    }
}