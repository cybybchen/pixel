package com.trans.pixel.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;

@Service
public class CostService {

	@Resource
	private UserService userService;
	@Resource
	private UserEquipService userEquipService;
	public boolean cost(UserBean user, int itemId, int count) {
		long userId = user.getId();
		if (itemId > RewardConst.HERO) {
			
		} else if (itemId > RewardConst.PROP) {
			
		} else if (itemId > RewardConst.PACKAGE) {
			
		} else if (itemId > RewardConst.CHIP) {
			
		} else if (itemId > RewardConst.EQUIPMENT) {
			UserEquipBean userEquip = userEquipService.selectUserEquip(userId, itemId);
			if (userEquip != null && userEquip.getEquipCount() >= count) {
				userEquip.setEquipCount(userEquip.getEquipCount() - count);
				userEquipService.updateUserEquip(userEquip);
				return true;
			}
		} else {
			switch (itemId) {
				default:
					break;
			}
		}
		return false;
	}
	
	public boolean costResult(UserBean user, int type, int cost) {
		long coin = user.getCoin();
		int jewel = user.getJewel();
		switch (type) {
			case RewardConst.COIN:
				coin -= cost;
				break;
			case RewardConst.JEWEL:
				jewel -= cost;
				break;
			default:
				break;
		}
		
		if (coin < 0 || jewel < 0)
			return false;
		
		user.setCoin(coin);
		user.setJewel(jewel);
		
		userService.updateUser(user);
		return true;
	}
}
