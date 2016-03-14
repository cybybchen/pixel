package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.userinfo.UserAreaPropBean;

public interface UserAreaPropMapper {
	
	public UserAreaPropBean selectUserAreaProp(int primaryid);
	
	public int updateUserAreaProp(UserAreaPropBean userAreaProp);
	
	public int addUserAreaProp(@Param("userId")long userId, @Param("id")int id, @Param("count")int count);
	
	public List<UserAreaPropBean> selectUserAreaPropList(long userId);
}
