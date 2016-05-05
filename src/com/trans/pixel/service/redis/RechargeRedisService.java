package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.Commands.Rmb;
import com.trans.pixel.protoc.Commands.RmbList;

@Service
public class RechargeRedisService extends RedisService {
	private static Logger logger = Logger.getLogger(RechargeRedisService.class);
	private static final String RMB_FILE_NAME = "lol_rmb.xml";
	
	public Rmb getRmb(int id) {
		String value = hget(RedisKey.RMB_KEY, "" + id);
		if (value == null) {
			Map<String, Rmb> rmbConfig = getRmbConfig();
			return rmbConfig.get("" + id);
		} else {
			Rmb.Builder builder = Rmb.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Rmb> getRmbConfig() {
		Map<String, String> keyvalue = hget(RedisKey.RMB_KEY);
		if(keyvalue.isEmpty()){
			Map<String, Rmb> map = buildRmbConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Rmb> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.RMB_KEY, redismap);
			return map;
		}else{
			Map<String, Rmb> map = new HashMap<String, Rmb>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Rmb.Builder builder = Rmb.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Rmb> buildRmbConfig(){
		String xml = ReadConfig(RMB_FILE_NAME);
		RmbList.Builder builder = RmbList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + RMB_FILE_NAME);
			return null;
		}
		
		Map<String, Rmb> map = new HashMap<String, Rmb>();
		for(Rmb.Builder rmb : builder.getRmbBuilderList()){
			map.put("" + rmb.getId(), rmb.build());
		}
		return map;
	}
}
