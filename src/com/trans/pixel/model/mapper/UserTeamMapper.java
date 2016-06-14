package com.trans.pixel.model.mapper;

import java.util.List;

import com.trans.pixel.model.userinfo.UserTeamBean;

public interface UserTeamMapper {
	
//	public int addUserTeam(UserTeamBean userTeam);
//	
//	public int delUserTeam(@Param("userId")long userId, @Param("id")int id);
	
	public int updateUserTeam(UserTeamBean userTeam);
	
	public List<UserTeamBean> selectUserTeamList(long userId);
}
