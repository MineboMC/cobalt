package net.minebo.cobalt.tablist.util;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum TabLatency {
   FIVE_BARS(149),
   FOUR_BARS(299),
   THREE_BARS(599),
   TWO_BARS(999),
   ONE_BAR(1001),
   NO_BAR(-1);

   private final int value;

}
