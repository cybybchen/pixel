package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
//	private static final Logger log = LoggerFactory.getLogger(PropService.class);
	
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
	@Resource
	private EquipPokedeService equipPokedeService;
	
	public Prop getProp(int itemId) {
		Prop prop = propRedisService.getPackage(itemId);
		
		return prop;
	}
	
	private void randomReward(MultiReward.Builder rewardList, int propId, int propCount, Map<String, Prop> map){
		//TODO
		Prop prop = map.get("" + propId);
		for (int i = 0; i < propCount; ++i) {
			for (int j = 0; j < Math.max(prop.getJudge(), 1); ++j) {
				RewardInfo reward = randomReward(prop);
				if(reward != null && reward.getItemid()/10000 == RewardConst.PACKAGE/10000 && reward.getItemid() != 37001 && reward.getItemid() < 36000){
					randomReward(rewardList, reward.getItemid(), (int)reward.getCount(), map);
				}else {
					RewardInfo.Builder builder = RewardInfo.newBuilder(reward);
					builder.clearCounta();
					builder.clearCountb();
					builder.clearWeight();
					rewardList.addLoot(builder.build());
				}
			}
		}
		rewardService.mergeReward(rewardList);
	}
	
	
	
	public ResultConst useProp(UserBean user, int propId, int propCount, MultiReward.Builder rewards) {
		UserPropBean userProp = userPropService.selectUserProp(user.getId(), propId);
		if (userProp == null || userProp.getPropCount() < propCount)
			return ErrorConst.PROP_USE_ERROR;
		
		Map<String, Prop> map = propRedisService.getPackageConfig();
		Prop prop = map.get("" + propId);
		if (prop == null)
			return ErrorConst.PROP_USE_ERROR;
		
//		if (prop.getBossid() > 0) {
//			ResultConst ret = rewardTaskService.zhaohuanTask(user, prop.getBossid());
//			if (ret instanceof ErrorConst)
//				return ret;
//			userProp.setPropCount(userProp.getPropCount() - 1);
//			userPropService.updateUserProp(userProp);
//			return ret;
//		}
		
		
		MultiReward.Builder rewardList = MultiReward.newBuilder();
		randomReward(rewardList, propId, propCount, map);
		
		if (rewardList.getLootCount() > 0) {
			rewards.addAllLoot(rewardList.getLootList());
			userProp.setPropCount(userProp.getPropCount() - propCount);
			userPropService.updateUserProp(userProp);
		}
		return SuccessConst.USE_PROP;
	}
	
	private RewardInfo randomItem(Prop prop) {
		Random rand = new Random();
		int randWeight = rand.nextInt(prop.getWeightall());
		for (int i = 0; i < prop.getRewardCount(); ++i) {
			RewardInfo item = prop.getReward(i);
			if (randWeight < item.getWeight()) {
				RewardInfo.Builder builder = RewardInfo.newBuilder(item);
				builder.setCount(rewardService.randomRewardCount(item));
				return builder.build();
			}
			
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
	
	public MultiReward.Builder handleRewards(List<RewardBean> rewardList) {
		return rewardsHandle(RewardBean.buildRewardInfoList(rewardList));
	}
	
	public MultiReward.Builder handleRewards(MultiReward.Builder builder) {
		return rewardsHandle(builder.getLootList());
	}
	
	public MultiReward.Builder handleRewards(MultiReward multi) {
		return rewardsHandle(multi.getLootList());
	}
	
	private MultiReward.Builder rewardsHandle(List<RewardInfo> rewardList) {
		MultiReward.Builder rewards = MultiReward.newBuilder();
//		List<RewardInfo> rewards = new ArrayList<RewardInfo>();
		Map<String, Prop> map = propRedisService.getPackageConfig();
		for (RewardInfo reward : rewardList) {
			Prop prop = map.get("" + reward.getItemid());
			if (prop == null || prop.getAutoopen() == 0) {
				rewards.addLoot(reward);
				continue;
			}
			
			randomReward(rewards, reward.getItemid(), (int)reward.getCount(), map);
		}
		return rewards;
	}
	
	public ResultConst canUseProp(UserBean user, int propId) {
		UserPropBean userProp = userPropService.selectUserProp(user.getId(), propId);
		if (userProp == null || userProp.getPropCount() < 1) {
			return ErrorConst.PROP_USE_ERROR;
		}
		
		return SuccessConst.USE_PROP;
	}
	
	public boolean canMaterialCompose(UserBean user, List<CostItem> costList) {
		int count = 0;
		for (CostItem cost : costList) {
			count += cost.getCostcount();
		}
		
		if (count < 1000)
			return false;
		
		if (!costService.canCostAll(user, costList))
			return false;
		
		return true;
	}
	
	public List<RewardInfo> meterialCompose(UserBean user, List<CostItem> costList) {
		costService.costAll(user, costList);
		
		return RewardBean.initRewardInfoList(24010, 1);
	}
}
