package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.EquipmentBean;
import com.trans.pixel.model.FenjieLevelBean;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.protoc.Commands.Chip;
import com.trans.pixel.protoc.Commands.Item;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.service.redis.EquipRedisService;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class EquipService {

	@Resource
	private UserEquipService userEquipService;
	@Resource
	private EquipRedisService equipRedisService;
	@Resource
	private FenjieService fenjieService;
	@Resource
	private RewardService rewardService;
	
	public EquipmentBean getEquip(int itemId) {
		EquipmentBean equip = equipRedisService.getEquip(itemId);
		if (equip == null) {
			parseAndSaveEquipConfig();
			equip = equipRedisService.getEquip(itemId);
		}
		
		return equip;
	}
	
	public int calHeroEquipLevel(HeroInfoBean heroInfo) {
		String[] equipIds = heroInfo.equipIds();
		int level = 0;
		for (String equipId : equipIds) {
			EquipmentBean equip = getEquip(TypeTranslatedUtil.stringToInt(equipId));
			if (equip != null)
				level += equip.getLevel();
		}
		
		return level;
	}
	
	public int equipCompose(UserBean user, int levelUpId, int count) {
		int composeEquipId = 0;;
		EquipmentBean equip = getEquip(levelUpId);
		if (equip != null) {//合成装备
			boolean equipLevelUpRet = equipLevelUp(user.getId(), equip);
			if (equipLevelUpRet) {
				userEquipService.addUserEquip(user.getId(), equip.getItemid(), 1);
				composeEquipId = levelUpId;
			}
		} else { //合成碎片
			Chip chip = equipRedisService.getChip(levelUpId);
			if (chip != null && chip.getAim() > 0) {
				UserEquipBean userEquip = userEquipService.selectUserEquip(user.getId(), levelUpId);
				if (userEquip != null && userEquip.getEquipCount() >= chip.getCount() * count) {
					userEquip.setEquipCount(userEquip.getEquipCount() - chip.getCount() * count);
					userEquipService.updateUserEquip(userEquip);
					userEquipService.addUserEquip(user.getId(), chip.getAim(), count);
					composeEquipId = chip.getAim();
				}
			}
				
		}
			
		return composeEquipId;
	}
	
	public List<RewardBean> fenjieUserEquip(UserBean user, int equipId, int fenjieCount) {
		UserEquipBean userEquip = userEquipService.selectUserEquip(user.getId(), equipId);
		if (userEquip.getEquipCount() < fenjieCount)
			return null;
		
		EquipmentBean equip = getEquip(equipId);
		if (equip == null)
			return null;
		
		FenjieLevelBean fenjie = fenjieService.getFenjie(equip.getLevel());
		if (fenjie == null)
			return null;
		
		userEquip.setEquipCount(userEquip.getEquipCount() - fenjieCount);
		userEquipService.updateUserEquip(userEquip);
		
		return fenjie.randomReward(fenjieCount);
	}
	
	public List<RewardBean> fenjieHeroEquip(UserBean user, int equipId, int fenjieCount) {
		EquipmentBean equip = getEquip(equipId);
		FenjieLevelBean fenjie = fenjieService.getFenjie(equip.getLevel());
		
		if (fenjie == null)
			return null;
		
		return fenjie.randomReward(fenjieCount);
	}
	
	public boolean equipLevelUp(long userId, EquipmentBean equip) {
		UserEquipBean userEquip1 = null;
		UserEquipBean userEquip2 = null;
		UserEquipBean userEquip3 = null;
		List<UserEquipBean> userEquipList = userEquipService.selectUserEquipList(userId);
		for (UserEquipBean userEquip : userEquipList) {
			if (userEquip.getEquipId() == equip.getCover1()) {
				userEquip1 = userEquip;
			}
			if (userEquip.getEquipId() == equip.getCover2()) {
				userEquip2 = userEquip;
			}
			if (userEquip.getEquipId() == equip.getCover3()) {
				userEquip3 = userEquip;
			}
		}
		
		
		boolean ret = false;
		if ((userEquip1 != null || equip.getCover1() == 0) 
				&& (userEquip2 != null || equip.getCover2() == 0) 
				&& (userEquip3 != null || equip.getCover3() == 0)) {
			ret = (userEquip1 == null || userEquip1.getEquipCount() >= equip.getCount1()) 
					&& (userEquip2 == null || userEquip2.getEquipCount() >= equip.getCount2()) 
					&& (userEquip3 == null || userEquip3.getEquipCount() >= equip.getCount3());
		}
		
		if (ret) {
			if (userEquip1 != null) {
				userEquip1.setEquipCount(userEquip1.getEquipCount() - equip.getCount1());
				userEquipService.updateUserEquip(userEquip1);
			}
			if (userEquip2 != null) {
				userEquip2.setEquipCount(userEquip2.getEquipCount() - equip.getCount2());
				userEquipService.updateUserEquip(userEquip2);
			}
			if (userEquip3 != null) {
				userEquip3.setEquipCount(userEquip3.getEquipCount() - equip.getCount3());
				userEquipService.updateUserEquip(userEquip3);
			}
		}
		return ret;
	}
	
	public List<RewardInfo> saleEquip(UserBean user, List<Item> itemList) {
		boolean canSale = canSaleEquip(user.getId(), itemList);
		if (!canSale)
			return null;
		
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		for (Item item : itemList) {
			RewardInfo.Builder reward = RewardInfo.newBuilder();
			reward.setItemid(RewardConst.COIN);
			reward.setCount(0);
			int itemId = item.getItemId();
			int itemCount = item.getItemCount();
			if(itemId > RewardConst.CHIP) {
				Chip chip = equipRedisService.getChip(itemId);
				reward.setCount(chip.getCost() * itemCount);	
			} else if (itemId > RewardConst.EQUIPMENT) {
				EquipmentBean equip = getEquip(itemId);
				reward.setCount(TypeTranslatedUtil.stringToInt(equip.getCost()) * itemCount);
			} 
			
			rewardList = rewardService.mergeReward(rewardList, reward.build());
			userEquipService.useUserEquip(user.getId(), itemId, itemCount);
		}
		return rewardList;
	}
	
	private boolean canSaleEquip(long userId, List<Item> itemList) {
		for (Item item : itemList) {
			UserEquipBean userEquip = userEquipService.selectUserEquip(userId, item.getItemId());
			if (userEquip.getEquipCount() < item.getItemCount())
				return false;
		}
		
		return true;
	}
	
	private void parseAndSaveEquipConfig() {
		List<EquipmentBean> list = EquipmentBean.xmlParse();
		equipRedisService.setEquipList(list);;
	}
}
