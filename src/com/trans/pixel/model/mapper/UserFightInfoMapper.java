package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.userinfo.UserFightInfoBean;

public interface UserFightInfoMapper {
	
	public int saveFightInfo(UserFightInfoBean userFightInfo);
	
	public List<UserFightInfoBean> getFightInfos(long userId);
	
	public int removeFightInfo(@Param("userId") long userId, @Param("fightinfoId") int fightinfoId);
}
