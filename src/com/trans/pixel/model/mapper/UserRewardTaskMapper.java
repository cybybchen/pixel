package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.userinfo.UserRewardTaskBean;

public interface UserRewardTaskMapper {
	
	public int updateUserRewardTask(UserRewardTaskBean userRewardTask);
	
	public List<UserRewardTaskBean> selectUserRewardTaskList(long userId);
	
	public UserRewardTaskBean selectUserRewardTaskList(@Param("userId")long userId, @Param("rewardTaskIndex")int rewardTaskIndex);
}
