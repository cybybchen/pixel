package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.ServerMapper;
import com.trans.pixel.service.redis.ServerRedisService;

@Service
public class ServerService {

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
}
