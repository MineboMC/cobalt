package net.minebo.cobalt.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ColorUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    public static String translateColors(String message) {
        if (message == null) return null;
        Component component = MINI_MESSAGE.deserialize(message);
        return LEGACY_SERIALIZER.serialize(component);
    }

    public static Component toComponent(String message) {
        if (message == null) return Component.empty();
        return MINI_MESSAGE.deserialize(message);
    }

    public static String strip(String message) {
        if (message == null) return null;
        return LEGACY_SERIALIZER.serialize(MINI_MESSAGE.deserialize(message))
                .replaceAll("§[0-9a-fk-orx]", "");
    }
}