package net.minebo.cobalt.tablist.adapter.impl;

import java.util.ArrayList;
import java.util.List;
import net.minebo.cobalt.tablist.adapter.TabAdapter;
import net.minebo.cobalt.tablist.setup.TabEntry;
import net.minebo.cobalt.tablist.util.Skin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ExampleAdapter implements TabAdapter {
   public String getHeader(Player player) {
      return "&3&lwww.ForestBukkit.com";
   }

   public String getFooter(Player player) {
      return "&9Home of &dMCRaidz&9!";
   }

   public List getLines(Player player) {
      List<TabEntry> entries = new ArrayList();
      Integer y = 0;
      Integer x = 0;

      for(Player selection : Bukkit.getOnlinePlayers()) {
         if (x <= 3) {
            entries.add(new TabEntry(x, y, selection.getDisplayName(), selection.getPing(), Skin.getPlayer(selection)));
            x = x + 1;
         } else {
            y = y + 1;
            x = 0;
         }

         if (y == 20) {
            return entries;
         }
      }

      return entries;
   }
}
