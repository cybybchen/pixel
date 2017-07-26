package com.trans.pixel.redis;


import redis.clients.jedis.ShardedJedis;
//这个接口是操作sharedJedis
public interface RedisDataSource {
    
    public abstract ShardedJedis getRedisClient();
    public void returnResource(ShardedJedis shardedJedis);
    public void returnResource(ShardedJedis shardedJedis,boolean broken);
}