package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.userinfo.UserFoodBean;

public interface UserFoodMapper {
	
	public UserFoodBean selectUserFood(@Param("userId") long userId, @Param("foodId") int foodId);
	
	public int updateUserFood(UserFoodBean userFood);
	
	public List<UserFoodBean> selectUserFoodList(long userId);
}
