package com.trans.pixel.model.mapper;

import java.util.List;

import com.trans.pixel.model.userinfo.UserBean;

public interface UserMapper {
	
	public UserBean queryById(long userId);
	
	public List<UserBean> queryByUnionId(int unionId);
	
	public int addNewUser(UserBean user);
	
	public int updateUser(UserBean user);
}
