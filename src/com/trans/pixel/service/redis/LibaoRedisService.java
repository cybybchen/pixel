package com.trans.pixel.service.redis;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.Base.JewelPool;
import com.trans.pixel.protoc.Base.JewelPoolList;

@Repository
public class LibaoRedisService extends RedisService{
	Logger logger = Logger.getLogger(LibaoRedisService.class);

	public JewelPool.Builder getJewelPool(int id) {
		JewelPoolList.Builder listbuilder = getJewelPoolList();
		for(JewelPool.Builder builder : listbuilder.getOrderBuilderList()){
			if(id == builder.getOrder())
				return builder;
		}
		return null;
	}

	public JewelPoolList.Builder getJewelPoolList() {
		String value = this.get(RedisKey.JEWELPOOL_CONFIG);
		JewelPoolList.Builder builder = JewelPoolList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder;
		}else{
			String xml = ReadConfig("lol_zuanshijijin.xml");
			parseXml(xml, builder);
			set(RedisKey.JEWELPOOL_CONFIG, formatJson(builder.build()));

			return builder;
		}
	}


	public JewelPool.Builder getExpPool(int id) {
		JewelPoolList.Builder listbuilder = getExpPoolList();
		for(JewelPool.Builder builder : listbuilder.getOrderBuilderList()){
			if(id == builder.getOrder())
				return builder;
		}
		return null;
	}

	public JewelPoolList.Builder getExpPoolList() {
		String value = this.get(RedisKey.EXPPOOL_CONFIG);
		JewelPoolList.Builder builder = JewelPoolList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder;
		}else{
			String xml = ReadConfig("lol_jingyanjijin.xml");
			parseXml(xml, builder);
			set(RedisKey.EXPPOOL_CONFIG, formatJson(builder.build()));

			return builder;
		}
	}
}
