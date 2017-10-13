package com.trans.pixel.model.mapper;

import com.trans.pixel.model.userinfo.UserDDBean;

public interface UserDDMapper {
	
	public UserDDBean queryById(long userId);
	
	public int updateUserDD(UserDDBean userdd);
}
