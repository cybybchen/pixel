package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.Commands.ServerData;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Repository
public class ServerRedisService extends RedisService{
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public boolean isInvalidServer(final int serverId) {
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundSetOperations<String, String> bsOps = redisTemplate
						.boundSetOps(RedisKey.PREFIX + RedisKey.SERVER_KEY);
				
				return bsOps.isMember(serverId);
			}
		});
	}
	
	public List<Integer> getServerIdList() {
		return redisTemplate.execute(new RedisCallback<List<Integer>>() {
			@Override
			public List<Integer> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundSetOperations<String, String> bsOps = redisTemplate
						.boundSetOps(RedisKey.PREFIX + RedisKey.SERVER_KEY);
				
				List<Integer> serverIds = new ArrayList<Integer>();
				Set<String> serverIdSet = bsOps.members();
				for (String serverId : serverIdSet) {
					serverIds.add(TypeTranslatedUtil.stringToInt(serverId));
				}
				return serverIds;
			}
		});
	}
	
	public void setServerId(final Integer serverId) {
		String key = RedisKey.PREFIX + RedisKey.SERVER_KEY;
		this.sadd(key, "" + serverId);
	}
	
	public void setServerIdList(final List<Integer> serverIds) {
		String key = RedisKey.PREFIX + RedisKey.SERVER_KEY;
		for (int serverId : serverIds)
			this.sadd(key, "" + serverId);
	}
	
	public int canRefreshAreaBoss(int serverId){
		String data = get(RedisKey.SERVERDATA+serverId);
		ServerData.Builder serverdata = ServerData.newBuilder();
		if(data != null)
			parseJson(data, serverdata);
		int values[] = {21, 18, 12, 0};
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
	
	public String getKaifuTime(int serverId) {
		String value = hget(RedisKey.SERVER_KAIFU_TIME, "" + serverId);
		
		return value;
	}
	
	public String getGameVersion() {
		String value = get(RedisKey.GAME_VERSION_KEY);
		
		return value;
	}
	
	public void setKaifuTime(int serverId, String kaifuTime) {
		hput(RedisKey.SERVER_KAIFU_TIME, "" + serverId, kaifuTime);
	}
	
	public int getOnlineStatus(String version) {
		String value = hget(RedisKey.VERSIONCONTROLLER_PREFIX, version);
		return TypeTranslatedUtil.stringToInt(value);
	}
}
