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
import com.trans.pixel.protoc.Commands.MohuaMapStage;
import com.trans.pixel.protoc.Commands.MohuaMapStageList;
import com.trans.pixel.protoc.Commands.MohuaUserData;
import com.trans.pixel.service.redis.MohuaRedisService;

@Service
public class MohuaService {

	@Resource
	private MohuaRedisService redis;
	
	public MohuaUserData getUserData(long userId) {
		MohuaUserData user = redis.getMohuaUserData(userId);
		if (user == null) {
			user = resetUserData(userId);
		}
		
		return user;
	}
	
	public MohuaUserData resetUserData(long userId) {
		MohuaMapStageList mohuaMap = randomMohuaMap();
		MohuaUserData user = initMohuaUserData(mohuaMap);
		redis.updateMohuaUserData(user, userId);
		
		return user;
	}
	
	public ResultConst useMohuaCard(long userId, List<MohuaCard> useCardList) {
		MohuaUserData user = getUserData(userId);
		if (canUseMohuaCard(user.getCardList(), useCardList)) {
			useMohuaCard(user.getCardList(), useCardList);
			redis.updateMohuaUserData(user, userId);
			
			return SuccessConst.MOHUACARD_USE_SUCCESS;
		}
		
		return ErrorConst.MOHUACARD_USE_ERROR;
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
	
	private MohuaUserData initMohuaUserData(MohuaMapStageList mohuaMap) {
		MohuaUserData.Builder userData = MohuaUserData.newBuilder();
		userData.setConsumehp(0);
		userData.setMapid(mohuaMap.getMapid());
		userData.setRewardHp(0);
		userData.setRewardStage(0);
		userData.setStage(1);
		
		List<MohuaMapStage> stageList = mohuaMap.getStageList();
		MohuaMapStage stage = getStage(stageList, userData.getStage());
		
		userData.setCrystal(stage.getCrystal());
		userData.addAllCard(randomMohuaCard(mohuaMap.getMapid(), stage.getRandomcard()));
		
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
