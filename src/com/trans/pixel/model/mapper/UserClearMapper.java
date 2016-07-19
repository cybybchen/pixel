package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.userinfo.UserClearBean;

public interface UserClearMapper {
	
	public List<UserClearBean> selectUserClear(@Param("userId") long userId, @Param("heroId") int heroId);
	
	public int updateUserClear(UserClearBean userClear);
	
	public List<UserClearBean> selectUserClearList(long userId);
}
