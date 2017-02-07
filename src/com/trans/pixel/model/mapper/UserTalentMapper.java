package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.userinfo.UserTalentBean;

public interface UserTalentMapper {
	
	public int updateUserTalent(UserTalentBean userTalent);
	
	public List<UserTalentBean> selectUserTalentList(long userId);
	
	public UserTalentBean selectUserTalent(@Param("userId")long userId, @Param("talentId")int talentId);
}
