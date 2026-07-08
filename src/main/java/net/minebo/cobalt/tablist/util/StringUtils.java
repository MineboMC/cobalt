package net.minebo.cobalt.tablist.util;

import net.minebo.cobalt.util.ColorUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtils {

   public static final int MINOR_VERSION;

   // Supports MiniMessage hex format <#RRGGBB>
   private static final Pattern hexPattern = Pattern.compile("<#[A-Fa-f0-9]{6}>");

   public static String[] split(String text) {
      if (text == null) return new String[]{"", ""};
      if (text.length() <= 16) {
         return new String[]{text, ""};
      }

      String prefix = text.substring(0, 16);
      String suffix;

      if (prefix.charAt(15) == '§' || prefix.charAt(15) == '&') {
         prefix = prefix.substring(0, 15);
         suffix = text.substring(15);
      } else if (prefix.charAt(14) == '§' || prefix.charAt(14) == '&') {
         prefix = prefix.substring(0, 14);
         suffix = text.substring(14);
      } else {
         String lastColor = org.bukkit.ChatColor.getLastColors(prefix);
         suffix = lastColor + text.substring(16);
      }

      if (suffix.length() > 16) {
         suffix = suffix.substring(0, 16);
      }

      return new String[]{prefix, suffix};
   }

   public static ChatColor getLastColors(String input) {
      if (input == null) return null;

      String colored = ColorUtil.translateColors(input);
      String lastColors = org.bukkit.ChatColor.getLastColors(colored);

      if (lastColors.isEmpty()) return null;

      try {
         if (MINOR_VERSION >= 16 && lastColors.contains("§x")) {
            // Convert §x§r§r§g§g§b§b to #RRGGBB
            String hex = lastColors.replace("§", "").replace("x", "#");
            return ChatColor.of(hex);
         }

         // Legacy fallback
         char code = lastColors.charAt(lastColors.length() - 1);
         org.bukkit.ChatColor bukkitColor = org.bukkit.ChatColor.getByChar(code);
         return bukkitColor != null ? bukkitColor.asBungee() : null;
      } catch (Exception e) {
         return null;
      }
   }

   public static String color(String text) {
      if (text == null) return "";

      // Main processing through ColorUtil (MiniMessage)
      text = ColorUtil.translateColors(text);

      // Extra pass for hex colors in tablist (1.16+)
      if (MINOR_VERSION >= 16) {
         Matcher matcher = hexPattern.matcher(text);
         while (matcher.find()) {
            try {
               String match = matcher.group();
               String hex = match.replace("<#", "#").replace(">", "");
               ChatColor bungeeColor = ChatColor.of(hex);
               text = text.replace(match, bungeeColor.toString());
            } catch (Exception ignored) {}
         }
      }

      return text;
   }

   static {
      Matcher matcher = Pattern.compile("MC: \\d\\.(\\d+)").matcher(Bukkit.getVersion());
      MINOR_VERSION = matcher.find() ? Integer.parseInt(matcher.group(1)) : 8;
   }
}