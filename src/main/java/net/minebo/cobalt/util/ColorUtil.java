package net.minebo.cobalt.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ColorUtil {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    public static String translateColors(String message) {
        if (message == null) return "";
        message = legacyToMiniMessage(message);
        try {
            return LEGACY.serialize(MINI_MESSAGE.deserialize(message));
        } catch (Exception e) {
            return message;
        }
    }

    public static Component toComponent(String message) {
        if (message == null) return Component.empty();
        return MINI_MESSAGE.deserialize(message);
    }

    public static String strip(String message) {
        if (message == null) return "";
        return LEGACY.serialize(MINI_MESSAGE.deserialize(message)).replaceAll("§[0-9a-fk-orx]", "");
    }

    public static String legacyToMiniMessage(String text) {
        if (text == null) return "";

        text = text.replace('§', '&');
        text = text.replaceAll("(?<!<)&?#([A-Fa-f0-9]{6})", "<#$1>");

        // Standard color codes
        text = text.replace("&0", "<black>");
        text = text.replace("&1", "<dark_blue>");
        text = text.replace("&2", "<dark_green>");
        text = text.replace("&3", "<dark_aqua>");
        text = text.replace("&4", "<dark_red>");
        text = text.replace("&5", "<dark_purple>");
        text = text.replace("&6", "<gold>");
        text = text.replace("&7", "<gray>");
        text = text.replace("&8", "<dark_gray>");
        text = text.replace("&9", "<blue>");
        text = text.replace("&a", "<green>");
        text = text.replace("&b", "<aqua>");
        text = text.replace("&c", "<red>");
        text = text.replace("&d", "<light_purple>");
        text = text.replace("&e", "<yellow>");
        text = text.replace("&f", "<white>");

        // Formatting codes
        text = text.replace("&k", "<obfuscated>");
        text = text.replace("&l", "<bold>");
        text = text.replace("&m", "<strikethrough>");
        text = text.replace("&n", "<underlined>");
        text = text.replace("&o", "<italic>");
        text = text.replace("&r", "<reset>");

        return text;
    }
}