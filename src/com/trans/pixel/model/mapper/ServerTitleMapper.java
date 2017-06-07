package com.trans.pixel.model.mapper;

import java.util.List;

import com.trans.pixel.model.ServerTitleBean;

public interface ServerTitleMapper {
	
	public List<ServerTitleBean> selectServerTileListByServerId(int serverId);
	
	public List<ServerTitleBean> selectServerTileList();
	
	public int updateServerTitle(ServerTitleBean serverTitle);
	
	public void deleteServerTileByTitleId(int titleId);
}
