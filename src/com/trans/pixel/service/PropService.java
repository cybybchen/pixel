package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.PackageBean;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserPropBean;
import com.trans.pixel.service.redis.PropRedisService;

@Service
public class PropService {

	@Resource
	private UserPropService userPropService;
	@Resource
	private PropRedisService propRedisService;
	@Resource
	private RewardService rewardService;
	
	public PackageBean getProp(int itemId) {
		PackageBean prop = propRedisService.getProp(itemId);
		if (prop == null) {
			parseAndSaveEquipConfig();
			prop = propRedisService.getProp(itemId);
		}
		
		return prop;
	}
	
	public RewardBean useProp(UserBean user, int propId) {
		UserPropBean userProp = userPropService.selectUserProp(user.getId(), propId);
		if (userProp == null || userProp.getPropCount() == 0)
			return null;
		
		PackageBean prop = getProp(userProp.getPropId());
		if (prop == null)
			return null;
		
		RewardBean reward = prop.randomReward();
		if (reward != null) {
			rewardService.doReward(user, reward);
			userProp.setPropCount(userProp.getPropCount() - 1);
			userPropService.updateUserProp(userProp);
			
			return reward;
		}
		
		return null;
		
	}
	
	private void parseAndSaveEquipConfig() {
		List<PackageBean> list = PackageBean.xmlParse();
		propRedisService.setPropList(list);;
	}
}
