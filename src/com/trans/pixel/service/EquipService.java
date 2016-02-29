package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.EquipmentBean;
import com.trans.pixel.model.FenjieLevelBean;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
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
	
	public EquipmentBean getEquip(int itemId) {
		EquipmentBean equip = equipRedisService.getEquip(itemId);
		if (equip == null) {
			parseAndSaveEquipConfig();
			equip = equipRedisService.getEquip(itemId);
		}
		
		return equip;
	}
	
	public int calHeroEquipLevel(HeroInfoBean heroInfo) {
		String[] equipIds = heroInfo.getEquipIds();
		int level = 0;
		for (String equipId : equipIds) {
			EquipmentBean equip = getEquip(TypeTranslatedUtil.stringToInt(equipId));
			if (equip != null)
				level += equip.getLevel();
		}
		
		return level;
	}
	
	public ResultConst equipCompose(UserBean user, int levelUpId) {
		ResultConst result = ErrorConst.NOT_ENOUGH_EQUIP;
		EquipmentBean equip = getEquip(levelUpId);
		if (equip == null) {
			result = ErrorConst.EQUIP_LEVELUP_ERROR;
		} else {
			boolean equipLevelUpRet = equipLevelUp(user.getId(), equip);
			if (equipLevelUpRet) {
				userEquipService.addUserEquip(user.getId(), equip.getItemid(), 1);
				result = SuccessConst.EQUIP_LEVELUP_SUCCESS;
			}
		}
			
		return result;
	}
	
	public List<RewardBean> fenjieEquip(UserBean user, int equipId, int fenjieCount) {
		UserEquipBean userEquip = userEquipService.selectUserEquip(user.getId(), equipId);
		if (userEquip.getEquipCount() < fenjieCount)
			return null;
		
		EquipmentBean equip = getEquip(equipId);
		FenjieLevelBean fenjie = fenjieService.getFenjie(equip.getLevel());
		
		userEquip.setEquipCount(userEquip.getEquipCount() - fenjieCount);
		userEquipService.updateUserEquip(userEquip);
		
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
	
	private void parseAndSaveEquipConfig() {
		List<EquipmentBean> list = EquipmentBean.xmlParse();
		equipRedisService.setEquipList(list);;
	}
}
