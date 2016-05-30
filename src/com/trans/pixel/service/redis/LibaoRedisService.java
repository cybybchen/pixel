package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.Commands.JewelPool;
import com.trans.pixel.protoc.Commands.JewelPoolList;
import com.trans.pixel.protoc.Commands.YueKa;
import com.trans.pixel.protoc.Commands.YueKaList;

@Repository
public class LibaoRedisService extends RedisService{
	Logger logger = Logger.getLogger(LibaoRedisService.class);
	
	public YueKa getYueKa(int id){
		String value = this.hget(RedisKey.YUEKA_CONFIG, ""+id);
		YueKa.Builder builder = YueKa.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			String xml = ReadConfig("lol_yueka.xml");
			YueKaList.Builder listbuilder = YueKaList.newBuilder();
			Map<String, String> keyvalue = new HashMap<String, String>();
			parseXml(xml, listbuilder);
			for(YueKa libao : listbuilder.getItemList()){
				if(libao.getItemid() == id)
					builder = YueKa.newBuilder(libao);
				keyvalue.put(libao.getItemid()+"", formatJson(libao));
			}
			hputAll(RedisKey.YUEKA_CONFIG, keyvalue);

			return builder.build();
		}
	}

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
