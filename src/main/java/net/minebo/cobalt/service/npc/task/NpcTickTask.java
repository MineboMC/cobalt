package net.minebo.cobalt.service.npc.task;

import net.minebo.cobalt.service.npc.NpcService;
import org.bukkit.scheduler.BukkitRunnable;

public final class NpcTickTask extends BukkitRunnable {

    public static final long PERIOD_TICKS = 1L;

    private final NpcService service;

    public NpcTickTask(NpcService service) {
        this.service = service;
    }

    @Override
    public void run() {
        service.tickViewers();
    }
}