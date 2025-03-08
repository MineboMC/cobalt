package net.minebo.cobalt.tablist.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public final class StringUtils {
   public static final int MINOR_VERSION;
   private static final Pattern hexPattern = Pattern.compile("&#[A-Fa-f0-9]{6}");

   public static String[] split(String text) {
      if (text.length() <= 16) {
         return new String[]{text, ""};
      } else {
         String prefix = text.substring(0, 16);
         String suffix;
         if (prefix.charAt(15) != 167 && prefix.charAt(15) != '&') {
            if (prefix.charAt(14) != 167 && prefix.charAt(14) != '&') {
               String lastColor = ChatColor.getLastColors(prefix);
               suffix = lastColor + text.substring(16);
            } else {
               prefix = prefix.substring(0, 14);
               suffix = text.substring(14);
            }
         } else {
            prefix = prefix.substring(0, 15);
            suffix = text.substring(15);
         }

         if (suffix.length() > 16) {
            suffix = suffix.substring(0, 16);
         }

         return new String[]{prefix, suffix};
      }
   }

   public static net.md_5.bungee.api.ChatColor getLastColors(String input) {
      String prefixColor = ChatColor.getLastColors(color(input));
      if (prefixColor.isEmpty()) {
         return null;
      } else {
         net.md_5.bungee.api.ChatColor color;
         if (MINOR_VERSION >= 16) {
            try {
               String hexColor = prefixColor.replace("§", "").replace("x", "#");
               color = net.md_5.bungee.api.ChatColor.of(hexColor);
            } catch (Exception var5) {
               ChatColor bukkitColor = ChatColor.getByChar(prefixColor.substring(prefixColor.length() - 1).charAt(0));
               if (bukkitColor == null) {
                  return null;
               }

               color = bukkitColor.asBungee();
            }
         } else {
            ChatColor bukkitColor = ChatColor.getByChar(prefixColor.substring(prefixColor.length() - 1).charAt(0));
            if (bukkitColor == null) {
               return null;
            }

            color = bukkitColor.asBungee();
         }

         return color;
      }
   }

   public static String color(String text) {
      if (text == null) {
         return "";
      } else {
         text = ChatColor.translateAlternateColorCodes('&', text);
         if (MINOR_VERSION >= 16) {
            Matcher matcher = hexPattern.matcher(text);

            while(matcher.find()) {
               try {
                  String color = matcher.group();
                  String hexColor = color.replace("&", "").replace("x", "#");
                  net.md_5.bungee.api.ChatColor bungeeColor = net.md_5.bungee.api.ChatColor.of(hexColor);
                  text = text.replace(color, bungeeColor.toString());
               } catch (Exception var5) {
               }
            }
         }

         return text;
      }
   }

   static {
      Matcher matcher = Pattern.compile("MC: \\d\\.(\\d+)").matcher(Bukkit.getVersion());
      if (matcher.find()) {
         MINOR_VERSION = Integer.parseInt(matcher.group(1));
      } else {
         MINOR_VERSION = 8;
      }

   }
}
