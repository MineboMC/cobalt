package net.minebo.cobalt.scoreboard.animation;

public class Frame {
    private final String text;
    private final long delay;

    public Frame(String text, long delay) {
        this.text = text;
        this.delay = delay;
    }

    public String getText() {
        return text;
    }

    public long getDelay() {
            return delay;
        }
}