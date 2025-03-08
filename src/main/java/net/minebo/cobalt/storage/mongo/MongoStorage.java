package net.minebo.cobalt.storage.mongo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Updates;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.print.Doc;

public class MongoStorage {
   private static final ReplaceOptions REPLACE_OPTIONS = (new ReplaceOptions()).upsert(true);
   private final MongoCollection<Document> collection;
   private final Gson gson;
   private final Type typeToken;

   public MongoStorage(MongoCollection collection, Gson gson) {
      this.collection = collection;
      this.gson = gson;
      this.typeToken = (new TypeToken() {
      }).getType();
   }

   public CompletableFuture fetchAllEntries() {
      return CompletableFuture.supplyAsync(() -> {
         List<Document> found = new ArrayList();

         for(Document document : this.collection.find()) {
            if (document != null) {
               found.add(this.gson.fromJson(document.toJson(), this.typeToken));
            }
         }

         return found;
      });
   }

   public CompletableFuture fetchAllRawEntries() {
      return CompletableFuture.supplyAsync(() -> {
         List<Document> found = new ArrayList();

         for(Document document : this.collection.find()) {
            found.add(document);
         }

         return found;
      });
   }

   public void saveData(UUID key, Object value, Type type) {
      CompletableFuture.runAsync(() -> this.saveDataSync(key, value, type));
   }

   public void saveDataSync(UUID key, Object value, Type type) {
      Bson query = Filters.eq("_id", key.toString());
      Document parsed = Document.parse(this.gson.toJson(value, type));
      this.collection.replaceOne(query, parsed, REPLACE_OPTIONS);
   }

   public void saveRawData(UUID key, Document document) {
      CompletableFuture.runAsync(() -> this.saveRawDataSync(key, document));
   }

   public void saveRawDataSync(UUID key, Document document) {
      Bson query = Filters.eq("_id", key.toString());
      this.collection.replaceOne(query, document, REPLACE_OPTIONS);
   }

   public Object loadData(UUID key, Type type) {
      Bson query = Filters.eq("_id", key.toString());
      Document document = (Document)this.collection.find(query).first();
      return document == null ? null : this.gson.fromJson(document.toJson(), type);
   }

   public CompletableFuture loadDataAsync(UUID key, Type type) {
      return CompletableFuture.supplyAsync(() -> this.loadData(key, type));
   }

   public Document loadRawData(UUID key) {
      Bson query = Filters.eq("_id", key.toString());
      return (Document)this.collection.find(query).first();
   }

   public CompletableFuture loadRawDataAsync(UUID key) {
      return CompletableFuture.supplyAsync(() -> this.loadRawData(key));
   }

   public void deleteData(UUID key) {
      CompletableFuture.runAsync(() -> {
         Bson query = Filters.eq("_id", key.toString());
         this.collection.deleteOne(query);
      });
   }

   public CompletableFuture deleteKeyInAll(String key) {
      return CompletableFuture.supplyAsync(() -> {
         Bson combinedUpdate = Updates.unset(key);
         return this.collection.updateMany(new Document(), (Bson)combinedUpdate).getModifiedCount();
      });
   }
}
