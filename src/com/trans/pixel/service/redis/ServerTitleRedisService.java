package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.ServerBean;
import com.trans.pixel.protoc.ServerProto.ServerTitleInfo.TitleInfo;
import com.trans.pixel.protoc.UserInfoProto.ServerData;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Repository
public class ServerTitleRedisService extends RedisService{
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
			hput(key, "" + (title.getTitleId() + title.getUserId()), RedisService.formatJson(title));
	}
	
	public void deleteServerTitle(int serverId, String titleKey) {
		String key = RedisKey.SERVER_TITLE_PREFIX + serverId;
		this.hdelete(key, titleKey);
	}
	
	public int canRefreshAreaBoss(int serverId){
		String data = get(RedisKey.SERVERDATA+serverId);
		ServerData.Builder serverdata = ServerData.newBuilder();
		if(data != null)
			parseJson(data, serverdata);
		int values[] = {21, 18, 12, 6, 0};
		for(int value : values){
			long time = today(value);
			if(time > serverdata.getAreaBossRefreshTime() && time < System.currentTimeMillis()/1000L){
				if(!setLock(RedisKey.SERVERDATA+serverId))
					return -1;
				serverdata.setAreaBossRefreshTime(time);
				set(RedisKey.SERVERDATA+serverId, formatJson(serverdata.build()));
				return value;
			}
		}
		return -1;
	}
	
	public ServerBean getServer(int serverId) {
		String value = hget(RedisKey.SERVER_KEY, "" + serverId);
		
		return ServerBean.getServer(value);
	}
	
	public String getGameVersion() {
		String value = get(RedisKey.GAME_VERSION_KEY);
		
		return value;
	}
	
	public void setServer(ServerBean server) {
		hput(RedisKey.SERVER_KEY, "" + server.getServerId(), server.toJson());
	}
	
	public int getOnlineStatus(String version) {
		String value = hget(RedisKey.VERSIONCONTROLLER_PREFIX, version);
		return TypeTranslatedUtil.stringToInt(value);
	}
}
