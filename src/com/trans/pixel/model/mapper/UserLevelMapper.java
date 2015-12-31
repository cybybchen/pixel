package com.trans.pixel.model.mapper;

import com.trans.pixel.model.userinfo.UserLevelBean;

public interface UserLevelMapper {
	public UserLevelBean selectUserLevelRecord(long userId);
	
	public int insertUserLevelRecord(UserLevelBean userLevelRecord);
	
	public int updateUserLevelRecord(UserLevelBean userLevelRecord);
}
