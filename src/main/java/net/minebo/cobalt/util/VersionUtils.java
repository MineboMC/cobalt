package net.minebo.cobalt.util;

import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

@UtilityClass
public final class VersionUtils {

    /**
     * Gets the player's protocol version through Paper's API.
     * @param player The player.
     * @return The protocol version as an integer.
     */
    public static int getPlayerProtocolVersion(Player player) {
        return player.getProtocolVersion();
    }

    /**
     * Checks if the player is using a "modern" version (1.13+ by default).
     * @param player The player.
     * @return True if modern, false if legacy.
     */
    public static boolean isModern(Player player) {
        int protocol = getPlayerProtocolVersion(player);
        // Minecraft 1.13 introduced the flattened ID system.
        return protocol >= 393;
    }

    /**
     * Gets a readable name for the player's version based on protocol number.
     * @param player The player.
     * @return A readable version string.
     */
    public static String getPlayerVersionName(Player player) {
        int protocol = getPlayerProtocolVersion(player);

        if (protocol == 47) return "1.8.x";
        if (protocol >= 107 && protocol <= 110) return "1.9.x";
        if (protocol == 210) return "1.10.x";
        if (protocol >= 315 && protocol <= 316) return "1.11.x";
        if (protocol >= 335 && protocol <= 340) return "1.12.x";
        if (protocol >= 393 && protocol <= 404) return "1.13.x";
        if (protocol >= 477 && protocol <= 498) return "1.14.x";
        if (protocol >= 573 && protocol <= 578) return "1.15.x";
        if (protocol >= 735 && protocol <= 754) return "1.16.x";
        if (protocol >= 755 && protocol <= 761) return "1.17+";

        return "Unknown (" + protocol + ")";
    }
}
