package net.minebo.cobalt.gson;

import com.google.gson.GsonBuilder;
import net.minebo.cobalt.gson.serializer.LocationSerializer;
import org.bukkit.Location;

public class Gson {
   public static com.google.gson.Gson GSON;
   public static GsonBuilder gsonBuilder = new GsonBuilder();

   public static void init() {
      gsonBuilder.registerTypeAdapter(Location.class, new LocationSerializer());
      GSON = gsonBuilder.create();
   }
}
