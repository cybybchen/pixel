package com.trans.pixel.model.mapper;

import com.trans.pixel.model.userinfo.UserBattletowerBean;

public interface UserBattletowerMapper {
	
	public UserBattletowerBean selectUserBattletower(long userId);
	
	public int insertUserBattertower(UserBattletowerBean ubt);
	
}
