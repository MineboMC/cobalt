package net.minebo.cobalt.service.nametag.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.service.nametag.Nametag;
import net.minebo.cobalt.util.Coloring;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class NametagPacketSender {

    public void create(Player viewer, Player target, Nametag nametag) {
        send(viewer, new WrapperPlayServerTeams(
                teamName(target.getUniqueId()),
                WrapperPlayServerTeams.TeamMode.CREATE,
                info(nametag),
                List.of(target.getName())
        ));
    }

    public void update(Player viewer, Player target, Nametag nametag) {
        send(viewer, new WrapperPlayServerTeams(
                teamName(target.getUniqueId()),
                WrapperPlayServerTeams.TeamMode.UPDATE,
                info(nametag),
                List.of()
        ));
    }

    public void remove(Player viewer, UUID targetId) {
        send(viewer, new WrapperPlayServerTeams(
                teamName(targetId),
                WrapperPlayServerTeams.TeamMode.REMOVE,
                Optional.empty(),
                List.of()
        ));
    }

    public static String teamName(UUID targetId) {
        String raw = targetId.toString().replace("-", "");
        return "nt" + raw.substring(0, 14);
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
                NamedTextColor.WHITE,
                WrapperPlayServerTeams.OptionData.NONE
        );
    }

    /**
     * Supports legacy (&) codes and MiniMessage tags in the same string.
     */
    private static Component parse(String text) {
        return Coloring.toComponent(text);
    }

    private void send(Player player, WrapperPlayServerTeams packet) {
        try {
            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet);
        } catch (Exception exception) {
            Cobalt.getInstance().getLogger().warning("[Nametag] Failed to send packet to "
                    + player.getName() + ": " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}