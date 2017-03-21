package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.HeroInfoBean;

public interface UserHeroMapper {
	
	public HeroInfoBean selectUserHero(@Param("userId") long userId, @Param("infoId") int infoId);
	
	public HeroInfoBean selectUserHeroByHeroId(@Param("userId") long userId, @Param("heroId") int heroId);
	
	public int updateUserHero(HeroInfoBean heroInfo);
	
	public int addUserHero(HeroInfoBean heroInfo);
	
	public List<HeroInfoBean> selectUserHeroList(long userId);
	
	public int deleteUserHero(@Param("userId") long userId, @Param("infoId") int infoId);
}
