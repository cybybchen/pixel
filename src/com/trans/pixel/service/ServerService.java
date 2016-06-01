package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.ServerMapper;
import com.trans.pixel.service.redis.ServerRedisService;
import com.trans.pixel.utils.DateUtil;

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
			Object value = serverMapper.selectServerKaifuTime(serverId);
			log.debug("value is:" + value);
			try {
				if (value != null)
					kaifuTime = (String)value;
			} catch (Exception e) {
				
			}
			if (kaifuTime == null) {
				kaifuTime = DateUtil.getCurrentDateString();
				serverRedisService.setKaifuTime(serverId, kaifuTime);
				serverMapper.insertServerKaifuTime(serverId, kaifuTime);
			} 
		}
		
		return kaifuTime;
	}
	
	public int getOnlineStatus(String version) {
		return serverRedisService.getOnlineStatus(version);
	}
}
