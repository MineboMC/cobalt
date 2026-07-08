package net.minebo.cobalt.scoreboard.provider;

import java.util.List;
import org.bukkit.entity.Player;

public class ScoreboardProvider {
    public String getModernTitle(Player player) {
       return "<light_purple><bold>MCRaidz";
    }

    public List<String> getModernLines(Player player) {
       return List.of("<red>coming soon...");
    }

    public String getLegacyTitle(Player player) {
        return getModernTitle(player);
    }

    public List<String> getLegacyLines(Player player) {
        return getModernLines(player);
    }

    public ScoreboardPriority getPriority() {
        return ScoreboardPriority.LOWEST;
    }
}
