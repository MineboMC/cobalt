package net.minebo.cobalt.tablist.setup;

import lombok.Generated;
import lombok.Getter;
import net.minebo.cobalt.skin.CachedSkin;
import net.minebo.cobalt.tablist.util.Skin;

@Getter
public class TabEntry {
   private final int x;
   private final int y;
   private String text;
   private int ping = 0;
   private CachedSkin skin;

   public TabEntry(int x, int y, String text) {
      this.skin = Skin.DEFAULT_SKIN;
      this.x = x;
      this.y = y;
      this.text = text;
   }

   public TabEntry(int x, int y, String text, int ping) {
      this.skin = Skin.DEFAULT_SKIN;
      this.x = x;
      this.y = y;
      this.text = text;
      this.ping = ping;
   }

   public TabEntry(int x, int y, String text, CachedSkin skin) {
      this.skin = Skin.DEFAULT_SKIN;
      this.x = x;
      this.y = y;
      this.text = text;
      this.skin = skin;
   }

   public TabEntry(int x, int y, String text, int ping, CachedSkin skin) {
      this.skin = Skin.DEFAULT_SKIN;
      this.x = x;
      this.y = y;
      this.text = text;
      this.ping = ping;
      this.skin = skin;
   }
}
