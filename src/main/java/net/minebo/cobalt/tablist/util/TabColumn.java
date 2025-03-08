package net.minebo.cobalt.tablist.util;

import lombok.Generated;

public final class TabColumn {
   public static int getColumn(int i) {
      if (i <= 20) {
         return 0;
      } else if (i <= 40) {
         return 1;
      } else if (i <= 60) {
         return 2;
      } else {
         return i <= 80 ? 3 : 0;
      }
   }

}
