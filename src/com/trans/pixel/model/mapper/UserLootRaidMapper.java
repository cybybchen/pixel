package com.trans.pixel.model.mapper;

import java.util.List;

import com.trans.pixel.model.userinfo.UserLootRaidBean;

public interface UserLootRaidMapper {
	
	public int updateUserLoot(UserLootRaidBean lootRaid);
	
	public List<UserLootRaidBean> selectUserLootList(long userid);
}
