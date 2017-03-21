package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserPropBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.EquipProto.Prop;
import com.trans.pixel.service.redis.PropRedisService;

@Service
public class PropService {

	@Resource
	private UserPropService userPropService;
	@Resource
	private PropRedisService propRedisService;
	@Resource
	private RewardService rewardService;
	@Resource
	private BossService bossService;
	
	public Prop getProp(int itemId) {
		Prop prop = propRedisService.getPackage(itemId);
		
		return prop;
	}
	
	private void randomReward(List<RewardInfo> rewardList, Prop prop, int propCount){
		for (int i = 0; i < propCount; ++i) {
			for (int j = 0; j < Math.max(prop.getJudge(), 1); ++j) {
				RewardInfo reward = randomReward(prop);
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
		
		Prop prop = getProp(userProp.getPropId());
		if (prop == null)
			return null;
		
		if (prop.getBossid() > 0) {
			bossService.zhaohuanBoss(user, prop.getBossid());
			userProp.setPropCount(userProp.getPropCount() - propCount);
			userPropService.updateUserProp(userProp);
			return null;
		}
		
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
	
	private RewardInfo randomItem(Prop prop) {
		Random rand = new Random();
		int randWeight = rand.nextInt(prop.getWeightall());
		for (int i = 0; i < prop.getItemCount(); ++i) {
			RewardInfo item = prop.getItem(i);
			if (randWeight < item.getWeight())
				return item;
			
			randWeight -= item.getWeight();
		}
		
		return null;
	}
	
	public RewardInfo randomReward(Prop prop) {
		RewardInfo item = randomItem(prop);
		if (item != null) {
			return item;
		}
		
		return null;
	}
}
