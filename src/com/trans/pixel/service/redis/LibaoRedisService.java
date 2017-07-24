package com.trans.pixel.service.redis;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.Base.JewelPool;
import com.trans.pixel.protoc.Base.JewelPoolList;
import com.trans.pixel.protoc.Base.JewelPoolLists;
import com.trans.pixel.service.cache.CacheService;

@Repository
public class LibaoRedisService extends CacheService{
	Logger logger = Logger.getLogger(LibaoRedisService.class);

	public LibaoRedisService() {
		buildPoolConfig();
	}
	
	public JewelPool.Builder getJewelPool(int id) {
		JewelPoolLists list = getcache(RedisKey.JEWELPOOL_CONFIG);
		JewelPoolList poollist = list.getData(0);
		for(JewelPool pool : poollist.getOrderList()){
			if(id == pool.getOrder())
				return JewelPool.newBuilder(pool);
		}
		return null;
	}

	public JewelPool.Builder getExpPool(int id) {
		JewelPoolLists list = getcache(RedisKey.JEWELPOOL_CONFIG);
		JewelPoolList poollist = list.getData(1);
		for(JewelPool pool : poollist.getOrderList()){
			if(id == pool.getOrder())
				return JewelPool.newBuilder(pool);
		}
		return null;
	}

	private void buildPoolConfig() {
		String xml = RedisService.ReadConfig("ld_pool.xml");
		JewelPoolLists.Builder builder = JewelPoolLists.newBuilder();
		RedisService.parseXml(xml, builder);
		setcache(RedisKey.JEWELPOOL_CONFIG, builder.build());
	}
}
