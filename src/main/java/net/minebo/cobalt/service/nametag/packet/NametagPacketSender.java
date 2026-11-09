package net.minebo.cobalt.service.nametag.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.service.nametag.Nametag;
import net.minebo.cobalt.util.Coloring;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public final class NametagPacketSender {

    private static final int DISPLAY_ID_START = 1_500_000_000;
    /** Height above the player's head that the bottom extra line sits at, clearing the vanilla name. */
    private static final float LINES_OFFSET = 0.8F;
    private static final int BACKGROUND_COLOR = 0x40000000;
    private static final byte BILLBOARD_CENTER = 3;

    private final AtomicInteger nextDisplayId = new AtomicInteger(DISPLAY_ID_START);
    private final Map<UUID, Integer> displayIds = new ConcurrentHashMap<>();
    private final Map<Integer, UUID> targetsByEntityId = new ConcurrentHashMap<>();
    /** Viewer -> targets whose extra lines that viewer's client currently has spawned. */
    private final Map<UUID, Set<UUID>> shownLines = new ConcurrentHashMap<>();

    public void create(Player viewer, Player target, Nametag nametag) {
        targetsByEntityId.put(target.getEntityId(), target.getUniqueId());
        send(viewer, new WrapperPlayServerTeams(
                teamName(target.getUniqueId()),
                WrapperPlayServerTeams.TeamMode.CREATE,
                info(nametag),
                List.of(target.getName())
        ));
        if (target.isTrackedBy(viewer)) {
            showLines(viewer, target, nametag);
        }
    }

    public void update(Player viewer, Player target, Nametag previous, Nametag next) {
        send(viewer, new WrapperPlayServerTeams(
                teamName(target.getUniqueId()),
                WrapperPlayServerTeams.TeamMode.UPDATE,
                info(next),
                List.of()
        ));

        List<String> before = linesFor(viewer, target, previous);
        List<String> after = linesFor(viewer, target, next);
        if (before.equals(after)) {
            return;
        }
        if (after.isEmpty()) {
            hideLines(viewer, target.getUniqueId());
        } else if (isShowingLines(viewer, target.getUniqueId())) {
            sendLinesText(viewer, displayId(target.getUniqueId()), after);
        } else if (target.isTrackedBy(viewer)) {
            showLines(viewer, target, next);
        }
    }

    public void remove(Player viewer, UUID targetId) {
        send(viewer, new WrapperPlayServerTeams(
                teamName(targetId),
                WrapperPlayServerTeams.TeamMode.REMOVE,
                Optional.empty(),
                List.of()
        ));
        hideLines(viewer, targetId);
    }

    /**
     * Spawns the target's extra lines for the viewer as a text display riding the target.
     */
    public void showLines(Player viewer, Player target, Nametag nametag) {
        List<String> lines = linesFor(viewer, target, nametag);
        if (lines.isEmpty()) {
            hideLines(viewer, target.getUniqueId());
            return;
        }

        int id = displayId(target.getUniqueId());
        Location at = target.getLocation();
        send(viewer, new WrapperPlayServerDestroyEntities(id));
        send(viewer, new WrapperPlayServerSpawnEntity(
                id,
                Optional.of(UUID.randomUUID()),
                EntityTypes.TEXT_DISPLAY,
                new Vector3d(at.getX(), at.getY() + target.getHeight(), at.getZ()),
                0.0F, 0.0F, 0.0F, 0, Optional.empty()
        ));
        sendLinesText(viewer, id, lines);

        int[] passengers = IntStream.concat(
                target.getPassengers().stream().mapToInt(Entity::getEntityId),
                IntStream.of(id)
        ).toArray();
        send(viewer, new WrapperPlayServerSetPassengers(target.getEntityId(), passengers));
        shownLines.computeIfAbsent(viewer.getUniqueId(), key -> ConcurrentHashMap.newKeySet()).add(target.getUniqueId());
    }

    public void hideLines(Player viewer, UUID targetId) {
        Set<UUID> shown = shownLines.get(viewer.getUniqueId());
        if (shown == null || !shown.remove(targetId)) {
            return;
        }
        Integer id = displayIds.get(targetId);
        if (id != null) {
            send(viewer, new WrapperPlayServerDestroyEntities(id));
        }
    }

    public boolean isShowingLines(Player viewer, UUID targetId) {
        Set<UUID> shown = shownLines.get(viewer.getUniqueId());
        return shown != null && shown.contains(targetId);
    }

    public UUID targetOf(int entityId) {
        return targetsByEntityId.get(entityId);
    }

    /** Keeps the extra lines mounted when the server rewrites the target's passengers. */
    public int[] withLines(UUID targetId, int[] passengers) {
        Integer id = displayIds.get(targetId);
        if (id == null || Arrays.stream(passengers).anyMatch(passenger -> passenger == id)) {
            return passengers;
        }
        int[] merged = Arrays.copyOf(passengers, passengers.length + 1);
        merged[passengers.length] = id;
        return merged;
    }

    public void forgetViewer(UUID viewerId) {
        shownLines.remove(viewerId);
    }

    public void forgetTarget(Player target) {
        targetsByEntityId.remove(target.getEntityId());
        displayIds.remove(target.getUniqueId());
    }

    public static String teamName(UUID targetId) {
        String raw = targetId.toString().replace("-", "");
        return "nt" + raw.substring(0, 14);
    }

    private int displayId(UUID targetId) {
        return displayIds.computeIfAbsent(targetId, key -> nextDisplayId.getAndIncrement());
    }

    private static List<String> linesFor(Player viewer, Player target, Nametag nametag) {
        // Your own lines would float in front of your camera.
        if (nametag == null || !nametag.visible() || viewer.equals(target)) {
            return List.of();
        }
        return nametag.lines();
    }

    private void sendLinesText(Player viewer, int id, List<String> lines) {
        Component text = Component.join(JoinConfiguration.newlines(), lines.stream().map(NametagPacketSender::parse).toList());
        List<EntityData<?>> data = new ArrayList<>();
        data.add(new EntityData<>(11, EntityDataTypes.VECTOR3F, new Vector3f(0.0F, LINES_OFFSET, 0.0F)));
        data.add(new EntityData<>(15, EntityDataTypes.BYTE, BILLBOARD_CENTER));
        data.add(new EntityData<>(23, EntityDataTypes.ADV_COMPONENT, text));
        data.add(new EntityData<>(24, EntityDataTypes.INT, Integer.MAX_VALUE));
        data.add(new EntityData<>(25, EntityDataTypes.INT, BACKGROUND_COLOR));
        send(viewer, new WrapperPlayServerEntityMetadata(id, data));
    }

    private static WrapperPlayServerTeams.ScoreBoardTeamInfo info(Nametag nametag) {
        WrapperPlayServerTeams.NameTagVisibility visibility = nametag.visible()
                ? WrapperPlayServerTeams.NameTagVisibility.ALWAYS
                : WrapperPlayServerTeams.NameTagVisibility.NEVER;
        return new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                Component.empty(),
                parse(nametag.prefix()),
                parse(nametag.suffix()),
                visibility,
                WrapperPlayServerTeams.CollisionRule.ALWAYS,
                nametag.color(),
                WrapperPlayServerTeams.OptionData.NONE
        );
    }

    /**
     * Supports legacy (&) codes and MiniMessage tags in the same string.
     */
    private static Component parse(String text) {
        return Coloring.toComponent(text);
    }

    private void send(Player player, PacketWrapper<?> packet) {
        try {
            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet);
        } catch (Exception exception) {
            Cobalt.getInstance().getLogger().warning("[Nametag] Failed to send packet to "
                    + player.getName() + ": " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}
