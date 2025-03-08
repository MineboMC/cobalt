package net.minebo.cobalt.storage.yaml;

import com.google.common.base.Charsets;
import java.io.File;
import java.io.IOException;
import java.util.List;
import net.minebo.cobalt.storage.annotations.Header;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.comments.format.YamlCommentFormat;
import org.simpleyaml.configuration.file.YamlConfiguration;
import org.simpleyaml.configuration.file.YamlConfigurationOptions;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.configuration.implementation.api.QuoteStyle;
import org.simpleyaml.configuration.implementation.snakeyaml.SnakeYamlImplementation;

public abstract class YamlStorage {
   private static final Logger LOGGER = LogManager.getLogger(YamlStorage.class);
   protected final String name;
   protected final YamlFile config;

   public YamlStorage(JavaPlugin plugin, String name, boolean saveResource) {
      File file = new File(plugin.getDataFolder(), name + ".yml");
      this.name = name;
      this.config = new YamlFile(file);
      if (!file.exists()) {
         try {
            if (saveResource) {
               plugin.saveResource(name + ".yml", false);
            } else {
               this.config.createNewFile(false);
            }
         } catch (IOException ex) {
            LOGGER.error("[Storage] Could not create/save " + name + ".yml!");
            LOGGER.error("[Storage] Error: " + ex.getMessage());
         }
      }

      this.setup();
   }

   public YamlStorage(String name, String folder) {
      File file = new File(folder, name + ".yml");
      this.name = name;
      this.config = new YamlFile(file);
      if (!file.exists()) {
         try {
            this.config.createNewFile(false);
         } catch (IOException ex) {
            LOGGER.error("[Storage] Could not create/save " + name + ".yml!");
            LOGGER.error("[Storage] Error: " + ex.getMessage());
         }
      }

      this.setup();
   }

   public void setup() {
      this.setupConfigOptions(this.config.options());
      this.loadConfig();
   }

   public void loadConfig() {
      try {
         this.config.loadWithComments();
      } catch (IOException ex) {
         LOGGER.error("[Storage] Could not load " + this.name + ".yml, please correct your syntax errors!");
         LOGGER.error("[Storage] Error: " + ex.getMessage());
      }

   }

   public void reloadConfig() {
      try {
         this.config.loadWithComments();
      } catch (IOException ex) {
         LOGGER.error("[Storage] Could not load " + this.name + ".yml, please correct your syntax errors!");
         LOGGER.error("[Storage] Error: " + ex.getMessage());
      }

      this.saveConfig();
   }

   public void saveConfig() {
      try {
         this.config.save();
      } catch (IOException var2) {
         LOGGER.error("[Storage] Unable to save " + this.name + ".yml!");
      }

   }

   public void clearConfig() {
      this.config.getKeys(false).forEach((key) -> this.config.set(key, (Object)null));
      this.saveConfig();
   }

   public void setupConfigOptions(YamlConfigurationOptions options) {
      this.config.setCommentFormat(YamlCommentFormat.PRETTY);
      options.charset(Charsets.UTF_8);
      options.useComments(true);
      options.quoteStyleDefaults().setQuoteStyle(String.class, QuoteStyle.DOUBLE);
      options.quoteStyleDefaults().setQuoteStyle(List.class, QuoteStyle.DOUBLE);
      String[] header = this.getHeader();
      if (header != null) {
         options.header(String.join("\n", header));
      }

      SnakeYamlImplementation implementation = (SnakeYamlImplementation)options.configuration().getImplementation();
      implementation.getDumperOptions().setSplitLines(false);
   }

   public abstract void addSeparateComments();

   public String[] getHeader() {
      String[] header = null;
      if (this.getClass().isAnnotationPresent(Header.class)) {
         Header comment = (Header)this.getClass().getAnnotation(Header.class);
         header = comment.value();
      }

      return header;
   }

   public String getString(String path) {
      return this.config.contains(path) ? this.config.getString(path) : null;
   }

   public boolean contains(String path) {
      return this.config.contains(path);
   }

   public String getStringOrDefault(String path, String or) {
      return this.config.contains(path) ? this.config.getString(path) : or;
   }

   public int getInteger(String path) {
      return this.config.contains(path) ? this.config.getInt(path) : 0;
   }

   public int getInteger(String path, int or) {
      int toReturn = this.getInteger(path);
      return this.config.contains(path) ? toReturn : or;
   }

   public void set(String path, Object value) {
      this.config.set(path, value);
   }

   public boolean getBoolean(String path) {
      return this.config.contains(path) && this.config.getBoolean(path);
   }

   public double getDouble(String path) {
      return this.config.contains(path) ? this.config.getDouble(path) : (double)0.0F;
   }

   public void addComment(String path, String comment) {
      this.addComment(path, comment, false);
   }

   public void addCommentWithBlankLine(String path, String comment) {
      this.addComment(path, comment, true);
   }

   public void addComment(String path, String comment, boolean lineBreak) {
      this.addComment(path, new String[]{comment}, lineBreak);
   }

   public void addComment(String path, String[] comment, boolean lineBreak) {
      this.config.setComment(path, String.join("\n", comment));
      if (lineBreak) {
         this.config.setBlankLine(path);
      }

   }

   public Object get(String path) {
      return this.config.contains(path) ? this.config.get(path) : null;
   }

   public List getStringList(String path) {
      return this.config.contains(path) ? this.config.getStringList(path) : null;
   }

   public ConfigurationSection getConfigurationSection(String path) {
      return this.config.getConfigurationSection(path);
   }

   public ConfigurationSection createSection(String path) {
      return this.config.createSection(path);
   }

   public YamlConfiguration getConfiguration() {
      return this.config;
   }
}
