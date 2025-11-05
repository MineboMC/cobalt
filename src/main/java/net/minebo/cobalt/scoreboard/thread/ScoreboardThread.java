package net.minebo.cobalt.scoreboard.thread;

import java.util.ArrayList;
import net.minebo.cobalt.scoreboard.ScoreboardHandler;
import net.minebo.cobalt.scoreboard.provider.ScoreboardProvider;
import net.minebo.cobalt.util.ColorUtil;
import net.minebo.cobalt.util.VersionUtils;

public class ScoreboardThread {
    public static void start() {
        Thread scoreboardThread = new Thread(() -> {
            while (true) {
                ScoreboardHandler.boards.values().forEach((board) -> {
                    ScoreboardProvider provider = ScoreboardHandler.getHighestPriorityProvider();
                    if (provider != null) {
                        boolean modern = VersionUtils.isModern(board.getPlayer());

                        String rawTitle;
                        java.util.List<String> rawLines;
                        if (modern) {
                            rawTitle = provider.getModernTitle(board.getPlayer());
                            rawLines = provider.getModernLines(board.getPlayer());
                        } else {
                            rawTitle = provider.getLegacyTitle(board.getPlayer());
                            rawLines = provider.getLegacyLines(board.getPlayer());
                        }

                        board.updateTitle(ColorUtil.translateColors(rawTitle));
                        ArrayList<String> scores = new ArrayList<>();
                        for (String line : rawLines) {
                            scores.add(ColorUtil.translateColors(line));
                        }
                        board.updateLines(scores);
                    }
                });

                try {
                    Thread.sleep(20L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "Cobalt - Scoreboard Thread");
        scoreboardThread.setDaemon(true);
        scoreboardThread.start();
    }
}