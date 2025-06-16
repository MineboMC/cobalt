package net.minebo.cobalt.util.format;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class TimeFormatting {

    public static String getRemaining(Long time) {
        if (time > TimeUnit.SECONDS.toMillis(60L)) {
            return DurationFormatUtils.formatDuration(time, "m:ss");
        } else {
            String formatted = DurationFormatUtils.formatDuration(time, "s.S");
            return formatted.substring(0, formatted.length() - 2);
        }
    }

}
