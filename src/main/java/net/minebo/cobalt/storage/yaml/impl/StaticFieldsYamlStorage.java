package net.minebo.cobalt.storage.yaml.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minebo.cobalt.storage.annotations.ConfigValue;
import net.minebo.cobalt.storage.yaml.YamlStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public class StaticFieldsYamlStorage extends YamlStorage {
   private static final Logger LOGGER = LogManager.getLogger(StaticFieldsYamlStorage.class);
   private List<Field> fields;

   public StaticFieldsYamlStorage(String name, String folder) {
      super(name, folder);
      this.readConfig();
   }

   public StaticFieldsYamlStorage(JavaPlugin plugin, String name, boolean saveResource) {
      super(plugin, name, saveResource);
      this.readConfig();
   }

   public void setup() {
      this.fields = this.getConfigFields();
      super.setup();
   }

   public void readConfig() {
      for(Field field : this.fields) {
         try {
            ConfigValue configValue = (ConfigValue)field.getAnnotation(ConfigValue.class);
            Object value = field.get((Object)null);
            if (this.config.contains(configValue.path())) {
               field.setAccessible(true);
               field.set((Object)null, this.config.get(configValue.path()));
               field.setAccessible(false);
            } else {
               this.config.set(configValue.path(), value);
            }

            if (!configValue.comment().isEmpty()) {
               this.config.path(configValue.path()).comment(configValue.comment()).blankLine();
            }
         } catch (IllegalAccessException | IllegalArgumentException ex) {
            LOGGER.error("[Storage] Error invoking {}", field, ex);
         }
      }

      this.addSeparateComments();
      this.saveConfig();
   }

   public void writeConfig() {
      for(Field field : this.fields) {
         ConfigValue configValue = (ConfigValue)field.getAnnotation(ConfigValue.class);

         try {
            Object value = field.get((Object)null);
            this.config.set(configValue.path(), value);
         } catch (IllegalAccessException | IllegalArgumentException ex) {
            LOGGER.error("[Storage] Error invoking " + String.valueOf(field), ex);
         }
      }

      this.saveConfig();
   }

   public void reloadConfig() {
      try {
         this.config.loadWithComments();
      } catch (IOException ex) {
         LOGGER.error("[Storage] Could not load {}.yml, please correct your syntax errors!", this.name);
         LOGGER.error("[Storage] Error: {}", ex.getMessage());
      }

      this.readConfig();
   }

   public void addSeparateComments() {
   }

   public List getConfigFields() {
      List<Field> annotatedFields = new ArrayList();

      for(Field field : this.getClass().getDeclaredFields()) {
         if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
            ConfigValue configValue = (ConfigValue)field.getAnnotation(ConfigValue.class);
            if (configValue != null) {
               annotatedFields.add(field);
            }
         }
      }

      annotatedFields.sort(Comparator.comparingInt((fieldx) -> ((ConfigValue)fieldx.getAnnotation(ConfigValue.class)).priority()));
      return annotatedFields;
   }
}
