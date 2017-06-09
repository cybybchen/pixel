package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.ServerTitleBean;
import com.trans.pixel.model.mapper.ServerTitleMapper;
import com.trans.pixel.protoc.ServerProto.ServerTitleInfo.TitleInfo;
import com.trans.pixel.service.redis.ServerTitleRedisService;

@Service
public class ServerTitleService {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ServerTitleService.class);
	
	@Resource
	private ServerTitleRedisService redis;
	@Resource
	private ServerTitleMapper mapper;
	
	public List<TitleInfo> selectServerTileListByServerId(int serverId) {
		List<TitleInfo> titleList = redis.selectServerTileListByServerId(serverId);
		if (titleList.isEmpty()) {
			List<ServerTitleBean> serverTitleList = mapper.selectServerTileListByServerId(serverId);
			for (ServerTitleBean serverTitle : serverTitleList) {
				titleList.add(serverTitle.build());
			}
			
			if (!titleList.isEmpty())
				redis.setServerTitleList(serverId, titleList);
		}
		
		return titleList;
	}
	
	public void deleteServerTitleByTitleId(int serverId, int titleId) {
		List<TitleInfo> titleList = selectServerTileListByServerId(serverId);
		for (TitleInfo title : titleList) {
			if (title.getTitleId() == titleId)
				redis.deleteServerTitle(serverId, "" + title.getTitleId() + title.getUserId());
		}
	}
	
	public void updateServerTitleByTitleId(int serverId, int titleId, List<Long> userIds) {
		List<TitleInfo> titleList = new ArrayList<TitleInfo>();
		for (long userId : userIds) {
			ServerTitleBean bean = new ServerTitleBean(serverId, titleId, userId);
			mapper.updateServerTitle(bean);
			titleList.add(bean.build());
		}
		
		redis.setServerTitleList(serverId, titleList);
	}
}
