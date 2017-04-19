package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipPokedeBean;
import com.trans.pixel.model.userinfo.UserPropBean;
import com.trans.pixel.protoc.Base.CostItem;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.EquipProto.Prop;
import com.trans.pixel.protoc.EquipProto.Synthetise;
import com.trans.pixel.protoc.EquipProto.SynthetiseCover;
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
	@Resource
	private RewardTaskService rewardTaskService;
	@Resource
	private UserEquipPokedeService userEquipPokedeService;
	@Resource
	private CostService costService;
	
	public Prop getProp(int itemId) {
		Prop prop = propRedisService.getPackage(itemId);
		
		return prop;
	}
	
	private void randomReward(List<RewardInfo> rewardList, Prop prop, int propCount){
		for (int i = 0; i < propCount; ++i) {
			for (int j = 0; j < Math.max(prop.getJudge(), 1); ++j) {
				RewardInfo reward = randomReward(prop);
				if(reward.getItemid()/10000 == RewardConst.PACKAGE/10000 && reward.getItemid() != 37001){
					randomReward(rewardList, getProp(reward.getItemid()), (int)rewardService.randomRewardCount(reward));
				}else
					rewardService.mergeReward(rewardList, reward);
			}
		}
	}
	
	
	
	public ResultConst useProp(UserBean user, int propId, int propCount, MultiReward.Builder rewards) {
		UserPropBean userProp = userPropService.selectUserProp(user.getId(), propId);
		if (userProp == null || userProp.getPropCount() < propCount)
			return ErrorConst.PROP_USE_ERROR;
		
		Prop prop = getProp(userProp.getPropId());
		if (prop == null)
			return ErrorConst.PROP_USE_ERROR;
		
		if (prop.getBossid() > 0) {
			ResultConst ret = rewardTaskService.zhaohuanTask(user, prop.getBossid());
			if (ret instanceof ErrorConst)
				return ret;
			userProp.setPropCount(userProp.getPropCount() - 1);
			userPropService.updateUserProp(userProp);
			return ret;
		}
		
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		randomReward(rewardList, prop, propCount);
		
		if (!rewardList.isEmpty()) {
			rewards.addAllLoot(rewardList);
			userProp.setPropCount(userProp.getPropCount() - propCount);
			userPropService.updateUserProp(userProp);
		}
		return SuccessConst.USE_PROP;
	}
	
	private RewardInfo randomItem(Prop prop) {
		Random rand = new Random();
		int randWeight = rand.nextInt(prop.getWeightall());
		for (int i = 0; i < prop.getItemcount(); ++i) {
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
	
	public ResultConst synthetiseCompose(UserBean user, int synthetiseId, MultiReward.Builder rewards, MultiReward.Builder costs) {
		Synthetise syn = propRedisService.getSynthetise(synthetiseId);
		if (syn == null)
			return ErrorConst.NOT_ENOUGH_PROP;
		
		if (syn.getNeedid() >0) {
			UserEquipPokedeBean needPokede = userEquipPokedeService.selectUserEquipPokede(user.getId(), syn.getNeedid());
			if (needPokede == null)
				return ErrorConst.NOT_ENOUGH_PROP;
		}
		UserEquipPokedeBean equipPokede = userEquipPokedeService.selectUserEquipPokede(user.getId(), syn.getTarget());
		if (equipPokede != null)
			return ErrorConst.EQUIP_IS_EXIST_ERROR;
		
		List<CostItem> costList = buildCostList(syn);
		if (!costService.canCostAll(user, costList)) 
			return ErrorConst.NOT_ENOUGH_PROP;
		
		costService.costAll(user, costList);
		
		rewards.addLoot(RewardBean.init(syn.getTarget(), 1).buildRewardInfo());
		costs.addAllLoot(RewardBean.buildCostList(costList));
		
		return SuccessConst.PROP_COMPOSE_SUCCESS;
	}
	
	private List<CostItem> buildCostList(Synthetise syn) {
		List<CostItem> costList = new ArrayList<CostItem>();
		CostItem.Builder builder = CostItem.newBuilder();
		builder.setCostid(syn.getId());
		builder.setCostcount(1);
		costList.add(builder.build());
		for (SynthetiseCover cover : syn.getCoverList()) {
			builder = CostItem.newBuilder();
			builder.setCostid(cover.getCover());
			builder.setCostcount(cover.getCount());
			costList.add(builder.build());
		}
		
		return costList;
	}
}
