package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface UserFriendMapper {
	
	public int insertUserFriend(@Param("userId") long userId, @Param("friendId") long friendId);
	
	public List<Long> selectUserFriendIdList(long userId);
	
	public int deleteUserFriend(@Param("userId") long userId, @Param("friendId") long friendId);
}
