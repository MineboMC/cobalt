package net.minebo.cobalt.scoreboard.animation;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minebo.cobalt.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TextAnimation {

    private final String text;
    private final String color1;        // MiniMessage color (e.g. "<red>", "<#ff0000>")
    private final String color2;
    private final AnimationType animationType;
    private final boolean bold;
    private final JavaPlugin plugin;

    private List<Frame> frames;
    private int currentStage = 0;
    private long lastStageTime = System.currentTimeMillis();
    private BukkitTask task;

    public TextAnimation(JavaPlugin plugin, String text, String color1, String color2) {
        this(plugin, text, color1, color2, AnimationType.DEFAULT, true);
    }

    public TextAnimation(JavaPlugin plugin, String text, String color1, String color2, AnimationType animationType) {
        this(plugin, text, color1, color2, animationType, true);
    }

    public TextAnimation(JavaPlugin plugin, String text, String color1, String color2, AnimationType animationType, boolean bold) {
        this.plugin = plugin;
        this.text = text;
        this.color1 = color1;
        this.color2 = color2;
        this.animationType = animationType;
        this.bold = bold;
        this.frames = generateFrames();
        this.start();
    }

    public void start() {
        if (task != null) task.cancel();
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::cycle, 0L, 1L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public String getCurrentFrame() {
        if (frames.isEmpty()) {
            return ColorUtil.translateColors(text);
        }
        return frames.get(currentStage).getText();
    }

    private void cycle() {
        if (frames.isEmpty()) return;

        Frame currentFrame = frames.get(currentStage);
        if (System.currentTimeMillis() - lastStageTime >= currentFrame.getDelay()) {
            lastStageTime = System.currentTimeMillis();
            currentStage = (currentStage + 1) % frames.size();
        }
    }

    public void reset() {
        currentStage = 0;
        lastStageTime = System.currentTimeMillis();
    }

    public int getCurrentStage() {
        return currentStage;
    }

    public int getFrameCount() {
        return frames.size();
    }

    private List<Frame> generateFrames() {
        if (animationType == AnimationType.DEFAULT) {
            return generateDefaultAnimation();
        }
        return new ArrayList<>();
    }

    private List<Frame> generateDefaultAnimation() {
        List<Frame> frames = new ArrayList<>();
        String boldTag = bold ? "<bold>" : "";
        String plainText = ColorUtil.strip(text); // remove old formatting

        if (plainText.isEmpty()) plainText = text;

        // Phase 1: color2 sweeps left to right
        for (int i = 0; i < plainText.length(); i++) {
            StringBuilder frame = new StringBuilder();
            for (int j = 0; j < plainText.length(); j++) {
                if (j <= i) {
                    frame.append(color2).append(boldTag).append(plainText.charAt(j));
                } else {
                    frame.append(color1).append(boldTag).append(plainText.charAt(j));
                }
            }
            frames.add(new Frame(ColorUtil.translateColors(frame.toString()), 240));
        }

        // Phase 2: Solid color1
        frames.add(new Frame(ColorUtil.translateColors(color1 + boldTag + plainText), 240));

        // Phase 3: Flash between colors
        for (int i = 0; i < 3; i++) {
            frames.add(new Frame(ColorUtil.translateColors(color1 + boldTag + plainText), 350));
            frames.add(new Frame(ColorUtil.translateColors(color2 + boldTag + plainText), 350));
        }

        return frames;
    }
}