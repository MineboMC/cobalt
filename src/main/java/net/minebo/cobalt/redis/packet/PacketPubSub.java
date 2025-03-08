package net.minebo.cobalt.redis.packet;

import net.minebo.cobalt.gson.Gson;
import redis.clients.jedis.JedisPubSub;

public class PacketPubSub extends JedisPubSub {
   public void onMessage(String channel, String message) {
      int packetMessageSplit = message.indexOf("||");
      String packetClassStr = message.substring(0, packetMessageSplit);
      String messageJson = message.substring(packetMessageSplit + "||".length());

      Class<?> packetClass;
      try {
         packetClass = Class.forName(packetClassStr);
      } catch (ClassNotFoundException var8) {
         return;
      }

      RedisPacket packet = (RedisPacket)Gson.GSON.fromJson(messageJson, packetClass);
      packet.onReceive();
   }
}
