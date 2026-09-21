package net.minebo.cobalt.service.hologram.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.service.hologram.Hologram;
import net.minebo.cobalt.util.Coloring;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class HologramPacketSender {

    private static final int MAX_LINES = 32;

    public void spawn(Player viewer, Hologram hologram, List<String> lines) {
        despawn(viewer, hologram, MAX_LINES);
        org.bukkit.Location base = hologram.location();
        if (base == null) return;
        int count = Math.min(lines.size(), MAX_LINES);
        for (int i = 0; i < count; i++) {
            spawnLine(viewer, hologram, i, lines.get(i), count);
        }
    }

    public void update(Player viewer, Hologram hologram, List<String> previous, List<String> next) {
        if (previous.size() != next.size()) {
            spawn(viewer, hologram, next);
            return;
        }
        for (int i = 0; i < next.size() && i < MAX_LINES; i++) {
            if (!previous.get(i).equals(next.get(i))) {
                sendMetadata(viewer, hologram.entityId(i), translateLine(viewer, next.get(i)));
            }
        }
    }

    public void teleport(Player viewer, Hologram hologram, List<String> lines) {
        org.bukkit.Location base = hologram.location();
        if (base == null) return;
        int count = Math.min(lines.size(), MAX_LINES);
        for (int i = 0; i < count; i++) {
            org.bukkit.Location at = lineLocation(base, hologram.lineSpacing(), i, count);
            send(viewer, new WrapperPlayServerEntityTeleport(
                    hologram.entityId(i),
                    new Location(new Vector3d(at.getX(), at.getY(), at.getZ()), 0.0F, 0.0F),
                    false
            ));
        }
    }

    public void despawn(Player viewer, Hologram hologram, int lineCount) {
        int count = Math.max(lineCount, MAX_LINES);
        int[] ids = new int[count];
        for (int i = 0; i < count; i++) ids[i] = hologram.entityId(i);
        send(viewer, new WrapperPlayServerDestroyEntities(ids));
    }

    private void spawnLine(Player viewer, Hologram hologram, int index, String text, int total) {
        org.bukkit.Location at = lineLocation(hologram.location(), hologram.lineSpacing(), index, total);
        send(viewer, new WrapperPlayServerSpawnEntity(
                hologram.entityId(index),
                Optional.of(hologram.lineUuid(index)),
                EntityTypes.ARMOR_STAND,
                new Vector3d(at.getX(), at.getY(), at.getZ()),
                0.0F, 0.0F, 0.0F, 0, Optional.empty()
        ));
        sendMetadata(viewer, hologram.entityId(index), translateLine(viewer, text));
    }

    private void sendMetadata(Player viewer, int entityId, String text) {
        boolean empty = text == null || text.isEmpty();
        List<EntityData<?>> data = new ArrayList<>();
        data.add(new EntityData<>(0, EntityDataTypes.BYTE, (byte) 0x20));
        // Legacy (&) codes and MiniMessage tags both work here.
        data.add(new EntityData<>(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT,
                empty ? Optional.empty() : Optional.of(Coloring.toComponent(text))));
        data.add(new EntityData<>(3, EntityDataTypes.BOOLEAN, !empty));
        data.add(new EntityData<>(5, EntityDataTypes.BOOLEAN, true));
        send(viewer, new WrapperPlayServerEntityMetadata(entityId, data));
    }

    private static org.bukkit.Location lineLocation(org.bukkit.Location base, double spacing, int index, int total) {
        double y = base.getY() + ((total - 1 - index) * spacing);
        return new org.bukkit.Location(base.getWorld(), base.getX(), y, base.getZ());
    }

    private void send(Player player, PacketWrapper<?> packet) {
        try {
            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet);
        } catch (Exception exception) {
            Cobalt.getInstance().getLogger().warning("[Hologram] Failed packet to " + player.getName() + ": " + exception.getMessage());
        }
    }

    public String translateLine(Player viewer, String string) {
        if (string == null) return "";
        return string.replace("%player%", viewer.getName());
    }
}