package com.trans.pixel.model.mapper;

import com.trans.pixel.model.UserLevelRecordBean;

public interface UserLevelRecordMapper {
	public UserLevelRecordBean selectUserLevelRecord(long userId);
	
	public int insertUserLevelRecord(UserLevelRecordBean userLevelRecord);
	
	public int updateUserLevelRecord(UserLevelRecordBean userLevelRecord);
}
