package net.minebo.cobalt.storage.json;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public class JsonStorage {

   private static final Logger LOGGER = LogManager.getLogger(JsonStorage.class);
   private final String name;
   private final File file;
   private final Gson gson;

   public JsonStorage(String name, JavaPlugin plugin, Gson gson) {
      this(name, new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "data"), gson);
   }

   public JsonStorage(String name, File directory, Gson gson) {
      if (!directory.exists()) {
         boolean created = directory.mkdir();
         if (!created) {
            LOGGER.info("[Storage] Couldn't create " + name + "'s storage");
         }
      }

      this.file = new File(directory, name + ".json");
      this.gson = gson;
      this.name = name;
      if (!this.file.exists()) {
         try {
            boolean created = this.file.createNewFile();
            if (!created) {
               LOGGER.info("[Storage] Couldn't create " + name + "'s storage");
            }
         } catch (IOException exception) {
            exception.printStackTrace();
         }
      }

   }

   public Object getData(Type type) {
      try {
         FileReader reader = new FileReader(this.file);

         Object var3;
         try {
            var3 = this.gson.fromJson((Reader)reader, (Type)type);
         } catch (Throwable var6) {
            try {
               reader.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }

            throw var6;
         }

         reader.close();
         return var3;
      } catch (IOException exception) {
         LOGGER.info("[Storage] Unable to load JSON Storage for " + this.name + ", check for syntax errors!");
         exception.printStackTrace();
         return null;
      }
   }

   public void saveAsync(Object list) {
      CompletableFuture.runAsync(() -> this.save(list));
   }

   public void save(Object list) {
      try {
         FileWriter fileWriter = new FileWriter(this.file);

         try {
            this.gson.toJson((Object)list, (Appendable)fileWriter);
         } catch (Throwable var6) {
            try {
               fileWriter.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }

            throw var6;
         }

         fileWriter.close();
      } catch (IOException exception) {
         exception.printStackTrace();
      }

   }
}
