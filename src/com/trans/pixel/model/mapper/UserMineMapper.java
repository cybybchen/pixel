package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.userinfo.UserMineBean;

public interface UserMineMapper {
	
	public UserMineBean selectUserMine(@Param("userId") long userId, @Param("mapId") int mapId, @Param("mineId") int mineId);
	
	public int updateUserMine(UserMineBean userMine);
	
	public List<UserMineBean> selectUserMineList(long userId);
}
