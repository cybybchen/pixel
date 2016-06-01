package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface ServerMapper {
	
	public List<Integer> selectServerIdList();
	
	public Object selectServerKaifuTime(int serverId);
	
	public Object insertServerKaifuTime(@Param("serverId") int serverId, @Param("kaifuTime") String kaifuTime);
}
