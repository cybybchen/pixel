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
import com.trans.pixel.protoc.EquipProto.Synthetise;
import com.trans.pixel.protoc.EquipProto.SynthetiseList;

@Service
public class PropRedisService extends RedisService {
	private static Logger logger = Logger.getLogger(PropRedisService.class);
	private static final String PACKAGE_FILE_NAME = "ld_package.xml";
	private static final String SYNTHETISE_FILE_NAME = "ld_equipcompose.xml";
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
	
	public Map<String, Prop> getPackageConfig() {
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
		for(Prop.Builder prop : builder.getDataBuilderList()){
			map.put("" + prop.getItemid(), prop.build());
		}
		return map;
	}
	
	//hero
	public Synthetise getSynthetise(int id) {
		String value = hget(RedisKey.SYNTHETISE_KEY, "" + id);
		if (value == null) {
			Map<String, Synthetise> config = getSynthetiseConfig();
			return config.get("" + id);
		} else {
			Synthetise.Builder builder = Synthetise.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	private Map<String, Synthetise> getSynthetiseConfig() {
		Map<String, String> keyvalue = hget(RedisKey.SYNTHETISE_KEY);
		if(keyvalue.isEmpty()){
			Map<String, Synthetise> map = buildSynthetiseConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Synthetise> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.SYNTHETISE_KEY, redismap);
			return map;
		}else{
			Map<String, Synthetise> map = new HashMap<String, Synthetise>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Synthetise.Builder builder = Synthetise.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Synthetise> buildSynthetiseConfig(){
		String xml = ReadConfig(SYNTHETISE_FILE_NAME);
		SynthetiseList.Builder builder = SynthetiseList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + SYNTHETISE_FILE_NAME);
			return null;
		}
		
		Map<String, Synthetise> map = new HashMap<String, Synthetise>();
		for(Synthetise.Builder syn : builder.getDataBuilderList()){
			map.put("" + syn.getId(), syn.build());
		}
		return map;
	}
}
