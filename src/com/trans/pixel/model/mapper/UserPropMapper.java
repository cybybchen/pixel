package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.userinfo.UserPropBean;

public interface UserPropMapper {
	
	public UserPropBean selectUserProp(@Param("userId") long userId, @Param("propId") int propId);
	
	public int updateUserProp(UserPropBean userProp);
	
	public List<UserPropBean> selectUserPropList(long userId);
}
