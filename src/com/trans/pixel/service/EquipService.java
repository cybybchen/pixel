package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.EquipmentBean;
import com.trans.pixel.model.FenjieLevelBean;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.hero.HeroBean;
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.model.userinfo.UserFoodBean;
import com.trans.pixel.protoc.Commands.Chip;
import com.trans.pixel.protoc.Commands.ClearFood;
import com.trans.pixel.protoc.Commands.HeroRareLevelupRank;
import com.trans.pixel.protoc.Commands.Item;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.service.redis.ClearRedisService;
import com.trans.pixel.service.redis.EquipRedisService;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class EquipService {
	private static final Logger log = LoggerFactory.getLogger(EquipService.class);
	
	@Resource
	private UserEquipService userEquipService;
	@Resource
	private EquipRedisService equipRedisService;
	@Resource
	private FenjieService fenjieService;
	@Resource
	private RewardService rewardService;
	@Resource
	private ClearRedisService clearRedisService;
	@Resource
	private UserFoodService userFoodService;
	@Resource
	private HeroService heroService;
	@Resource
	private UserHeroService userHeroService;
	
	public EquipmentBean getEquip(int itemId) {
		EquipmentBean equip = equipRedisService.getEquip(itemId);
		if (equip == null) {
			parseAndSaveEquipConfig();
			equip = equipRedisService.getEquip(itemId);
		}
		
		return equip;
	}
	
	public Map<String, EquipmentBean> getEquipConfig() {
		Map<String, EquipmentBean> equipConfig = equipRedisService.getEquipConfig();
		if (equipConfig.isEmpty()) {
			parseAndSaveEquipConfig();
			equipConfig = equipRedisService.getEquipConfig();
		}
		
		return equipConfig;
	}
	
	public EquipmentBean getEquip(Map<String, EquipmentBean> map, int itemId) {
		EquipmentBean equip = map.get("" + itemId);		
		if (equip != null)
			return equip;
		return getEquip(itemId);
	}
	
	public int calHeroEquipLevel(HeroInfoBean heroInfo) {
		String[] equipIds = heroInfo.equipIds();
		int level = 0;
		for (String equipId : equipIds) {
			EquipmentBean equip = getEquip(TypeTranslatedUtil.stringToInt(equipId));
			if (equip != null) {
				level += equip.getLevel();
			}
		}
		
		return level;
	}
	
	public int equipCompose(UserBean user, int levelUpId, int count, List<UserEquipBean> userEquipList) {
		int composeEquipId = 0;;
		if (levelUpId < RewardConst.CHIP) {//合成装备
			EquipmentBean equip = getEquip(levelUpId);
			boolean equipLevelUpRet = equipLevelUp(user.getId(), equip, userEquipList);
			if (equipLevelUpRet) {
				userEquipService.addUserEquip(user.getId(), equip.getItemid(), 1);
				composeEquipId = levelUpId;
				userEquipList.add(userEquipService.selectUserEquip(user.getId(), composeEquipId));
			}
		} else { //合成碎片
			Chip chip = equipRedisService.getChip(levelUpId);
			if (chip != null && chip.getAim() > 0) {
				UserEquipBean userEquip = userEquipService.selectUserEquip(user.getId(), levelUpId);
				if (userEquip != null && userEquip.getEquipCount() >= chip.getCount() * count) {
					userEquip.setEquipCount(userEquip.getEquipCount() - chip.getCount() * count);
					userEquipService.updateUserEquip(userEquip);
					rewardService.doReward(user, chip.getAim(), count);
//						userEquipService.addUserEquip(user.getId(), chip.getAim(), count);
					composeEquipId = chip.getAim();
						userEquipList.add(userEquip);
						if (composeEquipId < RewardConst.HERO)
							userEquipList.add(userEquipService.selectUserEquip(user.getId(), composeEquipId));
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
		
		return getFenjieReward(fenjie, equip, fenjieCount);
	}
	
	public List<RewardBean> fenjieHeroEquip(UserBean user, int equipId, int fenjieCount) {
		EquipmentBean equip = getEquip(equipId);
		FenjieLevelBean fenjie = fenjieService.getFenjie(equip.getLevel());
		
		if (fenjie == null)
			return null;
		
		return getFenjieReward(fenjie, equip, fenjieCount);
	}
	
	private List<RewardBean> getFenjieReward(FenjieLevelBean fenjie, EquipmentBean equip, int count) {
		List<RewardBean> rewardList = fenjie.randomReward(count, equip.getIsequipment());
		List<RewardBean> extraList = new ArrayList<RewardBean>();
		if (equip.getFenjie1() > 0)
			extraList.add(RewardBean.init(equip.getFenjie1(), equip.getFenjiecount1()));
		if (equip.getFenjie2() > 0)
			extraList.add(RewardBean.init(equip.getFenjie2(), equip.getFenjiecount2()));
		if (equip.getFenjie3() > 0)
			extraList.add(RewardBean.init(equip.getFenjie3(), equip.getFenjiecount3()));
		
		return rewardService.mergeReward(rewardList, extraList);
	}
	
	private List<UserEquipBean> getCostEquipList(EquipmentBean equip) {
		List<UserEquipBean> costEquipList = new ArrayList<UserEquipBean>();
		UserEquipBean costEquip = UserEquipBean.initUserEquip(equip.getCover1(), equip.getCount1());
		costEquipList = mergeEquipList(costEquipList, costEquip);
		
		costEquip = UserEquipBean.initUserEquip(equip.getCover2(), equip.getCount2());
		costEquipList = mergeEquipList(costEquipList, costEquip);
		
		costEquip = UserEquipBean.initUserEquip(equip.getCover3(), equip.getCount3());
		costEquipList = mergeEquipList(costEquipList, costEquip);
		
		return costEquipList;
		
	}
	
	private List<UserEquipBean> mergeEquipList(List<UserEquipBean> costEquipList, UserEquipBean costEquip) {
		if (costEquip.getEquipId() == 0 || costEquip.getEquipCount() == 0)
			return costEquipList;
		
		for (UserEquipBean userEquip : costEquipList) {
			if (costEquip.getEquipId() == userEquip.getEquipId()) {
				userEquip.setEquipCount(userEquip.getEquipCount() + costEquip.getEquipCount());
				return costEquipList;
			}
		}
		
		costEquipList.add(costEquip);
		return costEquipList;
	}
	
	private Map<Integer, Integer> getCostEquipMap(List<UserEquipBean> costEquipList) {
		Map<Integer, Integer> costEquipMap = new HashMap<Integer, Integer>();
		for (UserEquipBean costEquip : costEquipList) {
			costEquipMap.put(costEquip.getEquipId(), -costEquip.getEquipCount());
		}
		
		return costEquipMap;
	}
	
	public boolean equipLevelUp(long userId, EquipmentBean equip, List<UserEquipBean> returnUserEquipList) {
		List<UserEquipBean> userEquipList = userEquipService.selectUserEquipList(userId);
		List<UserEquipBean> costUserEquipList = getCostEquipList(equip);
		
		Map<Integer, Integer> updateEquipMap = getCostEquipMap(costUserEquipList);
		
		for (UserEquipBean userEquip : userEquipList) {
			int equipId = userEquip.getEquipId();
			if (updateEquipMap.get(equipId) != null && updateEquipMap.get(equipId) != 0)
				updateEquipMap.put(equipId, updateEquipMap.get(equipId) + userEquip.getEquipCount());
		}
		
		boolean ret = true;
		Iterator<Entry<Integer, Integer>> it = updateEquipMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, Integer> entry = it.next();
			if (entry.getValue() < 0) {
				ret = false;
				break;
			}
		}
		
		if (ret) {
			it = updateEquipMap.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Integer, Integer> entry = it.next();
				UserEquipBean userEquip = UserEquipBean.initUserEquip(entry.getKey(), entry.getValue());
				userEquip.setUserId(userId);
				userEquipService.updateUserEquip(userEquip);
				returnUserEquipList.add(userEquip);
			}
		}
		return ret;
	}
	
	public List<RewardInfo> sale(UserBean user, List<Item> itemList, MultiReward.Builder costItems) {
		boolean canSale = canSaleEquip(user, itemList);
		if (!canSale)
			return null;
		
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		for (Item item : itemList) {
			RewardInfo.Builder reward = RewardInfo.newBuilder();
			
			int itemId = item.getItemId();
			int itemCount = item.getItemCount();
			reward.setItemid(RewardConst.COIN);
			reward.setCount(getSaleRewardCount(itemId, itemCount));
			
			rewardList = rewardService.mergeReward(rewardList, reward.build());
			RewardInfo.Builder builder = RewardInfo.newBuilder();
			builder.setItemid(itemId);
			if (itemId > RewardConst.FOOD) {
				UserFoodBean userFood = userFoodService.addUserFood(user, itemId, -itemCount);
				builder.setCount(userFood.getCount());
			} else {
				UserEquipBean userEquip = userEquipService.useUserEquip(user.getId(), itemId, itemCount);
				builder.setCount(userEquip.getEquipCount());
			}
			costItems.addLoot(builder.build());
			
		}
		return rewardList;
	}
	
	public boolean canHeroRareLevelup(UserBean user, HeroInfoBean heroInfo, HeroRareLevelupRank herorare, List<UserEquipBean> equipList) {
		HeroBean hero = heroService.getHero(heroInfo.getHeroId());
			
		equipList.add(UserEquipBean.initUserEquip(herorare.getEquip1(), 
				userEquipService.selectUserEquip(user.getId(), herorare.getEquip1()).getEquipCount() - herorare.getCount1()));
		if (hero.getQuality() >= 2)
			equipList.add(UserEquipBean.initUserEquip(herorare.getEquip2(), 
					userEquipService.selectUserEquip(user.getId(), herorare.getEquip2()).getEquipCount() - herorare.getCount2()));
		
		if (hero.getQuality() >= 3)
			equipList.add(UserEquipBean.initUserEquip(herorare.getEquip3(), 
					userEquipService.selectUserEquip(user.getId(), herorare.getEquip3()).getEquipCount() - herorare.getCount3()));
		
		if (hero.getQuality() >= 4)
			equipList.add(UserEquipBean.initUserEquip(herorare.getEquip4(), 
					userEquipService.selectUserEquip(user.getId(), herorare.getEquip4()).getEquipCount() - herorare.getCount4()));
		
		if (hero.getQuality() >= 6)
			equipList.add(UserEquipBean.initUserEquip(herorare.getEquip5(), 
					userEquipService.selectUserEquip(user.getId(), herorare.getEquip5()).getEquipCount() - herorare.getCount5()));	
		
		for (UserEquipBean userEquip : equipList) {
			if (userEquip.getEquipCount() < 0)
				return false;
		}
		
		return true;
	}
	
	private int getSaleRewardCount(int itemId, int itemCount) {
		int rewardCount = 0;
		if (itemId > RewardConst.FOOD) {
			ClearFood food = clearRedisService.getClearFood(itemId);
			rewardCount = food.getCost();
		} else if(itemId > RewardConst.CHIP) {
			Chip chip = equipRedisService.getChip(itemId);
			rewardCount = chip.getCost();	
		} else if (itemId > RewardConst.EQUIPMENT) {
			EquipmentBean equip = getEquip(itemId);
			rewardCount = TypeTranslatedUtil.stringToInt(equip.getCost());
		} 
		
		return rewardCount * itemCount;
	}
	
	private boolean canSaleEquip(UserBean user, List<Item> itemList) {
		for (Item item : itemList) {
			if (item.getItemId() > RewardConst.FOOD) {
				UserFoodBean userFood = userFoodService.selectUserFood(user, item.getItemId());
				if (userFood.getCount() < item.getItemCount())
					return false;
			} else {
				UserEquipBean userEquip = userEquipService.selectUserEquip(user.getId(), item.getItemId());
				if (userEquip.getEquipCount() < item.getItemCount())
					return false;
			}
		}
		
		return true;
	}
	
	private void parseAndSaveEquipConfig() {
		List<EquipmentBean> list = EquipmentBean.xmlParse();
		equipRedisService.setEquipList(list);;
	}
}
