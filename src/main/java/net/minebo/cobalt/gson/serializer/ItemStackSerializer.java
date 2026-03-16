package net.minebo.cobalt.gson.serializer;

import com.google.gson.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.Base64;

public class ItemStackSerializer implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    @Override
    public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return JsonNull.INSTANCE;
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (BukkitObjectOutputStream oos = new BukkitObjectOutputStream(baos)) {
                oos.writeObject(src);
            }
            return new JsonPrimitive(Base64.getEncoder().encodeToString(baos.toByteArray()));
        } catch (Exception e) {
            throw new JsonParseException("Failed to serialize ItemStack", e);
        }
    }

    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null || json.isJsonNull()) {
            return null;
        }

        try {
            byte[] data = Base64.getDecoder().decode(json.getAsString());
            try (BukkitObjectInputStream ois = new BukkitObjectInputStream(new ByteArrayInputStream(data))) {
                return (ItemStack) ois.readObject();
            }
        } catch (Exception e) {
            throw new JsonParseException("Failed to deserialize ItemStack", e);
        }
    }
}