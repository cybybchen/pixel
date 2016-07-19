package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.userinfo.UserPokedeBean;

public interface UserPokedeMapper {
	
	public UserPokedeBean selectUserPokede(@Param("userId") long userId, @Param("heroId") int heroId);
	
	public int updateUserPokede(UserPokedeBean userPokede);
	
	public List<UserPokedeBean> selectUserPokedeList(long userId);
}
