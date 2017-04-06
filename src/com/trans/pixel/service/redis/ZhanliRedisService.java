package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.UserInfoProto.Merlevel;
import com.trans.pixel.protoc.UserInfoProto.MerlevelList;

@Repository
public class ZhanliRedisService extends RedisService {
//	private static Logger logger = Logger.getLogger(ZhanliRedisService.class);
	
	public int getMerlevel(int level){
		String value = hget(RedisKey.MERLEVEL_CONFIG, level+"");
		int score = 0;
		if(value != null){
			score = Integer.parseInt(value);
		}else if(!this.exists(RedisKey.MERLEVEL_CONFIG)){
			MerlevelList.Builder list = getMerlevel();
			Map<String, String> keyvalue = new HashMap<String, String>();
			for(Merlevel merlevel : list.getLevelList()){
				keyvalue.put(merlevel.getLevel()+"", merlevel.getScore()+"");
				if(merlevel.getLevel() == level)
					score = merlevel.getScore();
			}
			hputAll(RedisKey.MERLEVEL_CONFIG, keyvalue);
		}
		return score;
	}
	
	public MerlevelList.Builder getMerlevel(){
		String value = get(RedisKey.MERCENARY_CONFIG);
		MerlevelList.Builder list = MerlevelList.newBuilder();
		if(value != null && parseJson(value, list)){
			return list;
		}
		String xml = ReadConfig("ld_mercenary.xml");
		parseXml(xml, list);
		set(RedisKey.MERCENARY_CONFIG, formatJson(list.build()));
		return list;
	}
}