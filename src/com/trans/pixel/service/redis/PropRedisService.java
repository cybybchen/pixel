package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.EquipProto.Prop;
import com.trans.pixel.protoc.EquipProto.PropList;
import com.trans.pixel.protoc.EquipProto.Synthetise;
import com.trans.pixel.protoc.EquipProto.SynthetiseList;
import com.trans.pixel.service.cache.CacheService;

@Service
public class PropRedisService extends CacheService {
	private static Logger logger = Logger.getLogger(PropRedisService.class);
	private static final String PACKAGE_FILE_NAME = "ld_package.xml";
	private static final String SYNTHETISE_FILE_NAME = "ld_equipcompose.xml";
	
	public PropRedisService() {
		buildPackageConfig();
		buildSynthetiseConfig();
	}
	
	//pro
	public Prop getPackage(int id) {
		Map<Integer, Prop> map = hgetcache(RedisKey.PROP_KEY);
		return map.get(id);
	}
	
	public Map<Integer, Prop> getPackageConfig() {
		Map<Integer, Prop> map = hgetcache(RedisKey.PROP_KEY);
		return map;
	}
	
	private Map<Integer, Prop> buildPackageConfig(){
		String xml = RedisService.ReadConfig(PACKAGE_FILE_NAME);
		PropList.Builder builder = PropList.newBuilder();
		if(!RedisService.parseXml(xml, builder)){
			logger.warn("cannot build " + PACKAGE_FILE_NAME);
			return null;
		}
		
		Map<Integer, Prop> map = new HashMap<Integer, Prop>();
		for(Prop.Builder prop : builder.getDataBuilderList()){
			map.put(prop.getItemid(), prop.build());
		}
		CacheService.hputcacheAll(RedisKey.PROP_KEY, map);
		
		return map;
	}
	
	//syn
	public Synthetise getSynthetise(int id) {
		Map<Integer, Synthetise> map = hgetcache(RedisKey.SYNTHETISE_KEY);
		return map.get(id);
	}

	private void buildSynthetiseConfig(){
		String xml = RedisService.ReadConfig(SYNTHETISE_FILE_NAME);
		SynthetiseList.Builder builder = SynthetiseList.newBuilder();
		RedisService.parseXml(xml, builder);
		Map<Integer, Synthetise> map = new HashMap<Integer, Synthetise>();
		for(Synthetise.Builder syn : builder.getDataBuilderList()){
			map.put(syn.getId(), syn.build());
		}
		CacheService.hputcacheAll(RedisKey.SYNTHETISE_KEY, map);
	}
}
