package net.minebo.cobalt.scoreboard.provider;

import java.util.List;
import org.bukkit.entity.Player;

public class ScoreboardProvider {
    public String getTitle(Player player) {
       return "&d&lMCRaidz";
    }

    public List<String> getLines(Player player) {
       return List.of("&ccoming soon...");
    }

    public ScoreboardPriority getPriority() {
        return ScoreboardPriority.LOWEST;
    }
}
