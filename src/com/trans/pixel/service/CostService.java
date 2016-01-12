package com.trans.pixel.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.userinfo.UserBean;

@Service
public class CostService {

	@Resource
	private UserService userService;
	public boolean cost(UserBean user, int itemId) {
		return false;
	}
	
	public boolean costResult(UserBean user, int type, int cost) {
		int coin = user.getCoin();
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
