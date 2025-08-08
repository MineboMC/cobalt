package net.minebo.cobalt.redis;

import java.util.HashMap;
import net.minebo.cobalt.gson.Gson;
import net.minebo.cobalt.redis.packet.RedisPacket;
import net.minebo.cobalt.redis.thread.SubscribeThread;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisDatabase {
   public static HashMap<String, JedisPool> jedisPools = new HashMap<>();

   public static JedisPool getJedisPool(String poolId) {
      for(String id : jedisPools.keySet()) {
         if (id.equalsIgnoreCase(poolId)) {
            return jedisPools.get(id);
         }
      }

      return null;
   }

   public RedisDatabase(String poolId, String host, Integer port, String username, String password, Integer db) {
      try {
         jedisPools.put(poolId, new JedisPool(new JedisPoolConfig(), host, port, 20000, username, password, db));
      } catch (Exception e) {
         jedisPools.remove(poolId);
         e.printStackTrace();
         System.out.println("Couldn't connect to a Redis database at " + host + ".");
      }

      SubscribeThread.start();
   }

   public RedisDatabase(String poolId, String host, Integer port, Integer db) {
      try {
         jedisPools.put(poolId, new JedisPool(new JedisPoolConfig(), host, port, 20000, (String)null, db));
      } catch (Exception e) {
         jedisPools.remove(poolId);
         e.printStackTrace();
         System.out.println("Couldn't connect to a Redis database at " + host + ".");
      }

      SubscribeThread.start();
   }

   public Object runRedisCommand(JedisPool pool, RedisCommand redisCommand) {
      Jedis jedis = pool.getResource();

      Object result;
      try {
         result = redisCommand.execute(jedis);
      } catch (Throwable var8) {
         if (jedis != null) {
            try {
               jedis.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }
         }
         throw var8;
      }

      if (jedis != null) {
         jedis.close();
      }

      return result;
   }

   public void setData(JedisPool pool, String key, String value) {
      this.runRedisCommand(pool, (r) -> {
         r.set(key, value);
         return null;
      });
   }

   public String getData(JedisPool pool, String key) {
      return (String)this.runRedisCommand(pool, (r) -> r.get(key));
   }

   public Boolean ifExists(JedisPool pool, String key) {
      return (Boolean)this.runRedisCommand(pool, (r) -> r.exists(key));
   }

   public static void sendPacket(JedisPool pool, RedisPacket packet) {
      packet.onSend();
      Jedis jedis = pool.getResource();
      Throwable var3 = null;

      try {
         String var10000 = packet.getClass().getName();
         String encodedPacket = var10000 + "||" + Gson.GSON.toJson(packet);
         jedis.publish("Cobalt:Global", encodedPacket);
      } catch (Throwable var12) {
         var12.printStackTrace();
         var3 = var12;
         throw var12;
      } finally {
         if (jedis != null) {
            if (var3 != null) {
               try {
                  jedis.close();
               } catch (Throwable var11) {
                  var3.addSuppressed(var11);
               }
            } else {
               jedis.close();
            }
         }

      }

   }
}
