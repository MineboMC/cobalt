package net.minebo.cobalt.redis.thread;

import net.minebo.cobalt.redis.RedisDatabase;
import net.minebo.cobalt.redis.packet.PacketPubSub;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class SubscribeThread {
   public static void start() {
      Thread subscribeThread = new Thread(() -> RedisDatabase.jedisPools.values().forEach((pool) -> {
            try {
               Jedis jedis = pool.getResource();

               try {
                  JedisPubSub pubSub = new PacketPubSub();
                  String channel = "Hermes:Global";
                  jedis.subscribe(pubSub, channel);
               } catch (Throwable var5) {
                  if (jedis != null) {
                     try {
                        jedis.close();
                     } catch (Throwable var4) {
                        var5.addSuppressed(var4);
                     }
                  }

                  throw var5;
               }

               if (jedis != null) {
                  jedis.close();
               }
            } catch (JedisConnectionException var6) {
            } catch (Exception ex) {
               ex.printStackTrace();
            }

         }), "Cobalt - Packet Subscription Thread");
      subscribeThread.setDaemon(true);
      subscribeThread.start();
   }
}
