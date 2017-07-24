package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.ServerProto.ServerTitleInfo.TitleInfo;
import com.trans.pixel.protoc.ServerProto.Title;
import com.trans.pixel.protoc.ServerProto.TitleList;
import com.trans.pixel.service.cache.CacheService;

@Repository
public class ServerTitleRedisService extends RedisService {
	private static Logger logger = Logger.getLogger(ServerTitleRedisService.class);
	private static final String TITLE_FILE_NAME = "ld_title.xml";
	
	public ServerTitleRedisService() {
		buildTitleConfig();
	}
	
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
		Map<Integer, Title> map = CacheService.hgetcache(RedisKey.TITLE_KEY);
		return map.get(id);
	}

	public Map<Integer, Title> getTitleConfig() {
		Map<Integer, Title> map = CacheService.hgetcache(RedisKey.TITLE_KEY);
		return map;
	}
	
	private Map<Integer, Title> buildTitleConfig(){
		String xml = ReadConfig(TITLE_FILE_NAME);
		TitleList.Builder builder = TitleList.newBuilder();
		parseXml(xml, builder);
		Map<Integer, Title> map = new HashMap<Integer, Title>();
		for(Title.Builder title : builder.getDataBuilderList()){
			map.put(title.getId(), title.build());
		}
		CacheService.hputcacheAll(RedisKey.TITLE_KEY, map);
		
		return map;
	}
}
