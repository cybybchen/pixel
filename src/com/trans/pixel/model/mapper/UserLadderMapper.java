package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.userinfo.UserLadderBean;

public interface UserLadderMapper {
	
	public UserLadderBean selectUserLadder(@Param("userId") long userId, @Param("type") int type);
	
	public int updateUserLadder(UserLadderBean userLadder);
	
	public List<UserLadderBean> selectUserLadderList(long userId);
}
