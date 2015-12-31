package com.trans.pixel.model.mapper;

import com.trans.pixel.model.userinfo.UserLevelLootBean;

public interface UserLevelLootMapper {
	public UserLevelLootBean selectUserLevelLootRecord(long userId);
	
	public int insertUserLevelLootRecord(UserLevelLootBean userLevelLootRecord);
	
	public int updateUserLevelLootRecord(UserLevelLootBean userLevelLootRecord);
}
