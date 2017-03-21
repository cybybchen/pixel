package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.EquipProto.Prop;
import com.trans.pixel.protoc.EquipProto.PropList;

@Service
public class PropRedisService extends RedisService {
	private static Logger logger = Logger.getLogger(PropRedisService.class);
	private static final String PACKAGE_FILE_NAME = "ld_package.xml";
	@Resource
	public RedisTemplate<String, String> redisTemplate;
	
	//hero
	public Prop getPackage(int id) {
		String value = hget(RedisKey.PROP_KEY, "" + id);
		if (value == null) {
			Map<String, Prop> packageConfig = getPackageConfig();
			return packageConfig.get("" + id);
		} else {
			Prop.Builder builder = Prop.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	private Map<String, Prop> getPackageConfig() {
		Map<String, String> keyvalue = hget(RedisKey.PROP_KEY);
		if(keyvalue.isEmpty()){
			Map<String, Prop> map = buildPackageConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Prop> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.PROP_KEY, redismap);
			return map;
		}else{
			Map<String, Prop> map = new HashMap<String, Prop>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Prop.Builder builder = Prop.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Prop> buildPackageConfig(){
		String xml = ReadConfig(PACKAGE_FILE_NAME);
		PropList.Builder builder = PropList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + PACKAGE_FILE_NAME);
			return null;
		}
		
		Map<String, Prop> map = new HashMap<String, Prop>();
		for(Prop.Builder prop : builder.getLootBuilderList()){
			map.put("" + prop.getItemid(), prop.build());
		}
		return map;
	}
}
