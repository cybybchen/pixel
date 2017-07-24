package com.trans.pixel.service.redis;

import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.UserInfoProto.MerlevelList;
import com.trans.pixel.service.cache.CacheService;

@Repository
public class ZhanliRedisService extends CacheService {
//	private static Logger logger = Logger.getLogger(ZhanliRedisService.class);
	
	public ZhanliRedisService() {
		buildMerlevelConfig();
	}
	
//	public int getMerlevel(int level){
//		Map<Integer, Integer> map = hgetcache(RedisKey.MERLEVEL_CONFIG);
//		return map.get(level);
//	}
//
//	public void buildMerlevelConfig(){
//		MerlevelList.Builder list = getMerlevel();
//		Map<String, String> keyvalue = new HashMap<String, String>();
//		for(Merlevel merlevel : list.getLevelList()){
//			keyvalue.put(merlevel.getLevel()+"", merlevel.getScore()+"");
//			if(merlevel.getLevel() == level)
//				score = merlevel.getScore();
//		}
//		hputcacheAll(RedisKey.MERLEVEL_CONFIG, keyvalue);
//	}
	
	public MerlevelList.Builder getMerlevel(){
		MerlevelList list = getcache(RedisKey.MERCENARY_CONFIG);
		MerlevelList.Builder builder = MerlevelList.newBuilder(list);
		return builder;
	}

	private void buildMerlevelConfig(){
		String xml = RedisService.ReadConfig("ld_mercenary.xml");
		MerlevelList.Builder list = MerlevelList.newBuilder();
		RedisService.parseXml(xml, list);
		setcache(RedisKey.MERCENARY_CONFIG, list.build());
	}
}
