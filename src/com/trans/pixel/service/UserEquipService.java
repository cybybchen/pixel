package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.mapper.UserEquipMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.service.redis.UserEquipRedisService;

@Service
public class UserEquipService {
	@Resource
	private UserEquipRedisService userEquipRedisService;
	@Resource
	private UserEquipMapper userEquipMapper;
	@Resource
	private UserEquipPokedeService userEquipPokedeService;
	
	public UserEquipBean selectUserEquip(long userId, int equipId) {
		UserEquipBean userEquip = userEquipRedisService.selectUserEquip(userId, equipId);
		if (userEquip == null)
			userEquip = userEquipMapper.selectUserEquip(userId, equipId);
		
		return userEquip;
	}
	
	public void delUserEquip(UserEquipBean userEquip) {
		userEquip.setEquipCount(0);
		userEquipRedisService.updateUserEquip(userEquip);
//		userEquipMapper.updateUserEquip(userEquip);
	}
	
	public void updateUserEquip(UserEquipBean userEquip) {
		userEquipRedisService.updateUserEquip(userEquip);
//		userEquipMapper.updateUserEquip(userEquip);
	}
	
	public void updateToDB(long userId, int equipId) {
		UserEquipBean userEquip = userEquipRedisService.selectUserEquip(userId, equipId);
		if(userEquip != null)
			userEquipMapper.updateUserEquip(userEquip);
	}
	
	public String popDBKey(){
		return userEquipRedisService.popDBKey();
	}
	
	public List<UserEquipBean> selectUserEquipList(long userId) {
		List<UserEquipBean> userEquipList = userEquipRedisService.selectUserEquipList(userId);
		if (userEquipList.size() == 0) {
			userEquipList = userEquipMapper.selectUserEquipList(userId);
			if (userEquipList != null && userEquipList.size() > 0)
				userEquipRedisService.updateUserEquipList(userEquipList, userId);
		}
		
		return userEquipList;
	}
	
	public UserEquipBean useUserEquip(long userId, int equipId, int equipCount) {
		UserEquipBean userEquip = selectUserEquip(userId, equipId);
		if (userEquip == null) {
			userEquip = initUserEquip(userId, equipId);
		}
		
		userEquip.setEquipCount(userEquip.getEquipCount() - equipCount);
		updateUserEquip(userEquip);
		
		return userEquip;
	}
	
	public void addUserEquip(UserBean user, int equipId, int equipCount) {
		if (equipId < RewardConst.CHIP)
			userEquipPokedeService.updateUserEquipPokede(equipId, user);
		else {
			long userId = user.getId();
			UserEquipBean userEquip = selectUserEquip(userId, equipId);
			if (userEquip == null) {
				userEquip = initUserEquip(userId, equipId);
			}
			
			userEquip.setEquipCount(userEquip.getEquipCount() + equipCount);
			updateUserEquip(userEquip);
		}
			
	}
	
	private UserEquipBean initUserEquip(long userId, int equipId) {
		UserEquipBean userEquip = new UserEquipBean();
		userEquip.setUserId(userId);
		userEquip.setEquipId(equipId);
		
		return userEquip;
	}
}
