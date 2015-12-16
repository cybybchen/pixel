package com.trans.pixel.model.mapper;

import com.trans.pixel.model.UserLevelLootRecordBean;

public interface UserLevelLootRecordMapper {
	public UserLevelLootRecordBean selectUserLevelLootRecord(long userId);
	
	public int insertUserLevelLootRecord(UserLevelLootRecordBean userLevelLootRecord);
	
	public int updateUserLevelLootRecord(UserLevelLootRecordBean userLevelLootRecord);
}
