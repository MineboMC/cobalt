package net.minebo.cobalt.scoreboard.animation;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.minebo.cobalt.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TextAnimation {

    private final String text;
    private final ChatColor color1;
    private final ChatColor color2;
    private final AnimationType animationType;
    private final boolean bold;
    private final JavaPlugin plugin;

    private List<Frame> frames;
    private int currentStage = 0;
    private long lastStageTime = System.currentTimeMillis();
    private BukkitTask task;

    public TextAnimation(JavaPlugin plugin, String text, ChatColor color1, ChatColor color2) {
        this(plugin, text, color1, color2, AnimationType.DEFAULT, true);
    }

    public TextAnimation(JavaPlugin plugin, String text, ChatColor color1, ChatColor color2, AnimationType animationType) {
        this(plugin, text, color1, color2, animationType, true);
    }

    public TextAnimation(JavaPlugin plugin, String text, ChatColor color1, ChatColor color2, AnimationType animationType, boolean bold) {
        this.plugin = plugin;
        this.text = text;
        this.color1 = color1;
        this.color2 = color2;
        this.animationType = animationType;
        this.bold = bold;
        this.frames = generateFrames();
        this.start();
    }

    /**
     * Starts the automatic cycling of frames
     */
    public void start() {
        if (task != null) {
            task.cancel();
        }

        task = Bukkit.getScheduler().runTaskTimer(plugin, this::cycle, 0L, 1L);
    }

    /**
     * Stops the automatic cycling
     */
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * Gets the current frame without cycling
     * @return the current frame's text
     */
    public String getCurrentFrame() {
        if (frames.isEmpty()) {
            return text;
        }
        return ColorUtil.translateColors(frames.get(currentStage).getText());
    }

    /**
     * Cycles to the next frame if enough time has passed
     * This is called automatically by the scheduler
     */
    private void cycle() {
        if (frames.isEmpty()) {
            return;
        }

        Frame currentFrame = frames.get(currentStage);

        if (System.currentTimeMillis() - lastStageTime >= currentFrame.getDelay()) {
            lastStageTime = System.currentTimeMillis();

            if (currentStage + 1 >= frames.size()) {
                currentStage = 0;
            } else {
                currentStage++;
            }
        }
    }

    /**
     * Reset the animation to the first frame
     */
    public void reset() {
        currentStage = 0;
        lastStageTime = System.currentTimeMillis();
    }

    /**
     * Get the current stage index
     */
    public int getCurrentStage() {
        return currentStage;
    }

    /**
     * Get total number of frames
     */
    public int getFrameCount() {
        return frames.size();
    }

    public List<Frame> generateFrames() {
        switch (animationType) {
            case DEFAULT:
                return generateDefaultAnimation();
            default:
                return new ArrayList<>();
        }
    }

    private List<Frame> generateDefaultAnimation() {
        List<Frame> frames = new ArrayList<>();
        String boldCode = bold ? ChatColor.BOLD.toString() : "";
        String plainText = ChatColor.stripColor(text);

        if (plainText == null || plainText.isEmpty()) {
            plainText = text;
        }

        // Phase 1: color2 sweeps through (left to right) - progressively filling
        for (int i = 0; i < plainText.length(); i++) {
            StringBuilder frame = new StringBuilder();
            for (int j = 0; j < plainText.length(); j++) {
                if (j <= i) {
                    // All characters to the left (including current) are color2
                    frame.append(color2).append(boldCode).append(plainText.charAt(j));
                } else {
                    // Characters to the right remain color1
                    frame.append(color1).append(boldCode).append(plainText.charAt(j));
                }
            }
            frames.add(new Frame(frame.toString(), 240));
        }

        // Phase 2: All color1
        frames.add(new Frame(color1.toString() + boldCode + plainText, 240));

        // Phase 3: Flash between colors
        for (int i = 0; i < 3; i++) {
            frames.add(new Frame(color1 + boldCode + plainText, 350));
            frames.add(new Frame(color2 + boldCode + plainText, 350));
            frames.add(new Frame(color1 + boldCode + plainText, 350));
            frames.add(new Frame(color2 + boldCode + plainText, 350));
        }
        frames.add(new Frame(color1 + boldCode + plainText, 350));
        frames.add(new Frame(color2 + boldCode + plainText, 350));
        frames.add(new Frame(color1 + boldCode + plainText, 350));

        return frames;
    }
    
}