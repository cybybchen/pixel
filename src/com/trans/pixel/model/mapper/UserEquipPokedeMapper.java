package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.userinfo.UserEquipPokedeBean;

public interface UserEquipPokedeMapper {
	
	public UserEquipPokedeBean selectUserEquipPokede(@Param("userId") long userId, @Param("itemId") int itemId);
	
	public int updateUserEquipPokede(UserEquipPokedeBean userPokede);
	
	public List<UserEquipPokedeBean> selectUserEquipPokedeList(long userId);
}
