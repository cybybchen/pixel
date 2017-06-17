package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.ServerProto.ServerTitleInfo.TitleInfo;
import com.trans.pixel.protoc.ServerProto.Title;
import com.trans.pixel.protoc.ServerProto.TitleList;

@Repository
public class ServerTitleRedisService extends RedisService {
	private static Logger logger = Logger.getLogger(ServerTitleRedisService.class);
	private static final String TITLE_FILE_NAME = "ld_title.xml";
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public List<TitleInfo> selectServerTileListByServerId(int serverId) {
		String key = RedisKey.SERVER_TITLE_PREFIX + serverId;
		List<TitleInfo> titleList = new ArrayList<TitleInfo>();
		Map<String, String> titles = hget(key);
		for (String title : titles.values()) {
			TitleInfo.Builder builder = TitleInfo.newBuilder();
			if (RedisService.parseJson(title, builder))
				titleList.add(builder.build());
		}
		
		return titleList;
	}
	
	public void setServerTitleList(int serverId, List<TitleInfo> titleList) {
		String key = RedisKey.SERVER_TITLE_PREFIX + serverId;
		for (TitleInfo title : titleList)
			hput(key, "" + title.getTitleId() + title.getUserId(), RedisService.formatJson(title));
	}
	
	public void deleteServerTitle(int serverId, String titleKey) {
		String key = RedisKey.SERVER_TITLE_PREFIX + serverId;
		this.hdelete(key, titleKey);
	}
	
	public Title getTitle(int id) {
		String value = hget(RedisKey.TITLE_KEY, "" + id);
		if (value == null) {
			Map<String, Title> config = getTitleConfig();
			return config.get("" + id);
		} else {
			Title.Builder builder = Title.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, Title> getTitleConfig() {
		Map<String, String> keyvalue = hget(RedisKey.TITLE_KEY);
		if(keyvalue.isEmpty()){
			Map<String, Title> map = buildTitleConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, Title> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.TITLE_KEY, redismap);
			return map;
		}else{
			Map<String, Title> map = new HashMap<String, Title>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Title.Builder builder = Title.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, Title> buildTitleConfig(){
		String xml = ReadConfig(TITLE_FILE_NAME);
		TitleList.Builder builder = TitleList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + TITLE_FILE_NAME);
			return null;
		}
		
		Map<String, Title> map = new HashMap<String, Title>();
		for(Title.Builder title : builder.getIdBuilderList()){
			map.put("" + title.getId(), title.build());
		}
		return map;
	}
}
