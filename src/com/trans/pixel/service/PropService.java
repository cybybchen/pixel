package com.trans.pixel.service;

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
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.EquipProto.Chip;
import com.trans.pixel.protoc.EquipProto.Prop;
import com.trans.pixel.protoc.EquipProto.Synthetise;
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
	@Resource
	private EquipService equipService;
	@Resource
	private NoticeMessageService noticeMessageService;
	
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
				if (reward == null) {
					RewardInfo.Builder builder = RewardInfo.newBuilder();
					builder.setItemid(prop.getItemid());
					builder.setCount(1);
					rewardList.addLoot(builder.build());
				} else if(reward != null && reward.getItemid()/10000 == RewardConst.PACKAGE/10000 && reward.getItemid() != 37001 && reward.getItemid() < 36000){
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
	
	
	
	public ResultConst useProp(UserBean user, int propId, int propCount, MultiReward.Builder rewards, int chipId) {
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
		
		if (propId >= 34091 && propId <= 34093) {
			Chip chip = equipService.getChip(chipId);
			if (chip == null)
				return ErrorConst.CHIP_IS_NOT_EXISTS_ERROR;
			
			if (chip.getRare() != prop.getRare())
				return ErrorConst.CHIP_CAN_NOT_GET_ERROR;
			
			RewardInfo.Builder builder = RewardInfo.newBuilder();
			builder.setItemid(chipId);
			builder.setCount(prop.getJudge() * propCount * (user.getVip() >= 9 ? 2 : 1));//Vip特权
			rewards.addLoot(builder.build());
			
			userProp.setPropCount(userProp.getPropCount() - propCount);
			userPropService.updateUserProp(userProp);
			
			return SuccessConst.USE_PROP;
		}
		
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
		if (prop.getWeightall() <= 0)
			return null;
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
		
//		if (syn.getNeedid() >0) {
//			UserEquipPokedeBean needPokede = userEquipPokedeService.selectUserEquipPokede(user.getId(), syn.getNeedid());
//			if (needPokede == null)
//				return ErrorConst.NOT_ENOUGH_PROP;
//		}
		UserEquipPokedeBean equipPokede = userEquipPokedeService.selectUserEquipPokede(user.getId(), syn.getTargetid());
		if (equipPokede != null)
			return ErrorConst.EQUIP_IS_EXIST_ERROR;
		
//		List<RewardInfo> costList = buildCostList(syn);
		if (!costService.canCost(user, syn.getCoverList())) 
			return ErrorConst.NOT_ENOUGH_PROP;
		
		costService.cost(user, syn.getCoverList());
		
		rewards.addLoot(RewardBean.init(syn.getTargetid(), 1).buildRewardInfo());
		costs.addAllLoot(syn.getCoverList());
		
		return SuccessConst.PROP_COMPOSE_SUCCESS;
	}
	
//	private List<RewardInfo> buildCostList(Synthetise syn) {
//		List<RewardInfo> costList = new ArrayList<RewardInfo>();
//		RewardInfo.Builder builder = RewardInfo.newBuilder();
//		builder.setItemid(syn.getId());
//		builder.setCount(1);
//		costList.add(builder.build());
//		for (SynthetiseCover cover : syn.getCoverList()) {
//			builder = RewardInfo.newBuilder();
//			builder.setItemid(cover.getCover());
//			builder.setCount(cover.getCount());
//			costList.add(builder.build());
//		}
//		
//		return costList;
//	}
	
	public MultiReward.Builder handleRewards(UserBean user, List<RewardBean> rewardList) {
		return rewardsHandle(user, RewardBean.buildRewardInfoList(rewardList));
	}
	
	public MultiReward.Builder handleRewards(UserBean user, MultiReward.Builder builder) {
		return rewardsHandle(user, builder.getLootList());
	}
	
	public MultiReward.Builder handleRewards(UserBean user, MultiReward multi) {
		return rewardsHandle(user, multi.getLootList());
	}
	
	public MultiReward.Builder rewardsHandle(UserBean user, List<RewardInfo> rewardList) {
		boolean isShenyuan = false;
		for (RewardInfo reward : rewardList) {//判断是否深渊出装备
			if (reward.getItemid() == 33503 || reward.getItemid() == 33504) {
				isShenyuan = true;
				break;
			}
		}
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
		
		rewardService.mergeReward(rewards);
		if (isShenyuan) {
			noticeMessageService.handlerShenyuanEquipMessage(user, rewards.build());
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
	
	public boolean canMaterialCompose(UserBean user, List<RewardInfo> costList) {
		int count = 0;
		for (RewardInfo cost : costList) {
			count += cost.getCount();
		}
		
		if (count < 1000)
			return false;
		
		if (!costService.canCost(user, costList))
			return false;
		
		return true;
	}
	
	public List<RewardInfo> meterialCompose(UserBean user, List<RewardInfo> costList) {
		costService.cost(user, costList);
		
		return RewardBean.initRewardInfoList(24010, 1);
	}
}
