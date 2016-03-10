package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.userinfo.UserAchieveBean;

public interface UserAchieveMapper {
	
	public UserAchieveBean selectUserAchieve(@Param("userId") long userId, @Param("type") int type);
	
	public int updateUserAchieve(UserAchieveBean userAchieve);
	
	public List<UserAchieveBean> selectUserAchieveList(long userId);
}
