package net.minebo.cobalt.service.npc.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.service.npc.Npc;
import net.minebo.cobalt.service.npc.NpcEquipment;
import net.minebo.cobalt.service.npc.NpcSkin;
import net.minebo.cobalt.service.npc.NpcSkinMode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public final class NpcPacketSender {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private static final EnumSet<WrapperPlayServerPlayerInfoUpdate.Action> ADD_ACTIONS = EnumSet.of(
            WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_GAME_MODE
    );

    public void spawn(Player viewer, Npc npc) {
        org.bukkit.Location bukkit = npc.location();
        if (bukkit == null || viewer.getWorld() != bukkit.getWorld()) {
            return;
        }
        if (npc.isPlayerNpc()) {
            send(viewer, new WrapperPlayServerPlayerInfoUpdate(ADD_ACTIONS, List.of(playerInfo(npc, viewer))));
            hideProfileName(viewer, npc);
        }
        send(viewer, new WrapperPlayServerSpawnEntity(
                npc.entityId(),
                Optional.of(npc.isPlayerNpc() ? npc.profileId() : npc.id()),
                mapType(npc.type()),
                new Vector3d(bukkit.getX(), bukkit.getY(), bukkit.getZ()),
                bukkit.getPitch(),
                bukkit.getYaw(),
                bukkit.getYaw(),
                0,
                Optional.empty()
        ));
        send(viewer, new WrapperPlayServerEntityHeadLook(npc.entityId(), bukkit.getYaw()));
        sendMetadata(viewer, npc);
        sendSkinParts(viewer, npc);
        sendEquipment(viewer, npc);
        hideProfileName(viewer, npc);
        if (npc.isPlayerNpc()) {
            // Remove the tab-list entry once the client has had time to resolve the skin
            Bukkit.getScheduler().runTaskLater(Cobalt.getInstance(), () -> {
                if (viewer.isOnline()) {
                    send(viewer, new WrapperPlayServerPlayerInfoRemove(List.of(npc.profileId())));
                }
            }, 40L);
        }
    }

    public void despawn(Player viewer, Npc npc) {
        send(viewer, new WrapperPlayServerDestroyEntities(npc.entityId()));
        if (npc.isPlayerNpc()) {
            send(viewer, new WrapperPlayServerPlayerInfoRemove(List.of(npc.profileId())));
            send(viewer, new WrapperPlayServerTeams(
                    teamName(npc),
                    WrapperPlayServerTeams.TeamMode.REMOVE,
                    Optional.empty(),
                    List.of()
            ));
        }
    }

    public void teleport(Player viewer, Npc npc) {
        org.bukkit.Location bukkit = npc.location();
        if (bukkit == null) {
            return;
        }
        Location location = new Location(
                new Vector3d(bukkit.getX(), bukkit.getY(), bukkit.getZ()),
                bukkit.getYaw(),
                bukkit.getPitch()
        );
        send(viewer, new WrapperPlayServerEntityTeleport(npc.entityId(), location, false));
        send(viewer, new WrapperPlayServerEntityHeadLook(npc.entityId(), bukkit.getYaw()));
    }

    public void sendPose(Player viewer, Npc npc) {
        sendMetadata(viewer, npc);
    }

    public void sendName(Player viewer, Npc npc) {
        hideProfileName(viewer, npc);
    }

    /**
     * Player metadata indices shifted in 1.21.9 (Avatar refactor, protocol 773):
     * displayed skin parts moved from index 17 to index 16.
     * Sending a Byte to the wrong index makes the client throw and disconnect.
     */
    private static int skinPartsIndex() {
        int protocol = PacketEvents.getAPI().getServerManager().getVersion().getProtocolVersion();
        return protocol >= 773 ? 16 : 17;
    }

    private void sendMetadata(Player viewer, Npc npc) {
        byte flags = 0;
        EntityPose pose = EntityPose.STANDING;
        switch (npc.pose()) {
            case SNEAKING -> {
                flags = 0x02;
                pose = EntityPose.CROUCHING;
            }
            case SPRINTING -> flags = 0x08;
            case SWIMMING -> {
                flags = 0x10;
                pose = EntityPose.SWIMMING;
            }
            case SLEEPING -> pose = EntityPose.SLEEPING;
            default -> {
            }
        }
        List<EntityData<?>> data = new ArrayList<>();
        data.add(new EntityData<>(0, EntityDataTypes.BYTE, flags));
        data.add(new EntityData<>(6, EntityDataTypes.ENTITY_POSE, pose));
        if (npc.isPlayerNpc()) {
            // Displayed skin parts (cape/jacket/sleeves/pants/hat), index depends on version
            data.add(new EntityData<>(skinPartsIndex(), EntityDataTypes.BYTE, (byte) 0x7F));
        }
        send(viewer, new WrapperPlayServerEntityMetadata(npc.entityId(), data));
    }

    private void sendSkinParts(Player viewer, Npc npc) {
        if (!npc.isPlayerNpc()) {
            return;
        }
        send(viewer, new WrapperPlayServerEntityMetadata(
                npc.entityId(),
                List.of(new EntityData<>(skinPartsIndex(), EntityDataTypes.BYTE, (byte) 0x7F))
        ));
    }

    public void look(Player viewer, Npc npc, float yaw, float pitch) {
        send(viewer, new WrapperPlayServerEntityRotation(npc.entityId(), yaw, pitch, true));
        send(viewer, new WrapperPlayServerEntityHeadLook(npc.entityId(), yaw));
    }

    private void hideProfileName(Player viewer, Npc npc) {
        if (!npc.isPlayerNpc()) {
            return;
        }
        try {
            send(viewer, new WrapperPlayServerTeams(
                    teamName(npc),
                    WrapperPlayServerTeams.TeamMode.REMOVE,
                    Optional.empty(),
                    List.of()
            ));
            send(viewer, new WrapperPlayServerTeams(
                    teamName(npc),
                    WrapperPlayServerTeams.TeamMode.CREATE,
                    new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                            Component.empty(),
                            Component.empty(),
                            Component.empty(),
                            WrapperPlayServerTeams.NameTagVisibility.NEVER,
                            WrapperPlayServerTeams.CollisionRule.NEVER,
                            NamedTextColor.WHITE,
                            WrapperPlayServerTeams.OptionData.NONE
                    ),
                    List.of(profileName(npc))
            ));
        } catch (Exception ignored) {
        }
    }

    public void sendEquipment(Player viewer, Npc npc) {
        NpcEquipment equipment = npc.equipment();
        List<Equipment> list = new ArrayList<>();
        list.add(slot(EquipmentSlot.HELMET, equipment.helmet()));
        list.add(slot(EquipmentSlot.CHEST_PLATE, equipment.chestplate()));
        list.add(slot(EquipmentSlot.LEGGINGS, equipment.leggings()));
        list.add(slot(EquipmentSlot.BOOTS, equipment.boots()));
        list.add(slot(EquipmentSlot.MAIN_HAND, equipment.hand()));
        list.add(slot(EquipmentSlot.OFF_HAND, equipment.offHand()));
        send(viewer, new WrapperPlayServerEntityEquipment(npc.entityId(), list));
    }

    public void refresh(Player viewer, Npc npc) {
        despawn(viewer, npc);
        spawn(viewer, npc);
    }

    private WrapperPlayServerPlayerInfoUpdate.PlayerInfo playerInfo(Npc npc, Player viewer) {
        UserProfile profile = new UserProfile(npc.profileId(), profileName(npc));
        NpcSkin skin = resolveSkin(npc, viewer);
        if (skin != null && !skin.isEmpty() && skin.signature() != null && !skin.signature().isBlank()) {
            profile.setTextureProperties(List.of(new TextureProperty("textures", skin.value(), skin.signature())));
        }
        return new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                profile,
                false,
                0,
                GameMode.SURVIVAL,
                Component.empty(),
                null
        );
    }

    private static NpcSkin resolveSkin(Npc npc, Player viewer) {
        if (!npc.isPlayerNpc() || npc.skinMode() == NpcSkinMode.NONE) {
            return new NpcSkin("", "", "");
        }
        if (npc.skinMode() == NpcSkinMode.VIEWER) {
            return skinOf(viewer);
        }
        if (npc.skinMode() == NpcSkinMode.FIXED) {
            return npc.skin();
        }
        return npc.skin().isEmpty() ? skinOf(viewer) : npc.skin();
    }

    public static NpcSkin skinOf(Player player) {
        if (player == null) {
            return new NpcSkin("", "", "");
        }
        var user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        if (user == null || user.getProfile() == null) {
            return new NpcSkin("", "", player.getName());
        }
        var properties = user.getProfile().getTextureProperties();
        if (properties == null || properties.isEmpty()) {
            return new NpcSkin("", "", player.getName());
        }
        TextureProperty textures = null;
        for (TextureProperty property : properties) {
            if ("textures".equalsIgnoreCase(property.getName())) {
                textures = property;
                break;
            }
        }
        if (textures == null) {
            textures = properties.get(0);
        }
        return new NpcSkin(textures.getValue(), textures.getSignature(), player.getName());
    }

    private static Equipment slot(EquipmentSlot slot, org.bukkit.inventory.ItemStack item) {
        ItemStack stack;
        if (item == null || item.getType().isAir()) {
            stack = SpigotConversionUtil.fromBukkitItemStack(new org.bukkit.inventory.ItemStack(Material.AIR));
        } else {
            stack = SpigotConversionUtil.fromBukkitItemStack(item);
        }
        return new Equipment(slot, stack);
    }

    private static com.github.retrooper.packetevents.protocol.entity.type.EntityType mapType(EntityType type) {
        if (type == null || type == EntityType.PLAYER) {
            return EntityTypes.PLAYER;
        }
        var mapped = EntityTypes.getByName(type.getKey().toString());
        return mapped == null ? EntityTypes.ARMOR_STAND : mapped;
    }

    private static String profileName(Npc npc) {
        String name = "npc" + Integer.toHexString(npc.entityId());
        return name.length() <= 16 ? name : name.substring(0, 16);
    }

    private static String teamName(Npc npc) {
        return "cnpc" + Integer.toHexString(npc.entityId());
    }

    private void send(Player player, PacketWrapper<?> packet) {
        try {
            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet);
        } catch (Exception exception) {
            Cobalt.getInstance().getLogger().warning("[NPC] Failed packet to " + player.getName() + ": " + exception.getMessage());
        }
    }
}