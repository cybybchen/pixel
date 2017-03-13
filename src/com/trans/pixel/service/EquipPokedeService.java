package com.trans.pixel.service;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipPokedeBean;
import com.trans.pixel.protoc.Commands.EquipIncrease;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.service.redis.EquipPokedeRedisService;

@Service
public class EquipPokedeService {
	private static final Logger log = LoggerFactory.getLogger(EquipPokedeService.class);
	 
	@Resource
	private EquipPokedeRedisService equipPokedeRedisService;
	@Resource
	private CostService costService;
	@Resource
	private RewardService rewardService;
	
	public ResultConst heroStrengthen(UserEquipPokedeBean pokede, UserBean user, MultiReward.Builder rewards) {
		if (pokede == null)
			return ErrorConst.EQUIP_IS_NOT_EXIST_ERROR;
		
		EquipIncrease equipIncrease = equipPokedeRedisService.getEquipIncrease(pokede.getLevel());
		if (equipIncrease == null)
			return ErrorConst.EQUIP_IS_NOT_EXIST_ERROR;
		
		rewards.addAllLoot(rewardService.convertCost(equipIncrease.getCostList()));
		
		if (!costService.cost(user, rewards.build()))
			return ErrorConst.NOT_ENOUGH_PROP;
		
		if (RandomUtils.nextInt(10000) >= equipIncrease.getRate()) {
			if (equipIncrease.getZero() == 1) {
				pokede.setLevel(0);
			}
			
			return SuccessConst.EQUIP_STRENGTHEN_FALIED_SUCCESS;
		}
		
		pokede.setLevel(pokede.getLevel() + 1);
		return SuccessConst.EQUIP_STRENGTHEN_SUCCESS;
	}
}
