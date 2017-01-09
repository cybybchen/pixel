package com.trans.pixel.service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.hero.HeroBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserClearBean;
import com.trans.pixel.model.userinfo.UserPokedeBean;
import com.trans.pixel.model.userinfo.UserPropBean;
import com.trans.pixel.protoc.Commands.ClearAttribute;
import com.trans.pixel.protoc.Commands.ClearAttributeOrder;
import com.trans.pixel.protoc.Commands.ClearCost;
import com.trans.pixel.protoc.Commands.ClearLevel;
import com.trans.pixel.protoc.Commands.Strengthen;
import com.trans.pixel.service.redis.ClearRedisService;
import com.trans.pixel.service.redis.UserClearRedisService;

@Service
public class ClearService {
	private static final Logger log = LoggerFactory.getLogger(ClearService.class);
	@Resource
	private ClearRedisService clearRedisService;
	@Resource
	private CostService costService;
	@Resource
	private UserClearService userClearService;
	@Resource
	private UserClearRedisService userClearRedisService;
	@Resource
	private UserPropService userPropService;
	@Resource
	private LogService logService;
	@Resource
	private HeroService heroService;
	
	
	private static final int CLEAR_TYPE_COIN_COST = 10000;
	private static final int CLEAR_TYPE_JEWEL_COST = 100;
	
	private static final int USER_POKEDE_LIMIT_1 = 3;
	private static final int USER_POKEDE_LIMIT_2 = 6;
	private static final int USER_POKEDE_LIMIT_3 = 9;
	private static final int CLEAR_POSITION_2 = 2;
	private static final int CLEAR_POSITION_3 = 3;
	
	public ResultConst clearHero(UserPokedeBean userPokede, int position, int type, UserBean user, int count, 
			List<UserClearBean> clearList, List<UserPropBean> userPropList) {
		if (userPokede.getLevel() < USER_POKEDE_LIMIT_1 
				|| (userPokede.getLevel() < USER_POKEDE_LIMIT_2 && position >= CLEAR_POSITION_2)
				|| (userPokede.getLevel() < USER_POKEDE_LIMIT_3 && position >= CLEAR_POSITION_3))
			return ErrorConst.CLEAR_IS_LOCKED_ERROR;
		
		HeroBean hero = heroService.getHero(userPokede.getHeroId());
		ClearCost clearCost = clearRedisService.getClearCost(hero.getQuality());
		int costType = 0;
		int costCount = 0;
		if (position == 1) {
			costType = clearCost.getItemid1();
			costCount = clearCost.getItemcount1();
		} else if (position == 2) {
			costType = clearCost.getItemid2();
			costCount = clearCost.getItemcount2();
		} else if (position == 3) {
			costType = clearCost.getItemid3();
			costCount = clearCost.getItemcount3();
		}
		if (!costService.costAndUpdate(user, costType, costCount * count)) {
			return ErrorConst.NOT_ENOUGH_CLEARPROP_ERROR;
		}
		
		while (clearList.size() < count) {
			UserClearBean userClear = clearHero(user, userPokede.getHeroId(), position, RewardConst.JEWEL);
			if (userClear != null)
	//			userClearService.updateUserClear(userClear);
				userClear.setId(clearList.size());
				clearList.add(userClear);
		}
		
		userClearRedisService.updateUserLastClearInfoList(clearList, user.getId());
		
		userPropList.add(userPropService.selectUserProp(user.getId(), costType));
		
		return SuccessConst.HERO_CLEAR_SUCCESS;
	}
	
	public UserClearBean choseClear(UserBean user, int id) {	
		UserClearBean userClear = userClearRedisService.getLastClear(id, user.getId());
		if (userClear == null)
			return null;
		
		log.debug("11:" + userClear + "|" + userClear.getHeroId());
		userClearService.updateUserClear(userClear);
		
		
		return userClear;
	}
	
	public UserClearBean clearHero(UserBean user, int heroId, int position, int type) {
		ClearAttribute att = getClearAttribute(position, 0);
		int allWeight = 0;
		for (ClearAttributeOrder order : att.getOrderList()) {
			allWeight += order.getWeight();
		}
		
		for (ClearAttributeOrder order : att.getOrderList()) {
			int randWeight = RandomUtils.nextInt(allWeight);
			if (randWeight < order.getWeight()) 
				return randomClear(order, att.getPosition(), heroId, user.getId());
			
			allWeight -= order.getWeight();
		}
		
		return null;
	}
	
	public ResultConst heroStrengthen(UserPokedeBean userPokede, UserBean user, List<UserPropBean> propList) {
		Strengthen strengthen = clearRedisService.getStrengthen(userPokede.getStrengthen() + 1);
		if (strengthen == null)
			return ErrorConst.HERO_STRENGTHEN_ERROR;
		
		
		
		if (!costService.cost(user, strengthen.getItemid(), strengthen.getCount())) {
			UserPropBean userProp = userPropService.selectUserProp(user.getId(), strengthen.getItemid());
			if (userProp != null)
				propList.add(userProp);
			return ErrorConst.NOT_ENOUGH_PROP;
		}
		
		UserPropBean userProp = userPropService.selectUserProp(user.getId(), strengthen.getItemid());
		if (userProp != null)
			propList.add(userProp);
		
		boolean ret = strengthenSuccess(strengthen.getSuccess());
		logService.sendQianghuaLog(user.getServerId(), user.getId(), userPokede.getHeroId(), userPokede.getStrengthen(), ret ? 1 : 0);
		if (ret) {
			userPokede.setStrengthen(userPokede.getStrengthen() + 1);
			
			return SuccessConst.HERO_STRENGTHEN_SUCCESS;
		}
		
		return SuccessConst.HERO_STRENGTHEN_FAILED_SUCCESS;
	}

	public Strengthen getStrengthen(int id) {
		return clearRedisService.getStrengthen(id);
	}
	
	public Map<String, Strengthen> getStrengthenConfig() {
		return clearRedisService.getStrengthenConfig();
	}
	
	private boolean strengthenSuccess(int percent) {
		return RandomUtils.nextInt(100) < percent;
	}
	
	private UserClearBean randomClear(ClearAttributeOrder order, int position, int heroId, long userId) {
		UserClearBean userClear = new UserClearBean();
		userClear.setClearId(order.getType());
		userClear.setHeroId(heroId);
		userClear.setPosition(position);
		userClear.setUserId(userId);
		userClear.setRare(order.getRare());
		
		int count1 = order.getCount1();
		int count2 = order.getCount2();
		
		int randCount = count1 + RandomUtils.nextInt(1 + (count2 - count1) / order.getInterval()) * order.getInterval();
		userClear.setCount(randCount);
		
		return userClear;
	}
	
	private ClearAttribute getClearAttribute(int position, int type) {
		Map<String, ClearAttribute> map = clearRedisService.getClearAttributeConfig();
		Iterator<Entry<String, ClearAttribute>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			ClearAttribute att = it.next().getValue();
			if (att.getPosition() == position && att.getZuanshi() == type)
				return att;
		}
		
		return null;
	}
	
	public int getClearLevelZhanli(int heroId, Map<String, ClearLevel> map, List<UserPokedeBean> userPokedeList) {
		for (UserPokedeBean userPokede : userPokedeList) {
			if (userPokede.getHeroId() == heroId) {
				int zhanli = 0;
				Iterator<Entry<String, ClearLevel>> it = map.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, ClearLevel> entry = it.next();
					ClearLevel clear = entry.getValue();
					if (clear.getLevel() <= userPokede.getLevel())
						zhanli += clear.getZhanli();
				}
				
				return zhanli;
			}
			
		}
		return 0;
	}
}
