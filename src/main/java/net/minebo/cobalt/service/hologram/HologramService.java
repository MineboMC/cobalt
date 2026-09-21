package net.minebo.cobalt.service.hologram;

import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.CobaltAPI;
import net.minebo.cobalt.service.CService;
import net.minebo.cobalt.service.command.defaults.command.HologramCommand;
import net.minebo.cobalt.service.hologram.api.HologramApi;
import net.minebo.cobalt.service.hologram.listener.HologramListener;
import net.minebo.cobalt.service.hologram.packet.HologramPacketSender;
import net.minebo.cobalt.service.hologram.registry.HologramRegistry;
import net.minebo.cobalt.service.hologram.state.HologramTracker;
import net.minebo.cobalt.service.hologram.task.HologramUpdateTask;
import net.minebo.cobalt.service.store.StoreService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public final class HologramService extends CService {

    private HologramRegistry registry;
    private HologramTracker tracker;
    private HologramPacketSender packets;
    private HologramApi api;
    private HologramListener listener;
    private HologramStore store;
    private BukkitTask task;

    @Override
    public String getName() {
        return "Holograms";
    }

    @Override
    public void onEnable() {
        registry = new HologramRegistry();
        tracker = new HologramTracker();
        packets = new HologramPacketSender();
        api = new HologramApi(this);
        listener = new HologramListener(this);
        Bukkit.getPluginManager().registerEvents(listener, Cobalt.getInstance());
        task = new HologramUpdateTask().runTaskTimer(Cobalt.getInstance(), 5, 20L);

        this.useStore(CobaltAPI.getStoreService());
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
        }
        HandlerList.unregisterAll(listener);
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (Hologram hologram : registry.all()) {
                hide(player, hologram);
            }
        }
        tracker.clear();
        registry.clear();
    }

    public HologramApi api() { return api; }
    public HologramRegistry registry() { return registry; }

    public Hologram create(String id, Location location, List<String> lines, Boolean internal) {
        Hologram existing = registry.get(id);
        if (existing != null) {
            delete(existing);
        }
        Hologram hologram = new Hologram(id, registry.nextBaseEntityId(), location);
        hologram.setLines(lines);
        registry.register(hologram);
        showInRange(hologram);
        hologram.setInternal(internal);
        save(hologram);
        return hologram;
    }

    public Hologram create(String id, Location location, List<String> lines) {
        Hologram existing = registry.get(id);
        if (existing != null) {
            delete(existing);
        }
        Hologram hologram = new Hologram(id, registry.nextBaseEntityId(), location);
        hologram.setLines(lines);
        registry.register(hologram);
        showInRange(hologram);
        save(hologram);
        return hologram;
    }

    public Hologram create(Player creator, String id) {
        return create(id, creator.getLocation(), List.of("&f" + id));
    }

    public void delete(Hologram hologram) {
        hideAll(hologram);
        if (store != null) {
            store.delete(hologram);
        }
        registry.remove(hologram.id());
    }

    public void useStore(StoreService storeService) {
        if (storeService == null) {
            return;
        }
        store = new HologramStore(storeService, registry);
        store.loadAll();
        for (Hologram hologram : registry.all()) {
            showInRange(hologram);
        }
    }

    public void save(Hologram hologram) {
        if (store != null && hologram != null && !hologram.internal()) {
            store.save(hologram);
        }
    }

    public Hologram byId(String id) {
        return registry.get(id);
    }

    public void setLines(Hologram hologram, List<String> lines) {
        hologram.setLines(lines);
        refresh(hologram);
        save(hologram);
    }

    public void addLine(Hologram hologram, String line) {
        hologram.addLine(line);
        refresh(hologram);
        save(hologram);
    }

    public boolean setLine(Hologram hologram, int index, String line) {
        if (!hologram.setLine(index, line)) {
            return false;
        }
        refresh(hologram);
        save(hologram);
        return true;
    }

    public boolean removeLine(Hologram hologram, int index) {
        if (!hologram.removeLine(index)) {
            return false;
        }
        refresh(hologram);
        save(hologram);
        return true;
    }

    public void teleport(Hologram hologram, Location location) {
        hologram.setLocation(location);
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (tracker.isShown(viewer, hologram)) {
                packets.teleport(viewer, hologram, hologram.lines());
            }
        }
        showInRange(hologram);
        save(hologram);
    }

    public void teleportHere(Hologram hologram, Player player) {
        teleport(hologram, player.getLocation());
    }

    public void setViewDistance(Hologram hologram, double blocks) {
        hologram.setViewDistance(blocks);
        showInRange(hologram);
        save(hologram);
    }

    public void refresh(Hologram hologram) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (tracker.isShown(viewer, hologram)) {
                List<String> next = List.copyOf(hologram.lines());
                packets.update(viewer, hologram, tracker.lastLines(viewer, hologram), next);
                tracker.setLastLines(viewer, hologram, next);
            }
        }
    }

    public void tickViewers() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            for (Hologram hologram : registry.all()) {
                updateVisibility(viewer, hologram);
            }
        }
    }

    public void handleJoin(Player player) {
        Bukkit.getScheduler().runTaskLater(Cobalt.getInstance(), () -> {
            if (player.isOnline()) {
                tickViewers();
            }
        }, 40L);
    }

    public void handleQuit(Player player) {
        tracker.clear(player);
    }

    public void handleWorld(Player player) {
        for (Hologram hologram : registry.all()) {
            hide(player, hologram);
        }
        tickViewers();
    }

    private void updateVisibility(Player viewer, Hologram hologram) {
        if (viewer.getTicksLived() < 40) {
            return;
        }
        Location location = hologram.location();
        boolean inRange = location != null
                && location.getWorld() == viewer.getWorld()
                && location.distanceSquared(viewer.getLocation()) <= hologram.viewDistance() * hologram.viewDistance();
        boolean shown = tracker.isShown(viewer, hologram);
        if (inRange && !shown) {
            show(viewer, hologram);
        } else if (!inRange && shown) {
            hide(viewer, hologram);
        }
    }

    private void show(Player viewer, Hologram hologram) {
        List<String> lines = List.copyOf(hologram.lines());
        packets.spawn(viewer, hologram, lines);
        tracker.markShown(viewer, hologram);
        tracker.setLastLines(viewer, hologram, lines);
    }

    private void hide(Player viewer, Hologram hologram) {
        packets.despawn(viewer, hologram, Math.max(hologram.lines().size(), 8));
        tracker.markHidden(viewer, hologram);
    }

    private void showInRange(Hologram hologram) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            updateVisibility(viewer, hologram);
        }
    }

    private void hideAll(Hologram hologram) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            hide(viewer, hologram);
        }
    }
}