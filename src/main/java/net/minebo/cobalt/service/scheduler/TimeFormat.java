package net.minebo.cobalt.service.scheduler;

public final class TimeFormat {

    private TimeFormat() {
    }

    /** 01:23:45 */
    public static String hours(long millis) {
        long total = Math.max(0L, millis) / 1000L;
        long hours = total / 3600L;
        long minutes = (total % 3600L) / 60L;
        long seconds = total % 60L;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /** 05:09 */
    public static String minutes(long millis) {
        long total = Math.max(0L, millis) / 1000L;
        long minutes = total / 60L;
        long seconds = total % 60L;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /** 12.4 */
    public static String tenths(long millis) {
        long safe = Math.max(0L, millis);
        long seconds = safe / 1000L;
        long tenth = (safe % 1000L) / 100L;
        return seconds + "." + tenth + "s";
    }

    /** hh:mm:ss if >= 1h, mm:ss if >= 1m, else ss.x */
    public static String auto(long millis) {
        long safe = Math.max(0L, millis);
        if (safe >= 3_600_000L) {
            return hours(safe);
        }
        if (safe >= 60_000L) {
            return minutes(safe);
        }
        return tenths(safe);
    }
}