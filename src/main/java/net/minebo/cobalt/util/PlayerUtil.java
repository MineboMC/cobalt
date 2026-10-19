package net.minebo.cobalt.util;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import net.minebo.cobalt.Cobalt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerUtil {

    public static Integer getPing(Player player) {
        return player.getPing();
    }

    // ported from qlib
    public static void animateDeath(Player player) {
        animateDeath(player, player.getTrackedBy());
    }

    public static void animateDeath(Player player, Player watcher) {
        animateDeath(player, Collections.singleton(watcher));
    }

    private static void animateDeath(Player player, Collection<? extends Player> watchers) {
        if (watchers.isEmpty()) return;

        int entityId = SpigotReflectionUtil.generateEntityId(); // or your EntityUtils.getFakeEntityId()
        UUID fakeUuid = UUID.randomUUID(); // must NOT be the real player's UUID (see notes)

        // Copy the real player's skin onto a throwaway profile
        List<TextureProperty> textures = new ArrayList<>();
        for (ProfileProperty prop : player.getPlayerProfile().getProperties()) {
            if (prop.getName().equals("textures")) {
                textures.add(new TextureProperty(prop.getName(), prop.getValue(), prop.getSignature()));
            }
        }
        UserProfile profile = new UserProfile(fakeUuid, player.getName(), textures);

        // 1. Tab-list entry (unlisted) so the client can resolve the skin
        WrapperPlayServerPlayerInfoUpdate infoPacket = new WrapperPlayServerPlayerInfoUpdate(
                EnumSet.of(
                        WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
                        WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED),
                new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                        profile, false, 0, GameMode.SURVIVAL, null, null));

        // 2. Spawn the fake player entity at the real player's position
        Location loc = player.getLocation();
        WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(
                entityId,
                Optional.of(fakeUuid),
                EntityTypes.PLAYER,
                new Vector3d(loc.getX(), loc.getY(), loc.getZ()),
                loc.getPitch(),
                loc.getYaw(),
                loc.getYaw(), // head yaw
                0,
                Optional.empty());

        // 3. Show all skin layers (index 17 on 1.21.x: displayed skin parts)
        WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(
                entityId,
                List.of(new EntityData(17, EntityDataTypes.BYTE, (byte) 0x7F)));

        // 4. Status 3 = death animation
        WrapperPlayServerEntityStatus statusPacket = new WrapperPlayServerEntityStatus(entityId, 3);

        for (Player watcher : watchers) {
            if (watcher.getUniqueId().equals(player.getUniqueId())) continue;
            send(watcher, infoPacket);
            send(watcher, spawnPacket);
            send(watcher, metadataPacket);
            send(watcher, statusPacket);
        }

        // 5. Clean up: destroy the entity and remove the tab entry
        List<Player> sentTo = new ArrayList<>(watchers);
        Bukkit.getScheduler().runTaskLater(Cobalt.getInstance(), () -> {
            WrapperPlayServerDestroyEntities destroyPacket = new WrapperPlayServerDestroyEntities(entityId);
            WrapperPlayServerPlayerInfoRemove removePacket = new WrapperPlayServerPlayerInfoRemove(fakeUuid);
            for (Player watcher : sentTo) {
                if (!watcher.isOnline()) continue;
                send(watcher, destroyPacket);
                send(watcher, removePacket);
            }
        }, 40L);
    }

    private static void send(Player target, com.github.retrooper.packetevents.wrapper.PacketWrapper<?> packet) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(target, packet);
    }

}
