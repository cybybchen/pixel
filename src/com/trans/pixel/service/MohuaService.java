package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.protoc.Commands.MohuaCard;
import com.trans.pixel.protoc.Commands.MohuaCardMap;
import com.trans.pixel.protoc.Commands.MohuaCardSkill;
import com.trans.pixel.protoc.Commands.MohuaItem;
import com.trans.pixel.protoc.Commands.MohuaMapStage;
import com.trans.pixel.protoc.Commands.MohuaMapStageList;
import com.trans.pixel.protoc.Commands.MohuaUserData;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.service.redis.MohuaRedisService;
import com.trans.pixel.service.redis.UserRedisService;

@Service
public class MohuaService {

	@Resource
	private MohuaRedisService redis;
	@Resource
	private UserTeamService userTeamService;
	@Resource
	private UserRedisService userRedisService;
	
	public MohuaUserData.Builder enterUserData(long userId) {
		MohuaUserData user = redis.getMohuaUserData(userId);
		if (user != null)
			return MohuaUserData.newBuilder(user);
		else 
			return null;
	}
	
	public void delUserData(long userId) {
		redis.delMohuaUserData(userId);
	}
	
	public MohuaUserData.Builder getUserData(long userId) {
		return getUserData(userId, 0);
	}
	
	public MohuaUserData.Builder getUserData(long userId, int serverId) {
		MohuaUserData user = redis.getMohuaUserData(userId);
		if (user == null) {
			MohuaMapStageList mohuaMap = randomMohuaMap();
			user = initMohuaUserData(mohuaMap, userId, serverId);
			redis.updateMohuaUserData(user, userId);
		}
		
		return MohuaUserData.newBuilder(user);
	}
	
	public ResultConst useMohuaCard(long userId, List<MohuaCard> useCardList) {
		MohuaUserData.Builder user = getUserData(userId);
		if (canUseMohuaCard(user.getCardList(), useCardList)) {
			useMohuaCard(user.getCardList(), useCardList);
			redis.updateMohuaUserData(user.build(), userId);
			
			return SuccessConst.MOHUACARD_USE_SUCCESS;
		}
		
		return ErrorConst.MOHUACARD_USE_ERROR;
	}
	
	public List<RewardInfo> stageReward(long userId, int stage) {
		MohuaUserData.Builder user = getUserData(userId);
		List<Integer> rewardStageList = user.getRewardStageList();
		if (rewardStageList.contains(stage))
			return null;
		
		if (user.getStage() < stage)
			return null;
		
		user.addRewardStage(stage);
		List<MohuaItem> itemList = redis.getMohuaJieduanMap(user.getMapid()).getStage(stage).getItemList();
		
		return buildRewardList(itemList);
	}
	
	public List<RewardInfo> hpReward(long userId, int hp) {
		MohuaUserData.Builder user = getUserData(userId);
		List<Integer> rewardHpList = user.getRewardHpList();
		if (rewardHpList.contains(hp))
			return null;
		
		if (user.getConsumehp() < hp)
			return null;
		
		user.addRewardHp(hp);
		List<MohuaItem> itemList = redis.getMohuaLootMap(user.getMapid()).getHp(hp).getCarduseList();
		
		return buildRewardList(itemList);
	}
	
	public ResultConst submitStage(long userId, int hp, int selfhp) {
		MohuaUserData.Builder user = getUserData(userId);
		
		if (user.getConsumehp() == 100 || hp < user.getConsumehp())
			return ErrorConst.MOHUA_HAS_FINISH_ERROR;
		
		user.setStage(user.getStage() + 1);
		user.setConsumehp(hp);
		if (user.getConsumehp() == 100)
			user.setStage(11);
		
		redis.updateMohuaUserData(user.build(), userId);
		
		return SuccessConst.MOHUA_SUBMIT_SUCCESS;
	}
	
	private List<RewardInfo> buildRewardList(List<MohuaItem> itemList) {
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		for (MohuaItem item : itemList) {
			RewardInfo.Builder reward = RewardInfo.newBuilder();
			reward.setItemid(item.getItem());
			reward.setCount(item.getItemcount());
			rewardList.add(reward.build());
		}
		
		return rewardList;
	}
	
	private boolean canUseMohuaCard(List<MohuaCard> userCardList, List<MohuaCard> useCardList) {
		boolean hasEnoughCard = false;
		for (MohuaCard useCard : useCardList) {
			hasEnoughCard = false;
			for (MohuaCard userCard : userCardList) {
				if (userCard.getCardId() == useCard.getCardId()) {
					if (userCard.getCardCount() >= useCard.getCardCount())
						hasEnoughCard = true;
					
					break;
				}
			}
			
			if (!hasEnoughCard)
				break;
		}
		
		return hasEnoughCard;
	}
	
	private void useMohuaCard(List<MohuaCard> userCardList, List<MohuaCard> useCardList) {
		for (MohuaCard useCard : useCardList) {
			for (MohuaCard userCard : userCardList) {
				if (userCard.getCardId() == useCard.getCardId()) {
					if (userCard.getCardCount() >= useCard.getCardCount()) {
						MohuaCard.Builder nMohuaCard = MohuaCard.newBuilder();
						nMohuaCard.setCardId(userCard.getCardId());
						nMohuaCard.setCardCount(userCard.getCardCount() - useCard.getCardCount());
						userCard = nMohuaCard.build();
					}
				}
			}
		}
	}
	
	private MohuaMapStageList randomMohuaMap() {
		List<MohuaMapStageList> mohuaMapList = redis.getMohuaMapList();
		int index = RandomUtils.nextInt(mohuaMapList.size());
		
		return mohuaMapList.get(index);
	}
	
	private List<MohuaCard> randomMohuaCard(int mapid, int randomcard) {
		MohuaCardMap mohuaCardMap = redis.getMohuaCardMap(mapid);
		List<MohuaCardSkill> mohuaCardSkillList = mohuaCardMap.getSkillList();
		
		return randomUserMohuaCard(mohuaCardSkillList, randomcard);
	}
	
	private List<MohuaCard> randomUserMohuaCard(List<MohuaCardSkill> mohuaCardSkillList, int randomcard) {
		List<MohuaCard> userCardList = new ArrayList<MohuaCard>();
		while (userCardList.size() < randomcard) {
			MohuaCard mohuaCard = randomMohuaCard(mohuaCardSkillList);
			userCardList.add(mohuaCard);
		}
		
		return userCardList;
	}
	
	private MohuaCard randomMohuaCard(List<MohuaCardSkill> mohuaCardSkillList) {
		int totalWeight = 0;
		for (MohuaCardSkill mohuaCardSkill : mohuaCardSkillList) {
			totalWeight += mohuaCardSkill.getWeight();
		}
		
		int randomWeight = RandomUtils.nextInt(totalWeight);
		
		for (MohuaCardSkill mohuaCardSkill : mohuaCardSkillList) {
			if (mohuaCardSkill.getWeight() > randomWeight)
				return buildMohuaCard(mohuaCardSkill);
			
			randomWeight -= mohuaCardSkill.getWeight();
		}
		
		return null;
	}
	
	private MohuaCard buildMohuaCard(MohuaCardSkill mohuaCardSkill) {
		MohuaCard.Builder mohuaCard = MohuaCard.newBuilder();
		mohuaCard.setCardId(mohuaCardSkill.getCard());
		mohuaCard.setCardCount(1);
		
		return mohuaCard.build();
	}
	
	private MohuaUserData initMohuaUserData(MohuaMapStageList mohuaMap, long userId, int serverId) {
		MohuaUserData.Builder userData = MohuaUserData.newBuilder();
		userData.setConsumehp(0);
		userData.setMapid(mohuaMap.getMapid());
		userData.setStage(1);
		userData.setSelfhp(0);
		
		List<MohuaMapStage> stageList = mohuaMap.getStageList();
		MohuaMapStage stage = getStage(stageList, userData.getStage());
		
		userData.setCrystal(stage.getCrystal());
		userData.addAllCard(randomMohuaCard(mohuaMap.getMapid(), stage.getRandomcard()));
		userData.addAllHero(userTeamService.getProtoTeamCache(userId));
		
		UserInfo enemyUser = userRedisService.getRandUser(serverId);
		userData.setEnemyName(enemyUser.getName());
		userData.addAllEnemyHero(userTeamService.getProtoTeamCache(enemyUser.getId()));
		
		return userData.build();
	}
	
	private MohuaMapStage getStage(List<MohuaMapStage> stageList, int stage) {
		for (MohuaMapStage mapStage : stageList) {
			if (mapStage.getStage() == stage) {
				return mapStage;
			}
		}	
		
		return null;
	}
}
