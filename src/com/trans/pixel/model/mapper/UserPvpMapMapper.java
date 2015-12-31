package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.userinfo.UserPvpMapBean;

public interface UserPvpMapMapper {
	
	public UserPvpMapBean selectUserPvpMap(@Param("userId") long userId, @Param("mapId") int mapId);
	
	public int updateUserPvpMap(UserPvpMapBean userPvpMap);
	
	public List<UserPvpMapBean> selectUserPvpMapList(long userId);
}
