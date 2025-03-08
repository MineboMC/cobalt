package net.minebo.cobalt.util;

import java.util.concurrent.ThreadFactory;
import org.jetbrains.annotations.NotNull;

public class NameThreadFactory implements ThreadFactory {
   private final String name;

   public NameThreadFactory(String name) {
      this.name = name;
   }

   public Thread newThread(@NotNull Runnable r) {
      return new Thread(r, this.name);
   }
}
