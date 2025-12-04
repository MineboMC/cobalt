package net.minebo.cobalt.util;

public class DyeUtil {

    public static org.bukkit.Color fromHex(String hex) {
        hex = hex.replace("#", "");

        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);

        return org.bukkit.Color.fromRGB(r, g, b);
    }

}
