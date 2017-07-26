package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.ServerBean;
import com.trans.pixel.protoc.UserInfoProto.ServerData;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Repository
public class ServerRedisService extends RedisService{
	
	public boolean isInvalidServer(final int serverId) {
		String key = RedisKey.SERVER_KEY;
				
		return sismember(key, "" + serverId);
	}
	
	public List<Integer> getServerIdList() {
		String key = RedisKey.SERVER_KEY;
				
		List<Integer> serverIds = new ArrayList<Integer>();
		for (String serverId : hkeys(key)) {
			serverIds.add(TypeTranslatedUtil.stringToInt(serverId));
		}
		return serverIds;
	}
	
	public void setServerList(final List<ServerBean> serverList) {
		String key = RedisKey.SERVER_KEY;
		for (ServerBean server : serverList)
			hput(key, "" + server.getServerId(), server.toJson());
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
