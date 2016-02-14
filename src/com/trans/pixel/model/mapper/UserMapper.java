package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.userinfo.UserBean;

public interface UserMapper {
	
	public UserBean queryById(long userId);
	
	public UserBean queryByServerAndAccount(@Param("serverId")int serverId, @Param("account")String account);
	
	public List<UserBean> queryByUnionId(int unionId);
	
	public int addNewUser(UserBean user);
	
	public int updateUser(UserBean user);
}
