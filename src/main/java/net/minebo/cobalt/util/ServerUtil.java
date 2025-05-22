package net.minebo.cobalt.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.lang.reflect.Field;

public class ServerUtil {

    public static String getColoredTPS() {
        double tps = getTPS();

        return ((tps > 18.0)
                ? ChatColor.GREEN
                : ((tps > 16.0)
                ? ChatColor.YELLOW
                : ChatColor.RED)
        ) + ((tps > 20.0) ? "*" : "") + Math.min(Math.round(tps * 100.0) / 100.0, 20.0);
    }

    public static double getTPS() {
        try {
            Class<?> serverClass = Class.forName("net.minecraft.server.MinecraftServer");
            Object server = serverClass.getMethod("getServer").invoke(null);
            Field tpsField = serverClass.getField("recentTps");
            return ((double[]) tpsField.get(server))[0];
        } catch (Exception e) {
            e.printStackTrace();
            return -1.0;
        }
    }

}
