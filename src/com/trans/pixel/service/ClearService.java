package com.trans.pixel.service;

import java.util.ArrayList;
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
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserClearBean;
import com.trans.pixel.model.userinfo.UserPokedeBean;
import com.trans.pixel.protoc.Commands.ClearAttribute;
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
	
	
	private static final int CLEAR_TYPE_COIN_COST = 10000;
	private static final int CLEAR_TYPE_JEWEL_COST = 100;
	
	private static final int USER_POKEDE_LIMIT_1 = 1;
	private static final int USER_POKEDE_LIMIT_2 = 4;
	private static final int USER_POKEDE_LIMIT_3 = 7;
	private static final int CLEAR_POSITION_2 = 2;
	private static final int CLEAR_POSITION_3 = 3;
	
	public ResultConst clearHero(UserPokedeBean userPokede, int position, int type, UserBean user, int count, List<UserClearBean> clearList) {
		if (userPokede.getLevel() < USER_POKEDE_LIMIT_1 
				|| (userPokede.getLevel() < USER_POKEDE_LIMIT_2 && position >= CLEAR_POSITION_2)
				|| (userPokede.getLevel() < USER_POKEDE_LIMIT_3 && position >= CLEAR_POSITION_3))
			return ErrorConst.CLEAR_IS_LOCKED_ERROR;
		
		if (!costService.costAndUpdate(user, type, (type == RewardConst.COIN ? CLEAR_TYPE_COIN_COST : CLEAR_TYPE_JEWEL_COST) * count)) {
			return type == RewardConst.COIN ? ErrorConst.NOT_ENOUGH_COIN : ErrorConst.NOT_ENOUGH_JEWEL;
		}
		
		while (clearList.size() < count) {
			UserClearBean userClear = clearHero(user, userPokede.getHeroId(), position, type);
			if (userClear != null)
	//			userClearService.updateUserClear(userClear);
				userClear.setId(clearList.size());
				clearList.add(userClear);
		}
		
		userClearRedisService.updateUserLastClearInfoList(clearList, user.getId());
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
		List<ClearAttribute> attList = getClearAttribute(position);
		int allWeight = 0;
		for (ClearAttribute att : attList) {
			allWeight += att.getWeight();
		}
		
		for (ClearAttribute att : attList) {
			int randWeight = RandomUtils.nextInt(allWeight);
			if (randWeight < att.getWeight()) 
				return randomClear(att, type, heroId, user.getId());
			
			allWeight -= att.getWeight();
		}
		
		return null;
	}
	
	private UserClearBean randomClear(ClearAttribute att, int type, int heroId, long userId) {
		UserClearBean userClear = new UserClearBean();
		userClear.setClearId(att.getId());
		userClear.setHeroId(heroId);
		userClear.setPosition(att.getPosition());
		userClear.setUserId(userId);
		
		int count1 = att.getCount1();
		int count2 = att.getCount2();
		if (type == RewardConst.JEWEL)
			count1 = (count1 + count2) / 2;
		
		int randCount = count1 + RandomUtils.nextInt(1 + (count2 - count1) / att.getInterval()) * att.getInterval();
		userClear.setCount(randCount);
		
		return userClear;
	}
	
	private List<ClearAttribute> getClearAttribute(int position) {
		Map<String, ClearAttribute> map = clearRedisService.getClearAttributeConfig();
		List<ClearAttribute> attList = new ArrayList<ClearAttribute>();
		Iterator<Entry<String, ClearAttribute>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			ClearAttribute att = it.next().getValue();
			if (att.getPosition() == position)
				attList.add(att);
		}
		
		return attList;
	}
}
