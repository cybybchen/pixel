package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface UserRecommandMapper {
	
	public int addRecommand(@Param("userId") long userId, @Param("userId2") long userId2);
	
	public List<Long> getRecommands(long userId);
}
