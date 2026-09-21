package net.minebo.cobalt.service.npc;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.CobaltAPI;
import net.minebo.cobalt.service.CService;
import net.minebo.cobalt.service.npc.listener.NpcBukkitListener;
import net.minebo.cobalt.service.npc.packet.NpcInteractListener;
import net.minebo.cobalt.service.npc.api.NpcApi;
import net.minebo.cobalt.service.npc.packet.NpcPacketSender;
import net.minebo.cobalt.service.npc.task.NpcTickTask;
import net.minebo.cobalt.service.hologram.Hologram;
import net.minebo.cobalt.service.hologram.HologramService;
import net.minebo.cobalt.service.store.StoreService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.UUID;

public final class NpcService extends CService {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private NpcRegistry registry;
    private NpcSelection selection;
    private NpcPacketSender packets;
    private NpcVisibility visibility;
    private NpcLookTracker looks;
    private NpcApi api;
    private NpcStore store;
    private HologramService holograms;
    private NpcBukkitListener bukkitListener;
    private NpcInteractListener packetListener;
    private BukkitTask tickTask;

    @Override
    public String getName() {
        return "NPCs";
    }

    @Override
    public void onEnable() {
        registry = new NpcRegistry();
        selection = new NpcSelection();
        packets = new NpcPacketSender();
        visibility = new NpcVisibility();
        looks = new NpcLookTracker();
        api = new NpcApi(this);
        bukkitListener = new NpcBukkitListener(this);
        packetListener = new NpcInteractListener(this);

        Bukkit.getPluginManager().registerEvents(bukkitListener, Cobalt.getInstance());
        PacketEvents.getAPI().getEventManager().registerListener(packetListener);
        tickTask = new NpcTickTask(this).runTaskTimer(Cobalt.getInstance(), NpcTickTask.PERIOD_TICKS, NpcTickTask.PERIOD_TICKS);

        this.useStore(CobaltAPI.getStoreService());
        this.useHolograms(CobaltAPI.getHologramService());
    }

    @Override
    public void onDisable() {
        if (tickTask != null) {
            tickTask.cancel();
        }
        for (Npc npc : registry.all()) {
            hideAll(npc);
        }
        PacketEvents.getAPI().getEventManager().unregisterListener(packetListener);
    }

    public NpcApi api() {
        return api;
    }

    public NpcRegistry registry() {
        return registry;
    }

    public NpcSelection selection() {
        return selection;
    }

    public HologramService holograms() {
        return holograms;
    }

    public Hologram nameHologram(Npc npc) {
        return holograms == null || npc == null ? null : holograms.byId(hologramId(npc));
    }

    public Hologram ensureNameHologram(Npc npc) {
        syncNameHologram(npc);
        return nameHologram(npc);
    }

    public Npc create(Player creator, EntityType type, String name) {
        Npc npc = create(creator.getLocation(), type, name);
        selection.select(creator, npc);
        return npc;
    }

    public Npc create(Location location, EntityType type, String name) {
        Npc npc = new Npc(UUID.randomUUID(), registry.nextEntityId(), name, type, location);
        npc.setHideName(true);
        if (npc.isPlayerNpc()) {
            npc.setSkinMode(NpcSkinMode.VIEWER);
        } else {
            npc.setSkinMode(NpcSkinMode.NONE);
        }
        registry.register(npc);
        showInRange(npc);
        syncNameHologram(npc);
        save(npc);
        return npc;
    }

    public void save(Npc npc) {
        if (store != null && npc != null) {
            store.save(npc);
        }
    }

    public Npc byName(String name) {
        if (name == null) {
            return null;
        }
        for (Npc npc : registry.all()) {
            if (npc.name().equalsIgnoreCase(name)) {
                return npc;
            }
        }
        return null;
    }

    public boolean isNpcProfile(UUID profileId) {
        if (profileId == null) {
            return false;
        }
        for (Npc npc : registry.all()) {
            if (profileId.equals(npc.profileId()) || profileId.equals(npc.id())) {
                return true;
            }
        }
        return false;
    }

    public void useHolograms(HologramService hologramService) {
        this.holograms = hologramService;
        for (Npc npc : registry.all()) {
            syncNameHologram(npc);
        }
    }

    public void useStore(StoreService storeService) {
        if (storeService == null) {
            return;
        }
        store = new NpcStore(storeService, registry);
        store.loadAll();
        for (Npc npc : registry.all()) {
            showAll(npc);
            syncNameHologram(npc);
        }
    }

    public void delete(Npc npc) {
        deleteNameHologram(npc);
        hideAll(npc);
        looks.clearNpc(npc);
        if (store != null) {
            store.delete(npc);
        }
        registry.remove(npc.id());
    }

    public void teleportHere(Npc npc, Player player) {
        teleport(npc, player.getLocation());
    }

    public void teleport(Npc npc, Location location) {
        npc.setLocation(location);
        showInRange(npc);
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (visibility.isShown(viewer, npc)) {
                packets.teleport(viewer, npc);
            }
        }
        syncNameHologram(npc);
        save(npc);
    }

    public void setPose(Npc npc, NpcPose pose) {
        npc.setPose(pose);
        for (Player viewer : nearby(npc)) {
            packets.sendPose(viewer, npc);
        }
        syncNameHologram(npc);
        save(npc);
    }

    public void refreshEquipment(Npc npc) {
        for (Player viewer : nearby(npc)) {
            packets.sendEquipment(viewer, npc);
        }
        save(npc);
    }

    public void setDisplayName(Npc npc, String displayName) {
        npc.setDisplayName(displayName);
        Hologram hologram = ensureNameHologram(npc);
        if (hologram != null && holograms != null) {
            if (hologram.lines().isEmpty()) {
                holograms.addLine(hologram, displayName);
            } else {
                holograms.setLine(hologram, 0, displayName);
            }
        }
        save(npc);
    }

    public void setHideName(Npc npc, boolean hide) {
        npc.setHideName(true);
        syncNameHologram(npc);
        save(npc);
    }

    public void setSkinFromPlayer(Npc npc, Player source) {
        if (!npc.isPlayerNpc()) {
            return;
        }
        applySkinFromPlayer(npc, source);
        npc.setSkinMode(NpcSkinMode.FIXED);
        respawn(npc);
        save(npc);
    }

    public void setSkinFromUsername(Npc npc, String username, Player notify) {
        if (!npc.isPlayerNpc()) {
            if (notify != null) {
                notify.sendMessage(Component.text("Skins only work on player NPCs."));
            }
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(Cobalt.getInstance(), () -> {
            var skin = NpcSkinFetcher.fromUsername(username);
            Bukkit.getScheduler().runTask(Cobalt.getInstance(), () -> {
                if (skin.isEmpty()) {
                    if (notify != null) {
                        notify.sendMessage(Component.text("Could not fetch skin for " + username));
                    }
                    return;
                }
                setSkin(npc, skin.get());
                if (notify != null) {
                    notify.sendMessage(Component.text("Set " + npc.name() + " skin to " + username));
                }
            });
        });
    }

    public void setSkinMode(Npc npc, NpcSkinMode mode) {
        if (!npc.isPlayerNpc()) {
            return;
        }
        npc.setSkinMode(mode);
        if (mode == NpcSkinMode.VIEWER) {
            npc.setSkin(new NpcSkin("", "", ""));
        }
        respawn(npc);
        save(npc);
    }

    public void setSkin(Npc npc, NpcSkin skin) {
        if (!npc.isPlayerNpc()) {
            return;
        }
        npc.setSkin(skin);
        npc.setSkinMode(NpcSkinMode.FIXED);
        respawn(npc);
        save(npc);
    }

    public void respawn(Npc npc) {
        for (Player viewer : nearby(npc)) {
            packets.refresh(viewer, npc);
        }
    }

    public void show(Player viewer, Npc npc) {
        packets.spawn(viewer, npc);
        visibility.markShown(viewer, npc);
    }

    public void hide(Player viewer, Npc npc) {
        packets.despawn(viewer, npc);
        visibility.markHidden(viewer, npc);
    }

    public void showAll(Npc npc) {
        showInRange(npc);
    }

    public void showInRange(Npc npc) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            updateVisibility(viewer, npc);
        }
    }

    public void hideAll(Npc npc) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            hide(viewer, npc);
        }
    }

    public void tickViewers() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            for (Npc npc : registry.all()) {
                updateVisibility(viewer, npc);
                if (visibility.isShown(viewer, npc)) {
                    lookIfNeeded(viewer, npc);
                }
            }
        }
    }

    private void updateVisibility(Player viewer, Npc npc) {
        if (viewer.getTicksLived() < 40) {
            return;
        }
        Location location = npc.location();
        boolean inRange = location != null
                && location.getWorld() == viewer.getWorld()
                && location.distanceSquared(viewer.getLocation()) <= npc.viewDistance() * npc.viewDistance();
        boolean shown = visibility.isShown(viewer, npc);
        if (inRange && !shown) {
            show(viewer, npc);
        } else if (!inRange && shown) {
            hide(viewer, npc);
        }
    }

    private void lookIfNeeded(Player viewer, Npc npc) {
        Location location = npc.location();
        if (location == null || location.getWorld() != viewer.getWorld()) {
            return;
        }
        float targetYaw = npc.yaw();
        float targetPitch = npc.pitch();
        if (npc.lookAtPlayers() && location.distanceSquared(viewer.getLocation()) <= npc.lookDistance() * npc.lookDistance()) {
            Location from = location.clone().add(0.0D, eyeHeight(npc), 0.0D);
            Location look = from.setDirection(viewer.getEyeLocation().toVector().subtract(from.toVector()));
            targetYaw = look.getYaw();
            targetPitch = look.getPitch();
        }
        float[] current = looks.current(viewer, npc, npc.yaw(), npc.pitch());
        float nextYaw = lerpAngle(current[0], targetYaw, 0.28F);
        float nextPitch = lerpAngle(current[1], targetPitch, 0.28F);
        if (Math.abs(deltaAngle(current[0], nextYaw)) < 0.15F && Math.abs(deltaAngle(current[1], nextPitch)) < 0.15F) {
            return;
        }
        current[0] = nextYaw;
        current[1] = nextPitch;
        packets.look(viewer, npc, nextYaw, nextPitch);
    }

    private static double eyeHeight(Npc npc) {
        return switch (npc.pose()) {
            case SNEAKING, SITTING -> 1.27D;
            case SLEEPING, SWIMMING -> 0.40D;
            default -> 1.62D;
        };
    }

    private static float lerpAngle(float from, float to, float factor) {
        return from + deltaAngle(from, to) * factor;
    }

    private static float deltaAngle(float from, float to) {
        float delta = (to - from) % 360.0F;
        if (delta > 180.0F) {
            delta -= 360.0F;
        } else if (delta < -180.0F) {
            delta += 360.0F;
        }
        return delta;
    }

    public Npc selected(Player player) {
        UUID id = selection.get(player);
        return id == null ? null : registry.get(id);
    }

    public void handleClick(Player player, Npc npc) {
        if (npc.actions().isEmpty()) {
            player.sendMessage(Component.text("This NPC has no click actions."));
            return;
        }
        for (NpcAction action : npc.actions()) {
            String parsed = action.value()
                    .replace("%player%", player.getName())
                    .replace("%npc%", npc.name());
            switch (action.type()) {
                case MESSAGE -> player.sendMessage(LEGACY.deserialize(parsed));
                case PLAYER_COMMAND -> player.performCommand(stripSlash(parsed));
                case CONSOLE_COMMAND -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), stripSlash(parsed));
            }
        }
    }

    public void handleJoin(Player player) {
        Bukkit.getScheduler().runTaskLater(Cobalt.getInstance(), () -> {
            if (player.isOnline()) {
                tickViewers();
            }
        }, 10L);
    }

    public void handleQuit(Player player) {
        selection.clear(player);
        visibility.clear(player);
        looks.clear(player);
    }

    public void handleWorld(Player player) {
        for (Npc npc : registry.all()) {
            hide(player, npc);
        }
        tickViewers();
    }

    private void applySkinFromPlayer(Npc npc, Player source) {
        User user = PacketEvents.getAPI().getPlayerManager().getUser(source);
        if (user == null || user.getProfile() == null) {
            return;
        }
        List<TextureProperty> properties = user.getProfile().getTextureProperties();
        if (properties == null || properties.isEmpty()) {
            return;
        }
        TextureProperty property = properties.get(0);
        npc.setSkin(new NpcSkin(property.getValue(), property.getSignature(), source.getName()));
    }

    private List<Player> nearby(Npc npc) {
        Location location = npc.location();
        if (location == null || location.getWorld() == null) {
            return List.of();
        }
        return location.getWorld().getPlayers();
    }

    private void syncNameHologram(Npc npc) {
        if (holograms == null || npc == null) {
            return;
        }
        if (npc.location() == null) {
            return;
        }
        npc.setHideName(true);
        String id = hologramId(npc);
        Location location = npc.location().clone().add(0.0D, nameHeight(npc), 0.0D);
        Hologram hologram = holograms.byId(id);
        List<String> lines = npc.hologramLines().isEmpty() ? List.of(npc.displayName()) : List.copyOf(npc.hologramLines());
        if (hologram == null) {
            hologram = holograms.create(id, location, lines, true);
        } else {
            holograms.teleport(hologram, location);
        }
        hologram.setViewDistance(npc.viewDistance());
    }

    public void captureHologram(Npc npc) {
        Hologram hologram = nameHologram(npc);
        if (hologram == null) {
            return;
        }
        npc.setHologramLines(hologram.lines());
        save(npc);
    }

    private void deleteNameHologram(Npc npc) {
        if (holograms == null || npc == null) {
            return;
        }
        Hologram hologram = holograms.byId(hologramId(npc));
        if (hologram != null) {
            holograms.delete(hologram);
        }
    }

    private static String hologramId(Npc npc) {
        return "npc-" + npc.id();
    }

    private static double nameHeight(Npc npc) {
            return -0.25D;
    }

    private static String stripSlash(String value) {
        return value.startsWith("/") ? value.substring(1) : value;
    }
}