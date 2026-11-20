package net.minebo.cobalt.service.hologram.clickable;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import net.minebo.cobalt.Cobalt;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Every live ClickableHologram. Each tick works out which line each viewer's looking at, and any click
 * (a swing, using an item, or hitting a hologram's click box) runs that line's action.
 */
public final class ClickableHolograms extends PacketListenerAbstract implements Listener {

    private static final double REACH = 12D;
    private static final int CLICK_COOLDOWN_TICKS = 4; // one click can arrive as a swing and an interact

    private final List<ClickableHologram> holograms = new CopyOnWriteArrayList<>();
    private final Map<UUID, Integer> lastClick = new HashMap<>();
    private BukkitTask task;

    public ClickableHolograms() {
        super(PacketListenerPriority.NORMAL);
    }

    public void start() {
        Bukkit.getPluginManager().registerEvents(this, Cobalt.getInstance());
        PacketEvents.getAPI().getEventManager().registerListener(this);
        task = Bukkit.getScheduler().runTaskTimer(Cobalt.getInstance(), this::tick, 1L, 1L);
    }

    public void stop() {
        if (task != null) task.cancel();
        HandlerList.unregisterAll(this);
        PacketEvents.getAPI().getEventManager().unregisterListener(this);
        List.copyOf(holograms).forEach(ClickableHologram::remove);
    }

    void track(ClickableHologram hologram) {
        holograms.add(hologram);
    }

    void untrack(ClickableHologram hologram) {
        holograms.remove(hologram);
    }

    private void tick() {
        for (ClickableHologram hologram : holograms) {
            if (!hologram.getViewer().isOnline()) continue;
            hologram.hover(hologram.lineUnderCrosshair(REACH));
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        PacketType.Play.Client type = event.getPacketType() instanceof PacketType.Play.Client client ? client : null;
        if (type != PacketType.Play.Client.INTERACT_ENTITY && type != PacketType.Play.Client.ANIMATION && type != PacketType.Play.Client.USE_ITEM) return;
        if (!(event.getPlayer() instanceof Player player)) return;
        // swings come from every player, most of them have no hologram to click
        if (holograms.stream().noneMatch(hologram -> hologram.getViewer() == player)) return;

        if (type == PacketType.Play.Client.INTERACT_ENTITY) {
            int entityId = new WrapperPlayClientInteractEntity(event).getEntityId();
            if (holograms.stream().noneMatch(hologram -> hologram.getViewer() == player && hologram.owns(entityId))) return;
            event.setCancelled(true); // the server's never heard of it
        }

        Bukkit.getScheduler().runTask(Cobalt.getInstance(), () -> click(player));
    }

    private void click(Player player) {
        int tick = Bukkit.getCurrentTick();
        Integer last = lastClick.get(player.getUniqueId());
        if (last != null && tick - last < CLICK_COOLDOWN_TICKS) return;

        for (ClickableHologram hologram : holograms) {
            if (hologram.getViewer() == player && hologram.click()) {
                lastClick.put(player.getUniqueId(), tick);
                return;
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastClick.remove(event.getPlayer().getUniqueId());
        holograms.removeIf(hologram -> hologram.getViewer() == event.getPlayer());
    }
}
