package net.minebo.cobalt.util;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

@Getter
public enum Color {
    BLACK("0", "#000000"),
    DARK_BLUE("1", "#0000AA"),
    DARK_GREEN("2", "#00AA00"),
    DARK_AQUA("3", "#00AAAA"),
    DARK_RED("4", "#AA0000"),
    DARK_PURPLE("5", "#AA00AA"),
    GOLD("6", "#FFAA00"),
    GRAY("7", "#AAAAAA"),
    DARK_GRAY("8", "#555555"),
    BLUE("9", "#5555FF"),
    GREEN("a", "#55FF55"),
    AQUA("b", "#55FFFF"),
    RED("c", "#FF5555"),
    LIGHT_PURPLE("d", "#FF55FF"),
    YELLOW("e", "#FFFF55"),
    WHITE("f", "#FFFFFF"),
    RAINBOW("r", "#RAINBOW"), // Special case
    BOLD("l", "", true),
    ITALIC("o", "", true),
    RESET("r", "", true),
    UNDERLINE("n", "", true),
    MAGIC("k", "", true),
    STRIKETHROUGH("m", "", true);

    public static final char COLOR_CHAR = '&';

    private final String code;
    private final String hex;
    private final boolean format;

    Color(String code, String hex) {
        this(code, hex, false);
    }

    Color(String code, String hex, boolean format) {
        this.code = code;
        this.hex = hex;
        this.format = format;
    }

    @Override
    public String toString() {
        return "§" + code;
    }

    private static final boolean IS_BUKKIT_AVAILABLE;
    private static final Map<Color, ItemStack> GLASS_MATERIAL_MAP;
    private static final Map<Color, ItemStack> GLASS_PANE_MATERIAL_MAP;
    private static final Map<Color, ItemStack> WOOL_MATERIAL_MAP;
    private static final Map<Color, ItemStack> DYE_MATERIAL_MAP;

    static {
        boolean available;
        try {
            Class.forName("org.bukkit.Bukkit");
            available = true;
        } catch (ClassNotFoundException e) {
            available = false;
        }
        IS_BUKKIT_AVAILABLE = available;

        if (IS_BUKKIT_AVAILABLE) {
            GLASS_MATERIAL_MAP = createGlassMap();
            GLASS_PANE_MATERIAL_MAP = createGlassPaneMap();
            WOOL_MATERIAL_MAP = createWoolMap();
            DYE_MATERIAL_MAP = createDyeMap();
        } else {
            GLASS_MATERIAL_MAP = Collections.emptyMap();
            GLASS_PANE_MATERIAL_MAP = Collections.emptyMap();
            WOOL_MATERIAL_MAP = Collections.emptyMap();
            DYE_MATERIAL_MAP = Collections.emptyMap();
        }
    }

    private static Map<Color, ItemStack> createGlassMap() {
        Map<Color, ItemStack> map = new EnumMap<>(Color.class);
        map.put(WHITE, new ItemStack(Material.WHITE_STAINED_GLASS));
        map.put(GRAY, new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS));
        map.put(DARK_GRAY, new ItemStack(Material.GRAY_STAINED_GLASS));
        map.put(BLACK, new ItemStack(Material.BLACK_STAINED_GLASS));
        map.put(RED, new ItemStack(Material.RED_STAINED_GLASS));
        map.put(DARK_RED, new ItemStack(Material.RED_STAINED_GLASS));
        map.put(GOLD, new ItemStack(Material.ORANGE_STAINED_GLASS));
        map.put(YELLOW, new ItemStack(Material.YELLOW_STAINED_GLASS));
        map.put(GREEN, new ItemStack(Material.LIME_STAINED_GLASS));
        map.put(DARK_GREEN, new ItemStack(Material.GREEN_STAINED_GLASS));
        map.put(AQUA, new ItemStack(Material.CYAN_STAINED_GLASS));
        map.put(DARK_AQUA, new ItemStack(Material.CYAN_STAINED_GLASS));
        map.put(BLUE, new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS));
        map.put(DARK_BLUE, new ItemStack(Material.BLUE_STAINED_GLASS));
        map.put(LIGHT_PURPLE, new ItemStack(Material.MAGENTA_STAINED_GLASS));
        map.put(DARK_PURPLE, new ItemStack(Material.PURPLE_STAINED_GLASS));
        return map;
    }

    private static Map<Color, ItemStack> createGlassPaneMap() {
        Map<Color, ItemStack> map = new EnumMap<>(Color.class);
        map.put(WHITE, new ItemStack(Material.WHITE_STAINED_GLASS_PANE));
        map.put(GRAY, new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE));
        map.put(DARK_GRAY, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        map.put(BLACK, new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
        map.put(RED, new ItemStack(Material.RED_STAINED_GLASS_PANE));
        map.put(DARK_RED, new ItemStack(Material.RED_STAINED_GLASS_PANE));
        map.put(GOLD, new ItemStack(Material.ORANGE_STAINED_GLASS_PANE));
        map.put(YELLOW, new ItemStack(Material.YELLOW_STAINED_GLASS_PANE));
        map.put(GREEN, new ItemStack(Material.LIME_STAINED_GLASS_PANE));
        map.put(DARK_GREEN, new ItemStack(Material.GREEN_STAINED_GLASS_PANE));
        map.put(AQUA, new ItemStack(Material.CYAN_STAINED_GLASS_PANE));
        map.put(DARK_AQUA, new ItemStack(Material.CYAN_STAINED_GLASS_PANE));
        map.put(BLUE, new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
        map.put(DARK_BLUE, new ItemStack(Material.BLUE_STAINED_GLASS_PANE));
        map.put(LIGHT_PURPLE, new ItemStack(Material.MAGENTA_STAINED_GLASS_PANE));
        map.put(DARK_PURPLE, new ItemStack(Material.PURPLE_STAINED_GLASS_PANE));
        return map;
    }

    private static Map<Color, ItemStack> createWoolMap() {
        Map<Color, ItemStack> map = new EnumMap<>(Color.class);
        map.put(WHITE, new ItemStack(Material.WHITE_WOOL));
        map.put(GRAY, new ItemStack(Material.LIGHT_GRAY_WOOL));
        map.put(DARK_GRAY, new ItemStack(Material.GRAY_WOOL));
        map.put(BLACK, new ItemStack(Material.BLACK_WOOL));
        map.put(RED, new ItemStack(Material.RED_WOOL));
        map.put(DARK_RED, new ItemStack(Material.RED_WOOL));
        map.put(GOLD, new ItemStack(Material.ORANGE_WOOL));
        map.put(YELLOW, new ItemStack(Material.YELLOW_WOOL));
        map.put(GREEN, new ItemStack(Material.LIME_WOOL));
        map.put(DARK_GREEN, new ItemStack(Material.GREEN_WOOL));
        map.put(AQUA, new ItemStack(Material.CYAN_WOOL));
        map.put(DARK_AQUA, new ItemStack(Material.CYAN_WOOL));
        map.put(BLUE, new ItemStack(Material.LIGHT_BLUE_WOOL));
        map.put(DARK_BLUE, new ItemStack(Material.BLUE_WOOL));
        map.put(LIGHT_PURPLE, new ItemStack(Material.MAGENTA_WOOL));
        map.put(DARK_PURPLE, new ItemStack(Material.PURPLE_WOOL));
        return map;
    }

    private static Map<Color, ItemStack> createDyeMap() {
        Map<Color, ItemStack> map = new EnumMap<>(Color.class);
        map.put(WHITE, new ItemStack(Material.WHITE_DYE));
        map.put(GRAY, new ItemStack(Material.LIGHT_GRAY_DYE));
        map.put(DARK_GRAY, new ItemStack(Material.GRAY_DYE));
        map.put(BLACK, new ItemStack(Material.BLACK_DYE));
        map.put(RED, new ItemStack(Material.RED_DYE));
        map.put(DARK_RED, new ItemStack(Material.RED_DYE));
        map.put(GOLD, new ItemStack(Material.ORANGE_DYE));
        map.put(YELLOW, new ItemStack(Material.YELLOW_DYE));
        map.put(GREEN, new ItemStack(Material.LIME_DYE));
        map.put(DARK_GREEN, new ItemStack(Material.GREEN_DYE));
        map.put(AQUA, new ItemStack(Material.CYAN_DYE));
        map.put(DARK_AQUA, new ItemStack(Material.CYAN_DYE));
        map.put(BLUE, new ItemStack(Material.LIGHT_BLUE_DYE));
        map.put(DARK_BLUE, new ItemStack(Material.BLUE_DYE));
        map.put(LIGHT_PURPLE, new ItemStack(Material.MAGENTA_DYE));
        map.put(DARK_PURPLE, new ItemStack(Material.PURPLE_DYE));
        return map;
    }

    // ====== Public Accessors ======

    public static Color from(String code) {
        for (Color color : values()) {
            if (color.code.equalsIgnoreCase(code)) return color;
        }
        return null;
    }

    public static Color from(char code) {
        return from(String.valueOf(code));
    }

    public static ItemStack getGlassMaterial(Color color) {
        return GLASS_MATERIAL_MAP.get(color);
    }

    public static ItemStack getGlassPaneMaterial(Color color) {
        return GLASS_PANE_MATERIAL_MAP.get(color);
    }

    public static ItemStack getWoolMaterial(Color color) {
        return WOOL_MATERIAL_MAP.get(color);
    }

    public static ItemStack getDyeMaterial(Color color) {
        return DYE_MATERIAL_MAP.get(color);
    }

    public static Color last(String s) {
        Color lastColor = null;
        for (int i = 0; i < s.length() - 1; i++) {
            char c = s.charAt(i);
            if (c == '§' || c == COLOR_CHAR) {
                char colorCode = s.charAt(i + 1);
                Color color = from(colorCode);
                if (color != null && !color.isFormat()) {
                    lastColor = color;
                }
                i++;
            }
        }
        return lastColor;
    }
}
