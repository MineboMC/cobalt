package net.minebo.cobalt.scoreboard.construct;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.util.Objects;
import net.minebo.cobalt.util.Reflection;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Board extends BoardBase<String> {

   private static final MethodHandle MESSAGE_FROM_STRING;
   private static final Object EMPTY_MESSAGE;

   static {
      try {
         MethodHandles.Lookup lookup = MethodHandles.lookup();
         Class<?> craftChatMessageClass = Reflection.obcClass("util.CraftChatMessage");
         MESSAGE_FROM_STRING = lookup.unreflect(craftChatMessageClass.getMethod("fromString", String.class));
         EMPTY_MESSAGE = Array.get(MESSAGE_FROM_STRING.invoke(""), 0);
      } catch (Throwable t) {
         throw new ExceptionInInitializerError(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public Board(Player player) {
      super(player);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void updateTitle(String title) {
      Objects.requireNonNull(title, "title");

      if (!VersionType.V1_13.isHigherOrEqual() && title.length() > 32) {
         throw new IllegalArgumentException("Title is longer than 32 chars");
      }

      super.updateTitle(title);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void updateLines(String... lines) {
      Objects.requireNonNull(lines, "lines");

      if (!VersionType.V1_13.isHigherOrEqual()) {
         int lineCount = 0;
         for (String s : lines) {
            if (s != null && s.length() > 30) {
               throw new IllegalArgumentException("Line " + lineCount + " is longer than 30 chars");
            }
            lineCount++;
         }
      }

      super.updateLines(lines);
   }

   @Override
   protected void sendLineChange(int score) throws Throwable {
      int maxLength = hasLinesMaxLength() ? 16 : 1024;
      String line = getLineByScore(score);
      String prefix;
      String suffix = "";

      if (line == null || line.isEmpty()) {
         prefix = COLOR_CODES[score] + ChatColor.RESET;
      } else if (line.length() <= maxLength) {
         prefix = line;
      } else {
         // Prevent splitting color codes
         int index = line.charAt(maxLength - 1) == ChatColor.COLOR_CHAR
                 ? (maxLength - 1) : maxLength;
         prefix = line.substring(0, index);
         String suffixTmp = line.substring(index);
         ChatColor chatColor = null;

         if (suffixTmp.length() >= 2 && suffixTmp.charAt(0) == ChatColor.COLOR_CHAR) {
            chatColor = ChatColor.getByChar(suffixTmp.charAt(1));
         }

         String color = ChatColor.getLastColors(prefix);
         boolean addColor = chatColor == null || chatColor.isFormat();

         suffix = (addColor ? (color.isEmpty() ? ChatColor.RESET.toString() : color) : "") + suffixTmp;
      }

      if (prefix.length() > maxLength || suffix.length() > maxLength) {
         // Something went wrong, just cut to prevent client crash/kick
         prefix = prefix.substring(0, Math.min(maxLength, prefix.length()));
         suffix = suffix.substring(0, Math.min(maxLength, suffix.length()));
      }

      sendTeamPacket(score, TeamMode.UPDATE, prefix, suffix);
   }

   @Override
   protected Object toMinecraftComponent(String line) throws Throwable {
      if (line == null || line.isEmpty()) {
         return EMPTY_MESSAGE;
      }

      return Array.get(MESSAGE_FROM_STRING.invoke(line), 0);
   }

   @Override
   protected String serializeLine(String value) {
      return value;
   }

   @Override
   protected String emptyLine() {
      return "";
   }

   /**
    * Return if the player has a prefix/suffix characters limit.
    * By default, it returns true only in 1.12 or lower.
    * This method can be overridden to fix compatibility with some versions support plugin.
    *
    * @return max length
    */
   protected boolean hasLinesMaxLength() {
      return !VersionType.V1_13.isHigherOrEqual();
   }
}