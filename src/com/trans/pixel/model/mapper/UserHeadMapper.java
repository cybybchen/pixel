package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.userinfo.UserHeadBean;

public interface UserHeadMapper {
	
	public UserHeadBean selectUserHead(@Param("userId") long userId, @Param("headId") int headId);
	
	public int insertUserHead(UserHeadBean userHead);
	
	public List<UserHeadBean> selectUserHeadList(long userId);
	
}
