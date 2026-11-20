package net.minebo.cobalt.service.nametag.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.score.ScoreFormat;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisplayScoreboard;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerResetScore;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore;
import net.kyori.adventure.text.Component;
import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.service.nametag.Nametag;
import net.minebo.cobalt.util.Coloring;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NametagPacketSender {

    private static final String BELOW_NAME = "cobalt_below";
    private static final int BELOW_NAME_SLOT = 2;

    /** Viewers whose client has the below-name objective, and of those, the ones it's showing for. */
    private final Set<UUID> belowNameCreated = ConcurrentHashMap.newKeySet();
    private final Set<UUID> belowNameShown = ConcurrentHashMap.newKeySet();

    public void create(Player viewer, Player target, Nametag nametag) {
        send(viewer, new WrapperPlayServerTeams(
                teamName(target.getUniqueId()),
                WrapperPlayServerTeams.TeamMode.CREATE,
                info(nametag),
                List.of(target.getName())
        ));
        if (!nametag.belowName().isEmpty()) setBelowName(viewer, target, nametag.belowName());
    }

    public void update(Player viewer, Player target, Nametag previous, Nametag next) {
        send(viewer, new WrapperPlayServerTeams(
                teamName(target.getUniqueId()),
                WrapperPlayServerTeams.TeamMode.UPDATE,
                info(next),
                List.of()
        ));

        if (next.belowName().equals(previous.belowName())) return;
        if (next.belowName().isEmpty()) {
            send(viewer, new WrapperPlayServerResetScore(target.getName(), BELOW_NAME));
        } else {
            setBelowName(viewer, target, next.belowName());
        }
    }

    public void remove(Player viewer, UUID targetId, String targetName) {
        send(viewer, new WrapperPlayServerTeams(
                teamName(targetId),
                WrapperPlayServerTeams.TeamMode.REMOVE,
                Optional.empty(),
                List.of()
        ));
        if (belowNameCreated.contains(viewer.getUniqueId()) && targetName != null) {
            send(viewer, new WrapperPlayServerResetScore(targetName, BELOW_NAME));
        }
    }

    /**
     * The below-name line is on for everyone once it's shown, a name without a score of its own gets an empty
     * one, so it's only shown while someone the viewer can see has a line.
     */
    public void showBelowName(Player viewer, boolean show) {
        if (show == belowNameShown.contains(viewer.getUniqueId())) return;

        if (show) {
            createBelowName(viewer);
            send(viewer, new WrapperPlayServerDisplayScoreboard(BELOW_NAME_SLOT, BELOW_NAME));
            belowNameShown.add(viewer.getUniqueId());
        } else {
            send(viewer, new WrapperPlayServerDisplayScoreboard(BELOW_NAME_SLOT, ""));
            belowNameShown.remove(viewer.getUniqueId());
        }
    }

    public void forgetViewer(UUID viewerId) {
        belowNameCreated.remove(viewerId);
        belowNameShown.remove(viewerId);
    }

    public static String teamName(UUID targetId) {
        String raw = targetId.toString().replace("-", "");
        return "nt" + raw.substring(0, 14);
    }

    /** The whole line is the score's fixed text, the objective's own title is empty. */
    private void setBelowName(Player viewer, Player target, String line) {
        createBelowName(viewer);
        send(viewer, new WrapperPlayServerUpdateScore(
                target.getName(),
                WrapperPlayServerUpdateScore.Action.CREATE_OR_UPDATE_ITEM,
                BELOW_NAME,
                0,
                null,
                ScoreFormat.fixedScore(parse(line))
        ));
    }

    private void createBelowName(Player viewer) {
        if (!belowNameCreated.add(viewer.getUniqueId())) return;

        send(viewer, new WrapperPlayServerScoreboardObjective(
                BELOW_NAME,
                WrapperPlayServerScoreboardObjective.ObjectiveMode.CREATE,
                Component.empty(),
                WrapperPlayServerScoreboardObjective.RenderType.INTEGER,
                ScoreFormat.blankScore()
        ));
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
