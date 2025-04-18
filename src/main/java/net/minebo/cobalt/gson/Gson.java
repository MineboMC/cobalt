package net.minebo.cobalt.gson;

import com.google.gson.GsonBuilder;
import net.minebo.cobalt.gson.adapter.OptionalTypeAdapter;
import net.minebo.cobalt.gson.serializer.ItemStackSerializer;
import net.minebo.cobalt.gson.serializer.LocationSerializer;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class Gson {
   public static com.google.gson.Gson GSON;
   public static GsonBuilder gsonBuilder = new GsonBuilder();

   public static void init() {
      gsonBuilder.registerTypeAdapter(Location.class, new LocationSerializer());
      gsonBuilder.registerTypeAdapter(ItemStack.class, new ItemStackSerializer());
      gsonBuilder.registerTypeAdapter(Optional.class, new OptionalTypeAdapter());
      GSON = gsonBuilder.create();
   }
}
