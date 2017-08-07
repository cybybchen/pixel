package com.trans.pixel.model.mapper;

import java.util.List;

import com.trans.pixel.model.userinfo.UserLootRewardTaskBean;

public interface UserLootRewardTaskMapper {
	
	public int updateUserLoot(UserLootRewardTaskBean userLoot);
	
	public List<UserLootRewardTaskBean> selectUserLootList(long userId);
}
