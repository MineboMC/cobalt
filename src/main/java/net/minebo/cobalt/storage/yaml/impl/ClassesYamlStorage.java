package net.minebo.cobalt.storage.yaml.impl;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Arrays;
import net.minebo.cobalt.storage.annotations.Comment;
import net.minebo.cobalt.storage.annotations.Create;
import net.minebo.cobalt.storage.annotations.Ignore;
import net.minebo.cobalt.storage.utils.ReflectionUtils;
import net.minebo.cobalt.storage.yaml.YamlStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleyaml.configuration.ConfigurationSection;

public abstract class ClassesYamlStorage extends YamlStorage {
   private static final Logger LOGGER = LogManager.getLogger(ClassesYamlStorage.class);

   public ClassesYamlStorage(JavaPlugin plugin, String name, boolean saveResource) {
      super(plugin, name, saveResource);
   }

   public ClassesYamlStorage(String name, String dataFolder) {
      super(name, dataFolder);
   }

   public void setup() {
      this.setupConfigOptions(this.config.options());

      try {
         this.setupInstances("", this.getClass(), this);
      } catch (Exception e) {
         LOGGER.error("[Storage] Failed to setup instances.", e);
      }

      this.loadConfig();
      this.setSectionValue(this.config.getConfigurationSection(this.config.getCurrentPath()), "");
      this.clearConfig();
      this.saveConfig();
   }

   public void addSeparateComments() {
   }

   public void saveConfig() {
      try {
         this.save("", this.getClass(), this);
         this.config.save();
      } catch (Exception e) {
         LOGGER.error("[Storage] Unable to save {}.yml!", this.name, e);
      }

   }

   public void setupInstances(String path, Class clazz, Object instance) throws Exception {
      for(Field field : clazz.getFields()) {
         if (field.getAnnotation(Ignore.class) == null) {
            String prefix = !path.isEmpty() ? "." : "";
            if (field.isAnnotationPresent(Create.class)) {
               Class<?> current = field.getType();
               ReflectionUtils.setAccessible(field);
               Object value = field.get(instance);
               if (value == null) {
                  value = current.getConstructor().newInstance();
                  field.set(instance, value);
               }

               this.setupInstances(path + prefix + ReflectionUtils.toNodeName(current.getSimpleName()), current, value);
            }
         }
      }

   }

   public void save(String path, Class clazz, Object instance) throws Exception {
      for(Field field : clazz.getFields()) {
         if (field.getAnnotation(Ignore.class) == null) {
            String prefix = !path.isEmpty() ? "." : "";
            String currentPath = path + prefix + ReflectionUtils.toNodeName(field.getName());
            if (!field.isAnnotationPresent(Create.class)) {
               if (field.isAnnotationPresent(Comment.class)) {
                  Comment comment = (Comment)field.getAnnotation(Comment.class);
                  this.addComment(currentPath, comment.value(), comment.lineBreak());
               }

               this.config.set(currentPath, field.get(instance));
            } else {
               Class<?> current = field.getType();
               if (current.isAnnotationPresent(Comment.class)) {
                  Comment comment = (Comment)current.getAnnotation(Comment.class);
                  this.addComment(currentPath, comment.value(), comment.lineBreak());
               }

               ReflectionUtils.setAccessible(field);
               Object value = field.get(instance);
               if (value == null) {
                  value = current.getConstructor().newInstance();
                  field.set(instance, value);
               }

               this.save(path + prefix + ReflectionUtils.toNodeName(current.getSimpleName()), current, value);
            }
         }
      }

   }

   private void setSectionValue(ConfigurationSection yml, String oldPath) {
      for(String key : yml.getKeys(false)) {
         Object value = yml.get(key);
         String newPath = oldPath + (oldPath.isEmpty() ? "" : ".") + key;
         if (value instanceof ConfigurationSection) {
            this.setSectionValue((ConfigurationSection)value, newPath.toUpperCase());
         } else {
            this.setKeyValue(newPath.toUpperCase(), value);
         }
      }

   }

   private void setKeyValue(String key, Object value) {
      String[] split = key.split("\\.");
      Object instance = this.getInstance(split, this.getClass());
      if (instance != null) {
         Field field = ReflectionUtils.getField(split, instance);
         if (field != null) {
            try {
               if (field.getType() == String.class && !(value instanceof String)) {
                  value = value.toString();
               }

               field.set(instance, value);
            } catch (IllegalArgumentException | IllegalAccessException e) {
               LOGGER.error("Failed to set value for {}: {}", key, e);
            }

         }
      }
   }

   private Object getInstance(String[] split, Class root) {
      try {
         Class<?> clazz = root == null ? MethodHandles.lookup().lookupClass() : root;
         Object instance = this;

         while(split.length > 0) {
            if (split.length == 1) {
               return instance;
            }

            String[] finalSplit = split;
            if (clazz == null) {
               for(Class current : root.getDeclaredClasses()) {
                  if (Arrays.stream(current.getDeclaredFields()).anyMatch((f) -> f.getName().equalsIgnoreCase(ReflectionUtils.toFieldName(finalSplit[0])))) {
                     clazz = current;
                     break;
                  }
               }

               if (clazz == null) {
                  return null;
               }
            }

            Class<?> found = Arrays.stream(clazz.getDeclaredClasses()).filter((currentx) -> currentx.getSimpleName().equalsIgnoreCase(ReflectionUtils.toFieldName(finalSplit[0]))).findFirst().orElse(null);

            try {
               Field instanceField = clazz.getDeclaredField(ReflectionUtils.toFieldName(split[0]));
               ReflectionUtils.setAccessible(instanceField);
               Object value = instanceField.get(instance);
               if (value == null && found != null) {
                  value = found.getDeclaredConstructor().newInstance();
                  instanceField.set(instance, value);
               }

               clazz = found;
               instance = value;
               split = Arrays.copyOfRange(split, 1, split.length);
            } catch (NoSuchFieldException var10) {
               return null;
            }
         }
      } catch (ReflectiveOperationException e) {
         LOGGER.error("[Storage] Invalid Instance. {}", e.getMessage(), e);
      }

      return null;
   }
}
