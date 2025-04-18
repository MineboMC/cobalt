package net.minebo.cobalt.gson.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class LocationSerializer implements JsonSerializer<Location>, JsonDeserializer<Location> {
   public JsonElement serialize(Location location, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject locationJson = new JsonObject();
      locationJson.addProperty("world", location.getWorld().getName());
      locationJson.addProperty("x", (Number)location.getX());
      locationJson.addProperty("y", (Number)location.getY());
      locationJson.addProperty("z", (Number)location.getZ());
      locationJson.addProperty("yaw", (Number)location.getYaw());
      locationJson.addProperty("pitch", (Number)location.getPitch());
      return locationJson;
   }

   public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      JsonObject jsonObject = json.getAsJsonObject();
      String worldName = jsonObject.get("world").getAsString();
      double x = jsonObject.get("x").getAsDouble();
      double y = jsonObject.get("y").getAsDouble();
      double z = jsonObject.get("z").getAsDouble();
      float yaw = jsonObject.get("yaw").getAsFloat();
      float pitch = jsonObject.get("pitch").getAsFloat();
      return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
   }
}
