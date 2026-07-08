package net.minebo.cobalt.util;

import java.lang.reflect.Field;

public class ServerUtil {

    public static String getColoredTPS() {
        double tps = getTPS();

        return ((tps > 18.0)
                ? ColorUtil.translateColors("<green>")
                : ((tps > 16.0)
                ? ColorUtil.translateColors("<yellow>")
                : ColorUtil.translateColors("<red>"))
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
