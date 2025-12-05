package net.minebo.cobalt.util;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class TimeUtil {

    public static String longToTime(long time) {
        long millisLeft = time - System.currentTimeMillis();
        if (millisLeft <= 0) return "0";

        if (millisLeft >= TimeUnit.MINUTES.toMillis(1)) {
            return DurationFormatUtils.formatDuration(millisLeft, "mm:ss");
        }

        // Show seconds with one decimal (e.g., 15.3s)
        double seconds = millisLeft / 1000.0;
        return String.format("%.1fs", seconds);
    }

}
