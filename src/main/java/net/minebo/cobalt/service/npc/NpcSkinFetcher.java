package net.minebo.cobalt.service.npc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public final class NpcSkinFetcher {

    private NpcSkinFetcher() {
    }

    public static Optional<NpcSkin> fromUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        try {
            JsonObject profile = get("https://api.mojang.com/users/profiles/minecraft/" + username.trim());
            if (profile == null || !profile.has("id")) {
                return Optional.empty();
            }
            String id = profile.get("id").getAsString();
            String name = profile.has("name") ? profile.get("name").getAsString() : username;
            JsonObject session = get("https://sessionserver.mojang.com/session/minecraft/profile/" + id + "?unsigned=false");
            if (session == null || !session.has("properties")) {
                return Optional.empty();
            }
            JsonArray properties = session.getAsJsonArray("properties");
            for (int i = 0; i < properties.size(); i++) {
                JsonObject property = properties.get(i).getAsJsonObject();
                if (!"textures".equalsIgnoreCase(property.get("name").getAsString())) {
                    continue;
                }
                String value = property.get("value").getAsString();
                String signature = property.has("signature") ? property.get("signature").getAsString() : "";
                return Optional.of(new NpcSkin(value, signature, name));
            }
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }

    public static UUID offlineId(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name.toLowerCase(Locale.ROOT)).getBytes(StandardCharsets.UTF_8));
    }

    private static JsonObject get(String url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(4000);
        connection.setReadTimeout(4000);
        connection.setRequestProperty("User-Agent", "Cobalt-NPC");
        if (connection.getResponseCode() != 200) {
            return null;
        }
        try (InputStreamReader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } finally {
            connection.disconnect();
        }
    }
}