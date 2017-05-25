package com.trans.pixel.model.mapper;

import java.util.List;

import com.trans.pixel.model.ServerBean;

public interface ServerMapper {
	
	public List<ServerBean> selectServerList();
	
	public ServerBean selectServer(int serverId);
	
	public int updateServer(ServerBean server);
}
