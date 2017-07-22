package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface UserRecommandMapper {
	
	public int addRecommand(@Param("userId") long userId, @Param("markId") String markId);
	
	public List<String> getRecommands(long userId);
}
