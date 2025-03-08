package net.minebo.cobalt.skin;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import lombok.Generated;
import net.minebo.cobalt.skin.listener.SkinListener;
import net.minebo.cobalt.skin.player.IPlayerAdapter;
import net.minebo.cobalt.skin.player.impl.LegacyAdapter;
import net.minebo.cobalt.skin.player.impl.ModernAdapter;
import net.minebo.cobalt.storage.json.JsonStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SkinAPI {
   private static final Type TYPE = (new TypeToken() {
   }).getType();
   public static final CachedSkin DEFAULT = new CachedSkin("Default", "", "");
   private static final String ASHCON_URL = "https://api.ashcon.app/mojang/v2/user/%s";
   private static final String MOJANG_UUID_URL = "https://api.mojang.com/users/profiles/minecraft/%s";
   private static final String MOJANG_SKIN_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s";
   private final Map skinFutures = new ConcurrentHashMap();
   private final JsonStorage skinStorage;
   private final Map skinCache = new ConcurrentHashMap();
   private final Map temporaryCache = new ConcurrentHashMap();
   private final IPlayerAdapter playerAdapter;
   public static String VERSION;
   public static int MINOR_VERSION;
   public static boolean SUPPORTS_CARBON;

   public SkinAPI(JavaPlugin plugin, Gson gson) {
      this.skinStorage = new JsonStorage("skins", plugin, gson);
      this.initiateCacheFromStorage();
      if (MINOR_VERSION >= 13) {
         this.playerAdapter = new ModernAdapter(this);
      } else {
         this.playerAdapter = new LegacyAdapter(this);
      }

      Bukkit.getPluginManager().registerEvents(new SkinListener(this), plugin);
   }

   public void unload() {
      this.skinStorage.save(this.skinCache.values());
      this.skinCache.clear();
   }

   public void initiateCacheFromStorage() {
      Collection<CachedSkin> skinList = (Collection)this.skinStorage.getData(TYPE);
      if (skinList != null && !skinList.isEmpty()) {
         for(CachedSkin skin : skinList) {
            this.skinCache.put(skin.getName(), skin);
         }

      }
   }

   public CachedSkin getByPlayer(Player player) {
      return this.playerAdapter.getByPlayer(player);
   }

   public void registerSkin(Player player) {
      this.playerAdapter.registerSkin(player);
   }

   public void fetchSkin(String name, Consumer callback) {
      if (this.temporaryCache.containsKey(name)) {
         CachedSkin skin = (CachedSkin)this.temporaryCache.get(name);
         this.registerSkin(name, skin);
         callback.accept(skin);
      } else if (this.skinFutures.containsKey(name)) {
         CompletableFuture<CachedSkin> skinFuture = (CompletableFuture)this.skinFutures.get(name);
         skinFuture.whenComplete((skin, throwable) -> callback.accept(skin));
      } else {
         CompletableFuture<CachedSkin> skinFuture = CompletableFuture.supplyAsync(() -> this.fetchSkin(name));
         skinFuture.whenComplete((skin, throwable) -> {
            if (skin == null) {
               callback.accept(DEFAULT);
            } else {
               this.registerSkin(name, skin);
               callback.accept(skin);
               this.skinFutures.remove(name);
            }
         });
         this.skinFutures.put(name, skinFuture);
      }
   }

   public CachedSkin fetchSkin(String name) {
      try {
         URL url = new URL(String.format("https://api.ashcon.app/mojang/v2/user/%s", name));
         HttpURLConnection connection = (HttpURLConnection)url.openConnection();
         connection.setRequestMethod("GET");
         if (connection.getResponseCode() != 200) {
            return null;
         } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            CachedSkin var17;
            label48: {
               CachedSkin var13;
               try {
                  StringBuilder sb = new StringBuilder();

                  String inputLine;
                  while((inputLine = in.readLine()) != null) {
                     sb.append(inputLine);
                  }

                  JsonElement element = JsonParser.parseString(sb.toString());
                  if (!element.isJsonObject()) {
                     var17 = DEFAULT;
                     break label48;
                  }

                  JsonObject object = element.getAsJsonObject();
                  JsonObject textures = object.get("textures").getAsJsonObject();
                  JsonObject raw = textures.get("raw").getAsJsonObject();
                  String value = raw.get("value").getAsString();
                  String signature = raw.get("signature").getAsString();
                  var13 = new CachedSkin(name, value, signature);
               } catch (Throwable var15) {
                  try {
                     in.close();
                  } catch (Throwable var14) {
                     var15.addSuppressed(var14);
                  }

                  throw var15;
               }

               in.close();
               return var13;
            }

            in.close();
            return var17;
         }
      } catch (IOException | NullPointerException var16) {
         return DEFAULT;
      }
   }

   public void registerSkin(String name, CachedSkin skin) {
      this.skinCache.put(name, skin);
      this.skinStorage.saveAsync(this.skinCache.values());
   }

   public void registerPlayer(Player player) {
      if (!this.skinCache.containsKey(player.getName())) {
         CachedSkin skin = this.getByPlayer(player);
         this.temporaryCache.put(player.getName(), skin);
      }
   }

   public void clearPlayer(Player player) {
      this.temporaryCache.remove(player.getName());
   }

   public CachedSkin getSkin(String name) {
      return (CachedSkin)this.temporaryCache.getOrDefault(name, (CachedSkin)this.skinCache.getOrDefault(name, (Object)null));
   }

   static {
      try {
         String versionName = Bukkit.getServer().getClass().getPackage().getName();
         VERSION = versionName.length() < 4 ? versionName.split("\\.")[2] : versionName.split("\\.")[3];
         MINOR_VERSION = Integer.parseInt(VERSION.split("_")[1]);
      } catch (Exception var1) {
         String var10000 = Bukkit.getServer().getBukkitVersion().replace("-SNAPSHOT", "").replace("-R0.", "_R");
         VERSION = "v" + var10000.replace(".", "_");
         MINOR_VERSION = Integer.parseInt(VERSION.split("_")[1]);
      }

   }
}
