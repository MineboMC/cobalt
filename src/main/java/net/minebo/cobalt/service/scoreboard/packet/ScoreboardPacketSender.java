package net.minebo.cobalt.service.scoreboard.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.score.ScoreFormat;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisplayScoreboard;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerResetScore;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore;
import net.kyori.adventure.text.Component;
import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.service.scoreboard.state.PlayerScoreboardState;
import org.bukkit.entity.Player;

import java.util.List;

public final class ScoreboardPacketSender {

    private static final int SIDEBAR = 1;

    public void create(Player player, Component title) {
        send(player, new WrapperPlayServerScoreboardObjective(
                PlayerScoreboardState.OBJECTIVE,
                WrapperPlayServerScoreboardObjective.ObjectiveMode.CREATE,
                title,
                WrapperPlayServerScoreboardObjective.RenderType.INTEGER,
                ScoreFormat.blankScore()
        ));
        send(player, new WrapperPlayServerDisplayScoreboard(SIDEBAR, PlayerScoreboardState.OBJECTIVE));
    }

    public void updateTitle(Player player, Component title) {
        send(player, new WrapperPlayServerScoreboardObjective(
                PlayerScoreboardState.OBJECTIVE,
                WrapperPlayServerScoreboardObjective.ObjectiveMode.UPDATE,
                title,
                WrapperPlayServerScoreboardObjective.RenderType.INTEGER,
                ScoreFormat.blankScore()
        ));
    }

    public void setLine(Player player, int index, Component text) {
        send(player, new WrapperPlayServerUpdateScore(
                holder(index),
                WrapperPlayServerUpdateScore.Action.CREATE_OR_UPDATE_ITEM,
                PlayerScoreboardState.OBJECTIVE,
                PlayerScoreboardState.MAX_LINES - index,
                text,
                ScoreFormat.blankScore()
        ));
    }

    public void clearLine(Player player, int index) {
        send(player, new WrapperPlayServerResetScore(holder(index), PlayerScoreboardState.OBJECTIVE));
    }

    public void setLines(Player player, PlayerScoreboardState state, List<Component> lines) {
        int oldCount = state.lines().size();
        int newCount = lines.size();
        int shared = Math.min(oldCount, newCount);

        for (int index = 0; index < shared; index++) {
            if (!state.lines().get(index).equals(lines.get(index))) {
                setLine(player, index, lines.get(index));
            }
        }
        for (int index = shared; index < newCount; index++) {
            setLine(player, index, lines.get(index));
        }
        for (int index = newCount; index < oldCount; index++) {
            clearLine(player, index);
        }
    }

    public void destroy(Player player, PlayerScoreboardState state) {
        for (int index = 0; index < state.lines().size(); index++) {
            clearLine(player, index);
        }
        send(player, new WrapperPlayServerDisplayScoreboard(SIDEBAR, ""));
        send(player, new WrapperPlayServerScoreboardObjective(
                PlayerScoreboardState.OBJECTIVE,
                WrapperPlayServerScoreboardObjective.ObjectiveMode.REMOVE,
                Component.empty(),
                null
        ));
    }

    public static String holder(int index) {
        return "\u00A7" + Integer.toHexString(index) + "\u00A7r";
    }

    private void send(Player player, com.github.retrooper.packetevents.wrapper.PacketWrapper<?> packet) {
        try {
            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet);
        } catch (Exception exception) {
            Cobalt.getInstance().getLogger().warning("[Scoreboard] Failed to send packet to "
                    + player.getName() + ": " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}