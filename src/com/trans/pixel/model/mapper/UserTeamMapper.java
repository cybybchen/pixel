package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.UserTeamBean;

public interface UserTeamMapper {
	
	public UserTeamBean selectUserTeam(@Param("userId") long userId, @Param("mode") int mode);
	
	public int updateUserTeam(UserTeamBean userTeam);
	
	public List<UserTeamBean> selectUserTeamList(long userId);
}
