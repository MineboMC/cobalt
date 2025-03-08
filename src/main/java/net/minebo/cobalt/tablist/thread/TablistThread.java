package net.minebo.cobalt.tablist.thread;

import lombok.AllArgsConstructor;
import lombok.Generated;
import net.minebo.cobalt.tablist.TablistHandler;
import net.minebo.cobalt.tablist.setup.TabLayout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@AllArgsConstructor
public class TablistThread extends Thread {

   private static final Logger log = LogManager.getLogger(TablistThread.class);
   private final TablistHandler handler;

   public void run() {
      while(true) {
         this.tick();

         try {
            Thread.sleep(20L);
         } catch (InterruptedException e) {
            throw new RuntimeException(e);
         }
      }
   }

   private void tick() {
      if (this.handler.getPlugin().isEnabled()) {
         for(TabLayout layout : this.handler.getLayoutMapping().values()) {
            layout.refresh();
         }

      }
   }

}
