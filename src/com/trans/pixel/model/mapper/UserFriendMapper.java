package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.userinfo.UserFriendBean;

public interface UserFriendMapper {
	
	public UserFriendBean selectUserFriend(@Param("userId") long userId, @Param("friendId") long friendId);
	
	public int insertUserFriend(@Param("userId") long userId, @Param("friendId") long friendId);
	
	public List<Long> selectUserFriendIdList(long userId);
	
	public int deleteUserFriend(@Param("userId") long userId, @Param("friendId") long friendId);
}
