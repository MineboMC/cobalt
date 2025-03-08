package net.minebo.cobalt.storage.yaml.impl;

import java.lang.reflect.Field;
import java.util.List;

public abstract class ChildYamlStorage {

   private final ParentYamlStorage parentStorage;

   public ChildYamlStorage(ParentYamlStorage parentStorage) {
      this.parentStorage = parentStorage;
   }

   public abstract List<Field> getConfigFields();

   public ParentYamlStorage getParentStorage() {
      return parentStorage;
   }
}