package net.minebo.cobalt.gson.serializer;

import com.google.gson.*;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.Map;

public class ItemStackSerializer implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    @Override
    public JsonElement serialize(ItemStack itemStack, Type typeOfSrc, JsonSerializationContext context) {
        Map<String, Object> serialized = itemStack.serialize();
        return context.serialize(serialized);
    }

    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Map<String, Object> map = context.deserialize(json, Map.class);
        return ItemStack.deserialize(map);
    }
}
