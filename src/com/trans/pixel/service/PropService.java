package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.PackageBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserPropBean;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RewardInfo;
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
	
	private void randomReward(List<RewardInfo> rewardList, PackageBean prop, int propCount){
		for (int i = 0; i < propCount; ++i) {
			for (int j = 0; j < Math.max(prop.getJudge(), 1); ++j) {
				RewardInfo reward = prop.randomReward();
				if(reward.getItemid()/10000 == RewardConst.PACKAGE/10000 && reward.getItemid() != 37001){
					randomReward(rewardList, getProp(reward.getItemid()), (int)reward.getCount());
				}else
					rewardService.mergeReward(rewardList, reward);
			}
		}
	}
	
	public MultiReward useProp(UserBean user, int propId, int propCount) {
		UserPropBean userProp = userPropService.selectUserProp(user.getId(), propId);
		if (userProp == null || userProp.getPropCount() < propCount)
			return null;
		
		PackageBean prop = getProp(userProp.getPropId());
		if (prop == null || prop.getCyb() == 1)
			return null;
		
		MultiReward.Builder multiReward = MultiReward.newBuilder();
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		randomReward(rewardList, prop, propCount);
		
		if (!rewardList.isEmpty()) {
			multiReward.addAllLoot(rewardList);
			userProp.setPropCount(userProp.getPropCount() - propCount);
			userPropService.updateUserProp(userProp);
			
			return multiReward.build();
		}
		return null;
	}
	
	private void parseAndSaveEquipConfig() {
		List<PackageBean> list = PackageBean.xmlParse();
		propRedisService.setPropList(list);;
	}
}
