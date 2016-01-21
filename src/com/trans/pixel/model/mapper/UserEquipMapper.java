package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.userinfo.UserEquipBean;

public interface UserEquipMapper {
	
	public UserEquipBean selectUserEquip(@Param("userId") long userId, @Param("equipId") int equipId);
	
	public int updateUserEquip(UserEquipBean userEquip);
	
	public List<UserEquipBean> selectUserEquipList(long userId);
}
