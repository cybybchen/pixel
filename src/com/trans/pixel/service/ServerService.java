package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.ServerBean;
import com.trans.pixel.model.mapper.ServerMapper;
import com.trans.pixel.service.redis.ServerRedisService;
import com.trans.pixel.utils.DateUtil;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class ServerService {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ServerService.class);
	
	@Resource
	private ServerRedisService serverRedisService;
	@Resource
	private ServerMapper serverMapper;
	
	public void setServer(ServerBean server) {
		serverRedisService.setServer(server);
		serverMapper.updateServer(server);
	}
	
	public List<Integer> getServerIdList() {
		List<Integer> serverIds = serverRedisService.getServerIdList();
		if (serverIds == null || serverIds.size() == 0) {
			List<ServerBean> serverList = serverMapper.selectServerList();
			serverIds = new ArrayList<Integer>();
			for (ServerBean server : serverList) {
				serverIds.add(server.getServerId());
			}
			if (serverIds != null && serverIds.size() > 0)
				serverRedisService.setServerList(serverList);
		}
		
		return serverIds;
	}
	
	public ServerBean getServer(int serverId) {
		ServerBean server = serverRedisService.getServer(serverId);
		if (server == null) {
			server = serverMapper.selectServer(serverId);
//			if (kaifuTime == null) {
//				kaifuTime = DateUtil.getCurrentDateString();
//				serverMapper.insertServerKaifuTime(serverId, kaifuTime);
//			}
			if (server != null)
				serverRedisService.setServer(server);
		}
		
		return server;
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
		ServerBean server = getServer(serverId);
		String kaifuTime = server.getKaifuTime();
		return DateUtil.intervalDays(DateUtil.getDate(DateUtil.getCurrentDateString()), DateUtil.getDate(kaifuTime));
	}
}
