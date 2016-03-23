package com.trans.pixel.model.mapper;

import java.util.List;

import com.trans.pixel.model.userinfo.UserAreaPropBean;

public interface UserAreaPropMapper {
	
	public UserAreaPropBean selectUserAreaProp(int primaryid);
	
	public int updateUserAreaProp(UserAreaPropBean userAreaProp);
	
	public int addUserAreaProp(UserAreaPropBean userAreaProp);
	
	public List<UserAreaPropBean> selectUserAreaPropList(long userId);
}
