package net.minebo.cobalt.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ItemUtil {

    static String[] baseIds = {
            "POTION_OF_WATER",
            "POTION_OF_NIGHT_VISION", "POTION_OF_NIGHT_VISION_LONG",
            "POTION_OF_INVISIBILITY", "POTION_OF_INVISIBILITY_LONG",
            "POTION_OF_LEAPING", "POTION_OF_LEAPING_LONG", "POTION_OF_LEAPING_2",
            "POTION_OF_FIRE_RESISTANCE", "POTION_OF_FIRE_RESISTANCE_LONG",
            "POTION_OF_SWIFTNESS", "POTION_OF_SWIFTNESS_LONG", "POTION_OF_SWIFTNESS_2",
            "POTION_OF_SLOWNESS", "POTION_OF_SLOWNESS_LONG", "POTION_OF_SLOWNESS_2",
            "POTION_OF_WATER_BREATHING", "POTION_OF_WATER_BREATHING_LONG",
            "POTION_OF_HEALING", "POTION_OF_HEALING_2",
            "POTION_OF_HARMING", "POTION_OF_HARMING_2",
            "POTION_OF_POISON", "POTION_OF_POISON_LONG", "POTION_OF_POISON_2",
            "POTION_OF_REGENERATION", "POTION_OF_REGENERATION_LONG", "POTION_OF_REGENERATION_2",
            "POTION_OF_STRENGTH", "POTION_OF_STRENGTH_LONG", "POTION_OF_STRENGTH_2",
            "POTION_OF_WEAKNESS", "POTION_OF_WEAKNESS_LONG",
            "POTION_OF_LUCK",
            "POTION_OF_TURTLE_MASTER", "POTION_OF_TURTLE_MASTER_LONG", "POTION_OF_TURTLE_MASTER_2",
            "POTION_OF_SLOW_FALLING", "POTION_OF_SLOW_FALLING_LONG",
            "POTION_OF_WIND_CHARGED", "POTION_OF_WEAVING", "POTION_OF_OOZING", "POTION_OF_INFESTATION"
    };

    public static String getItemId(ItemStack item) {
        if(item.getType() == Material.POTION || item.getType() == Material.SPLASH_POTION || item.getType() == Material.LINGERING_POTION){
            return getPotionId(item);
        }

        return item.getType().name().toLowerCase();
    }

    public static String getPotionId(ItemStack item) {
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta == null) return "unknown";

        PotionType type = meta.getBasePotionType();
        String prefix = item.getType() == Material.SPLASH_POTION ? "SPLASH_" : "";

        switch (type) {
            case WATER: return prefix + "POTION_OF_WATER";
            case NIGHT_VISION: return prefix + "POTION_OF_NIGHT_VISION";
            case LONG_NIGHT_VISION: return prefix + "POTION_OF_NIGHT_VISION_LONG";

            case INVISIBILITY: return prefix + "POTION_OF_INVISIBILITY";
            case LONG_INVISIBILITY: return prefix + "POTION_OF_INVISIBILITY_LONG";

            case LEAPING: return prefix + "POTION_OF_LEAPING";
            case LONG_LEAPING: return prefix + "POTION_OF_LEAPING_LONG";
            case STRONG_LEAPING: return prefix + "POTION_OF_LEAPING_2";

            case FIRE_RESISTANCE: return prefix + "POTION_OF_FIRE_RESISTANCE";
            case LONG_FIRE_RESISTANCE: return prefix + "POTION_OF_FIRE_RESISTANCE_LONG";

            case SWIFTNESS: return prefix + "POTION_OF_SWIFTNESS";
            case LONG_SWIFTNESS: return prefix + "POTION_OF_SWIFTNESS_LONG";
            case STRONG_SWIFTNESS: return prefix + "POTION_OF_SWIFTNESS_2";

            case SLOWNESS: return prefix + "POTION_OF_SLOWNESS";
            case LONG_SLOWNESS: return prefix + "POTION_OF_SLOWNESS_LONG";
            case STRONG_SLOWNESS: return prefix + "POTION_OF_SLOWNESS_2";

            case WATER_BREATHING: return prefix + "POTION_OF_WATER_BREATHING";
            case LONG_WATER_BREATHING: return prefix + "POTION_OF_WATER_BREATHING_LONG";

            case HEALING: return prefix + "POTION_OF_HEALING";
            case STRONG_HEALING: return prefix + "POTION_OF_HEALING_2";

            case HARMING: return prefix + "POTION_OF_HARMING";
            case STRONG_HARMING: return prefix + "POTION_OF_HARMING_2";

            case POISON: return prefix + "POTION_OF_POISON";
            case LONG_POISON: return prefix + "POTION_OF_POISON_LONG";
            case STRONG_POISON: return prefix + "POTION_OF_POISON_2";

            case REGENERATION: return prefix + "POTION_OF_REGENERATION";
            case LONG_REGENERATION: return prefix + "POTION_OF_REGENERATION_LONG";
            case STRONG_REGENERATION: return prefix + "POTION_OF_REGENERATION_2";

            case STRENGTH: return prefix + "POTION_OF_STRENGTH";
            case LONG_STRENGTH: return prefix + "POTION_OF_STRENGTH_LONG";
            case STRONG_STRENGTH: return prefix + "POTION_OF_STRENGTH_2";

            case WEAKNESS: return prefix + "POTION_OF_WEAKNESS";
            case LONG_WEAKNESS: return prefix + "POTION_OF_WEAKNESS_LONG";

            case LUCK: return prefix + "POTION_OF_LUCK";

            case TURTLE_MASTER: return prefix + "POTION_OF_TURTLE_MASTER";
            case LONG_TURTLE_MASTER: return prefix + "POTION_OF_TURTLE_MASTER_LONG";
            case STRONG_TURTLE_MASTER: return prefix + "POTION_OF_TURTLE_MASTER_2";

            case SLOW_FALLING: return prefix + "POTION_OF_SLOW_FALLING";
            case LONG_SLOW_FALLING: return prefix + "POTION_OF_SLOW_FALLING_LONG";

            case WIND_CHARGED: return prefix + "POTION_OF_WIND_CHARGED";
            case WEAVING: return prefix + "POTION_OF_WEAVING";
            case OOZING: return prefix + "POTION_OF_OOZING";
            case INFESTED: return prefix + "POTION_OF_INFESTATION";

            default:
                return prefix + "POTION_OF_" + type.name();
        }
    }


    public static ItemStack getItemFromId(String id) {
        // Check if it's a known potion ID
        PotionType potionType = getPotionTypeFromId(id);
        if (potionType != null) {
            boolean splash = id.endsWith("s");
            Material mat = splash ? Material.SPLASH_POTION : Material.POTION;

            ItemStack potion = new ItemStack(mat);
            PotionMeta meta = (PotionMeta) potion.getItemMeta();

            if (meta != null) {
                meta.setBasePotionType(potionType);
                potion.setItemMeta(meta);
            }

            return potion;
        }

        // Otherwise treat as material
        try {
            Material mat = Material.matchMaterial(id.toUpperCase());
            if (mat != null) {
                return new ItemStack(mat);
            }
        } catch (IllegalArgumentException ignored) {}

        // Unknown item
        return null;
    }

    public static PotionType getPotionTypeFromId(String id) {
        boolean splash = id.startsWith("SPLASH_");
        String baseId = splash ? id.substring("SPLASH_".length()) : id;

        switch (baseId) {
            case "POTION_OF_WATER": return PotionType.WATER;

            case "POTION_OF_NIGHT_VISION": return PotionType.NIGHT_VISION;
            case "POTION_OF_NIGHT_VISION_LONG": return PotionType.LONG_NIGHT_VISION;

            case "POTION_OF_INVISIBILITY": return PotionType.INVISIBILITY;
            case "POTION_OF_INVISIBILITY_LONG": return PotionType.LONG_INVISIBILITY;

            case "POTION_OF_LEAPING": return PotionType.LEAPING;
            case "POTION_OF_LEAPING_LONG": return PotionType.LONG_LEAPING;
            case "POTION_OF_LEAPING_2": return PotionType.STRONG_LEAPING;

            case "POTION_OF_FIRE_RESISTANCE": return PotionType.FIRE_RESISTANCE;
            case "POTION_OF_FIRE_RESISTANCE_LONG": return PotionType.LONG_FIRE_RESISTANCE;

            case "POTION_OF_SWIFTNESS": return PotionType.SWIFTNESS;
            case "POTION_OF_SWIFTNESS_LONG": return PotionType.LONG_SWIFTNESS;
            case "POTION_OF_SWIFTNESS_2": return PotionType.STRONG_SWIFTNESS;

            case "POTION_OF_SLOWNESS": return PotionType.SLOWNESS;
            case "POTION_OF_SLOWNESS_LONG": return PotionType.LONG_SLOWNESS;
            case "POTION_OF_SLOWNESS_2": return PotionType.STRONG_SLOWNESS;

            case "POTION_OF_WATER_BREATHING": return PotionType.WATER_BREATHING;
            case "POTION_OF_WATER_BREATHING_LONG": return PotionType.LONG_WATER_BREATHING;

            case "POTION_OF_HEALING": return PotionType.HEALING;
            case "POTION_OF_HEALING_2": return PotionType.STRONG_HEALING;

            case "POTION_OF_HARMING": return PotionType.HARMING;
            case "POTION_OF_HARMING_2": return PotionType.STRONG_HARMING;

            case "POTION_OF_POISON": return PotionType.POISON;
            case "POTION_OF_POISON_LONG": return PotionType.LONG_POISON;
            case "POTION_OF_POISON_2": return PotionType.STRONG_POISON;

            case "POTION_OF_REGENERATION": return PotionType.REGENERATION;
            case "POTION_OF_REGENERATION_LONG": return PotionType.LONG_REGENERATION;
            case "POTION_OF_REGENERATION_2": return PotionType.STRONG_REGENERATION;

            case "POTION_OF_STRENGTH": return PotionType.STRENGTH;
            case "POTION_OF_STRENGTH_LONG": return PotionType.LONG_STRENGTH;
            case "POTION_OF_STRENGTH_2": return PotionType.STRONG_STRENGTH;

            case "POTION_OF_WEAKNESS": return PotionType.WEAKNESS;
            case "POTION_OF_WEAKNESS_LONG": return PotionType.LONG_WEAKNESS;

            case "POTION_OF_LUCK": return PotionType.LUCK;

            case "POTION_OF_TURTLE_MASTER": return PotionType.TURTLE_MASTER;
            case "POTION_OF_TURTLE_MASTER_LONG": return PotionType.LONG_TURTLE_MASTER;
            case "POTION_OF_TURTLE_MASTER_2": return PotionType.STRONG_TURTLE_MASTER;

            case "POTION_OF_SLOW_FALLING": return PotionType.SLOW_FALLING;
            case "POTION_OF_SLOW_FALLING_LONG": return PotionType.LONG_SLOW_FALLING;

            case "POTION_OF_WIND_CHARGED": return PotionType.WIND_CHARGED;
            case "POTION_OF_WEAVING": return PotionType.WEAVING;
            case "POTION_OF_OOZING": return PotionType.OOZING;
            case "POTION_OF_INFESTATION": return PotionType.INFESTED;

            default: return null;
        }
    }

    public static ItemStack getItemFromPotionId(String id) {
        PotionType type = getPotionTypeFromId(id);
        if (type == null) return null;

        boolean splash = id.endsWith("s");

        Material material = splash ? Material.SPLASH_POTION : Material.POTION;
        ItemStack item = new ItemStack(material);
        PotionMeta meta = (PotionMeta) item.getItemMeta();

        if (meta != null) {
            meta.setBasePotionType(type);
            item.setItemMeta(meta);
        }

        return item;
    }

    public static List<String> getListOfMaterials(){
        List<String> materialNames = Arrays.stream(Material.values())
                .map(Material::name)
                .collect(Collectors.toList());

        for (String id : baseIds) {
            materialNames.add(id);
            materialNames.add("SPLASH_" + id); // Add splash variant
        }

        return materialNames;
    }

}