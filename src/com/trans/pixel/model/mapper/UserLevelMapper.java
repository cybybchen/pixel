package com.trans.pixel.model.mapper;

import com.trans.pixel.model.userinfo.UserLevelBean;

public interface UserLevelMapper {
	public UserLevelBean getUserLevel(long userId);
	public int updateUserLevel(UserLevelBean userLevelRecord);
}