package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.ServerMapper;
import com.trans.pixel.service.redis.ServerRedisService;
import com.trans.pixel.utils.DateUtil;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class ServerService {
	private static final Logger log = LoggerFactory.getLogger(ServerService.class);
	
	@Resource
	private ServerRedisService serverRedisService;
	@Resource
	private ServerMapper serverMapper;
	
	public List<Integer> getServerIdList() {
		List<Integer> serverIds = serverRedisService.getServerIdList();
		if (serverIds == null || serverIds.size() == 0) {
			serverIds = serverMapper.selectServerIdList();
			if (serverIds != null && serverIds.size() > 0)
				serverRedisService.setServerIdList(serverIds);
		}
		
		return serverIds;
	}
	
	public String getKaifuTime(int serverId) {
		String kaifuTime = serverRedisService.getKaifuTime(serverId);
		if (kaifuTime == null) {
			kaifuTime = serverMapper.selectServerKaifuTime(serverId);
			if (kaifuTime == null) {
				kaifuTime = DateUtil.getCurrentDateString();
				serverMapper.insertServerKaifuTime(serverId, kaifuTime);
			}
			serverRedisService.setKaifuTime(serverId, kaifuTime);
		}
		
		return kaifuTime;
	}
	
	public int getGameVersion() {
		String gameVersion = serverRedisService.getGameVersion();
		if (gameVersion == null) {
			return 0;
		}
		
		return TypeTranslatedUtil.stringToInt(gameVersion);
	}
	
	public int getOnlineStatus(String version) {
		return serverRedisService.getOnlineStatus(version);
	}
	
	public boolean isInKaifuActivityTime(int lastTime, int serverId) {
		if (lastTime <= 0)
			return true;
		
		return getKaifuDays(serverId) < lastTime;
	}
	
	public int getKaifuDays(int serverId) {
		String kaifuTime = getKaifuTime(serverId);
		return DateUtil.intervalDays(DateUtil.getDate(DateUtil.getCurrentDateString()), DateUtil.getDate(kaifuTime));
	}
}
