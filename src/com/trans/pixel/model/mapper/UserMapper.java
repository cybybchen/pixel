package com.trans.pixel.model.mapper;

import com.trans.pixel.model.userinfo.UserBean;

public interface UserMapper {
	public UserBean queryById(long userId);
	
	public int addNewUser(UserBean user);
	
	public int updateUser(UserBean user);
}
