package net.minebo.cobalt.storage.utils;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import lombok.Generated;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ReflectionUtils {
   private static final Logger LOGGER = LogManager.getLogger(ReflectionUtils.class);
   private static Field LOOKUP_FIELD;
   private static Field MODIFIERS_FIELD;

   public static Field getField(String[] split, Object instance) {
      try {
         Field field = instance.getClass().getField(toFieldName(split[split.length - 1]));
         setAccessible(field);
         return field;
      } catch (Exception e) {
         LOGGER.error("Error (Invalid Field): {}", e.getMessage(), e);
         return null;
      }
   }

   public static String toFieldName(String node) {
      return node.toUpperCase().replaceAll("-", "_");
   }

   public static String toNodeName(String field) {
      return field.toUpperCase().replace("_", "-");
   }

   public static void setAccessible(Field field) {
      setAccessibleNonFinal(field);
   }

   public static void setAccessibleNonFinal(Field field) {
      field.setAccessible(true);
      if (Modifier.isFinal(field.getModifiers())) {
         try {
            if (getVersion() > 11) {
               ((MethodHandles.Lookup)LOOKUP_FIELD.get((Object)null)).findSetter(Field.class, "modifiers", Integer.TYPE).invokeExact(field, field.getModifiers() & -17);
            } else {
               MODIFIERS_FIELD.setInt(field, field.getModifiers() & -17);
            }
         } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
         }
      }

   }

   public static int getVersion() {
      String version = System.getProperty("java.version");
      if (version.startsWith("1.")) {
         version = version.substring(2, 3);
      } else {
         int dot = version.indexOf(".");
         if (dot != -1) {
            version = version.substring(0, dot);
         }
      }

      return Integer.parseInt(version);
   }

   static {
      if (getVersion() > 11) {
         try {
            LOOKUP_FIELD = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            LOOKUP_FIELD.setAccessible(true);
         } catch (Throwable e) {
            System.out.println("[Carbon] Failed to find trusted lookup field.");
            e.printStackTrace();
         }
      } else {
         try {
            MODIFIERS_FIELD = Field.class.getDeclaredField("modifiers");
            MODIFIERS_FIELD.setAccessible(true);
         } catch (Throwable e) {
            System.out.println("[Carbon] Failed to find modifiers field.");
            e.printStackTrace();
         }
      }

   }
}
