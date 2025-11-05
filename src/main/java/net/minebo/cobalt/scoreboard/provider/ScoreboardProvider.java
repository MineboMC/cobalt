package net.minebo.cobalt.scoreboard.provider;

import java.util.List;
import org.bukkit.entity.Player;

public class ScoreboardProvider {
    public String getModernTitle(Player player) {
       return "&d&lMCRaidz";
    }

    public List<String> getModernLines(Player player) {
       return List.of("&ccoming soon...");
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
