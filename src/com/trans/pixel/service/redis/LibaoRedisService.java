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
		getPoolList();
	}
	
	public JewelPool.Builder getJewelPool(int id) {
		JewelPoolList.Builder listbuilder = getPoolList().getDataBuilder(0);
		for(JewelPool.Builder builder : listbuilder.getOrderBuilderList()){
			if(id == builder.getOrder())
				return builder;
		}
		return null;
	}

//	public JewelPoolList.Builder getJewelPoolList() {
//		String value = this.get(RedisKey.JEWELPOOL_CONFIG);
//		JewelPoolList.Builder builder = JewelPoolList.newBuilder();
//		if(value != null && parseJson(value, builder)){
//			return builder;
//		}else{
//			String xml = ReadConfig("lol_zuanshijijin.xml");
//			parseXml(xml, builder);
//			set(RedisKey.JEWELPOOL_CONFIG, formatJson(builder.build()));
//
//			return builder;
//		}
//	}


	public JewelPool.Builder getExpPool(int id) {
		JewelPoolList.Builder listbuilder = getPoolList().getDataBuilder(1);
		for(JewelPool.Builder builder : listbuilder.getOrderBuilderList()){
			if(id == builder.getOrder())
				return builder;
		}
		return null;
	}

	public JewelPoolLists.Builder getPoolList() {
		String value = get(RedisKey.JEWELPOOL_CONFIG);
		JewelPoolLists.Builder builder = JewelPoolLists.newBuilder();
		if(value != null && RedisService.parseJson(value, builder)){
			return builder;
		}else{
			String xml = RedisService.ReadConfig("ld_pool.xml");
			RedisService.parseXml(xml, builder);
			set(RedisKey.JEWELPOOL_CONFIG, RedisService.formatJson(builder.build()));

			return builder;
		}
	}
}
