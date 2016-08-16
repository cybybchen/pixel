package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.Commands.BosskillRecord;
import com.trans.pixel.protoc.Commands.Richang;
import com.trans.pixel.protoc.Commands.RichangList;

@Service
public class BossRedisService extends RedisService {
	private static Logger logger = Logger.getLogger(BossRedisService.class);
	private static final String ACTIVITY_RICHANG_FILE_NAME = "lol_taskrichang.xml";
	private static final String ACTIVITY_KAIFU2_FILE_NAME = "lol_taskkaifu2.xml";
	private static final String ACTIVITY_KAIFU_FILE_NAME = "lol_taskkaifu1.xml";
	private static final String ACTIVITY_SHOUCHONG_FILE_NAME = "lol_taskshouchong.xml";
	private static final String ACTIVITY_FILE_PREFIX = "activity/activity_";
	
	//richang activity
	public Richang getRichang(int id) {
		String value = hget(RedisKey.ACTIVITY_RICHANG_KEY, "" + id);
		if (value == null) {
			Map<String, Richang> config = getRichangConfig();
			return config.get("" + id);
		} else {
			Richang.Builder builder = Richang.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Richang> getRichangConfig() {
		Map<String, String> keyvalue = hget(RedisKey.ACTIVITY_RICHANG_KEY);
		if(keyvalue.isEmpty()){
			Map<String, Richang> map = buildRichangConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Richang> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.ACTIVITY_RICHANG_KEY, redismap);
			return map;
		}else{
			Map<String, Richang> map = new HashMap<String, Richang>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Richang.Builder builder = Richang.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Richang> buildRichangConfig(){
		String xml = ReadConfig(ACTIVITY_RICHANG_FILE_NAME);
		RichangList.Builder builder = RichangList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + ACTIVITY_RICHANG_FILE_NAME);
			return null;
		}
		
		Map<String, Richang> map = new HashMap<String, Richang>();
		for(Richang.Builder richang : builder.getRichangBuilderList()){
			map.put("" + richang.getId(), richang.build());
		}
		return map;
	}
	
	public List<BosskillRecord> getBosskillRecord(long userId) {
		List<BosskillRecord> list = new ArrayList<BosskillRecord>();
		Map<String, String> keyvalue = hget(RedisKey.BOSSKILL_RECORD_PREFIX + userId);
		if(!keyvalue.isEmpty()){
			for(Entry<String, String> entry : keyvalue.entrySet()){
				BosskillRecord.Builder builder = BosskillRecord.newBuilder();
				if(parseJson(entry.getValue(), builder))
					list.add(builder.build());
			}
		}
		
		return list;
	}
	
	public BosskillRecord getBosskillRecord(long userId, int bossId) {
		String value = hget(RedisKey.BOSSKILL_RECORD_PREFIX + userId, "" + bossId);
		BosskillRecord.Builder builder = BosskillRecord.newBuilder();
		if(parseJson(value, builder))
			return builder.build();
		
		return null;
	}
	
	public void setBosskillRecord(long userId, BosskillRecord record) {
		this.hput(RedisKey.BOSSKILL_RECORD_PREFIX + userId, "" + record.getBossId(), formatJson(record));
	}
}
