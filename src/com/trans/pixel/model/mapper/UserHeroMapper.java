package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.userinfo.UserHeroBean;

public interface UserHeroMapper {
	
	public UserHeroBean selectUserHero(@Param("userId") long userId, @Param("heroId") int heroId);
	
	public int updateUserHero(UserHeroBean userHero);
	
	public List<UserHeroBean> selectUserHeroList(long userId);
}
