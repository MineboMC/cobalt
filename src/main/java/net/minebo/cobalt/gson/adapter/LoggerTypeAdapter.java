package net.minebo.cobalt.gson.adapter;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.logging.Logger;

public class LoggerTypeAdapter extends TypeAdapter<Logger> {
    @Override
    public void write(JsonWriter out, Logger value) throws IOException {
        // Custom logic to serialize the Logger object, e.g., write a simplified version or ignore it.
        out.beginObject();
        out.name("name").value(value.getName());
        out.endObject();
    }

    @Override
    public Logger read(JsonReader in) throws IOException {
        // Custom logic to deserialize the Logger object.
        JsonObject jsonObject = JsonParser.parseReader(in).getAsJsonObject();
        String name = jsonObject.get("name").getAsString();
        return Logger.getLogger(name); // Or any other deserialization logic you need.
    }
}
