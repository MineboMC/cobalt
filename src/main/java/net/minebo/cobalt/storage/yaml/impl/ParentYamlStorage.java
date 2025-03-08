package net.minebo.cobalt.storage.yaml.impl;

import com.google.common.base.Preconditions;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.minebo.cobalt.storage.annotations.ConfigValue;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class ParentYamlStorage extends StaticFieldsYamlStorage {
   private List<ChildYamlStorage> childStorages;

   public ParentYamlStorage(JavaPlugin plugin, String name, boolean saveResource) {
      super(plugin, name, saveResource);
   }

   public ParentYamlStorage(JavaPlugin plugin, String name) {
      super(plugin, name, false);
   }

   public void setup() {
      this.registerChildStorages();
      super.setup();
   }

   public void addChildStorage(ChildYamlStorage storage) {
      Preconditions.checkNotNull(storage, "[StorageAPI] Child Storage can not be null!");
      if (this.childStorages == null) {
         this.childStorages = new ArrayList();
      }

      this.childStorages.add(storage);
   }

   public List<Field> getConfigFields() {
      List<Field> annotatedFields = new ArrayList<>(this.getParentFields());

      // Add child fields
      this.childStorages.stream().map(ChildYamlStorage::getConfigFields).forEach(annotatedFields::addAll);
      Preconditions.checkArgument(annotatedFields.stream().allMatch(field -> field.isAnnotationPresent(ConfigValue.class)), "[Storage-API] One of your field is missing annotation!");

      // Sort according to priority
      annotatedFields.sort(Comparator.comparingInt(field -> field.getAnnotation(ConfigValue.class).priority()));

      return annotatedFields;
   }

   public abstract List getParentFields();

   public abstract void addSeparateComments();

   public abstract void registerChildStorages();
}
