package net.minebo.cobalt.redis.packet;

public interface RedisPacket {
   void onReceive();

   void onSend();
}
