package com.trans.pixel.model.mapper;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.userinfo.UserActivityBean;

public interface UserActivityMapper {
	
	public UserActivityBean selectUserActivity(@Param("userId") long userId, @Param("activityId") int activityId);
	
	public int updateUserActivity(UserActivityBean userActivity);
	
}
