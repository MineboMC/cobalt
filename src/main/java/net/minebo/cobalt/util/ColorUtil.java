package net.minebo.cobalt.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    public static String translateHexColors(String message) {
        if (message == null) return null;

        Pattern hexPattern = Pattern.compile("#([A-Fa-f0-9]{6})");
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append('§').append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    public static String translateColors(String message) {
        if (message == null) return null;

        // Translate & codes + hex first
        String legacy = ChatColor.translateAlternateColorCodes('&', translateHexColors(message));

        // Then parse MiniMessage
        Component component = MINI_MESSAGE.deserialize(legacy);

        // Convert back to legacy string for maximum compatibility
        return LEGACY_SERIALIZER.serialize(component);
    }

    public static Component toComponent(String message) {
        if (message == null) return Component.empty();

        // Let MiniMessage handle & codes, hex, and MM tags
        return MINI_MESSAGE.deserialize(message);
    }

    public static String strip(String message) {
        if (message == null) return null;
        return LEGACY_SERIALIZER.serialize(MINI_MESSAGE.deserialize(message))
                .replaceAll("§[0-9a-fk-orx]", "");
    }
}