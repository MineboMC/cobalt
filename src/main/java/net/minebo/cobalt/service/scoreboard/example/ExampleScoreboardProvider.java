package net.minebo.cobalt.service.scoreboard.example;

import net.minebo.cobalt.CobaltAPI;
import net.minebo.cobalt.service.scheduler.TimeFormat;
import net.minebo.cobalt.service.scoreboard.ScoreboardProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class ExampleScoreboardProvider implements ScoreboardProvider {

    @Override
    public String getTitle(Player player) {
        return "<light_purple><bold>Azarath</bold> <white>[Map 1] <head:" + player.getUniqueId() + ">";
    }

    @Override
    public List<String> getLines(Player player) {
        List<String> lines = new ArrayList<>();

        lines.add("<gray><strikethrough>------------------");
        if(CobaltAPI.getSchedulerService().isTimerActive(player, "sale")) {
            lines.add("<light_purple><bold>Sale</bold>: <red>" + TimeFormat.auto(CobaltAPI.getSchedulerService().getTimerRemaining(player, "sale")));
        }
        if(CobaltAPI.getSchedulerService().isCooldownActive(player, "enderpearl")) {
            lines.add("<dark_aqua><bold>Enderpearl</bold>: <red>" + TimeFormat.auto(CobaltAPI.getSchedulerService().getRemaining(player, "enderpearl")));
        }
        lines.add("<gray><strikethrough>------------------");

        return lines;
    }
}