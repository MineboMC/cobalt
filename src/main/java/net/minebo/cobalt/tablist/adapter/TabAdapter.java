package net.minebo.cobalt.tablist.adapter;

import java.util.List;
import org.bukkit.entity.Player;

public interface TabAdapter {
   String getHeader(Player var1);

   String getFooter(Player var1);

   List getLines(Player var1);
}
