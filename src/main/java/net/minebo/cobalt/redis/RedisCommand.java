package net.minebo.cobalt.redis;

import redis.clients.jedis.Jedis;

public interface RedisCommand {
   Object execute(Jedis var1);
}
