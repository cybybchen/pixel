package com.trans.pixel.model.mapper;

import org.apache.ibatis.annotations.Param;

public interface TeamMapper {
	
	public int updateTeam(@Param("userId")long userId, @Param("cache")String cache);
	
	public String selectTeam(long userId);
}
