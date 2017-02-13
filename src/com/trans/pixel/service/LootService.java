package com.trans.pixel.service;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.DaguanBean;
import com.trans.pixel.model.LootBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.protoc.Commands.Libao;
import com.trans.pixel.protoc.Commands.LibaoList;
import com.trans.pixel.protoc.Commands.Rmb;
import com.trans.pixel.protoc.Commands.YueKa;
import com.trans.pixel.service.redis.LootRedisService;
import com.trans.pixel.service.redis.RechargeRedisService;
import com.trans.pixel.service.redis.UserRedisService;

@Service
public class LootService {
	Logger logger = LoggerFactory.getLogger(LootService.class);
	@Resource
	private LootRedisService lootRedisService;
	@Resource
	private UserLevelService userLevelRecordService;
	@Resource
	private LevelService levelService;
	@Resource
	private UserService userService;
	@Resource
	private RewardService rewardService;
	@Resource
	private ShopService shopService;
	@Resource
	private UserRedisService userRedisService;
	@Resource
	private RechargeRedisService rechargeRedisService;
	
	public LootBean getLootByLevelId(int levelId) {
		LootBean loot = lootRedisService.getLootByLevelId(levelId);
		if (loot == null && levelId > 1000) {
			parseAndSaveConfig();
			loot = lootRedisService.getLootByLevelId(levelId);
		}
		
		return loot;
	}
	
	public UserBean updateLootResult(UserBean user) {
		long userId = user.getId();
		UserLevelBean userLevelRecord = userLevelRecordService.selectUserLevelRecord(userId);
		DaguanBean dg = levelService.getDaguan(userLevelRecord.getPutongLevel());
		long addGold = 0;
		long addExp = 0;
		if (dg != null && user.getLastLootTime() != 0) {
			long deltaTime = (int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND) - user.getLastLootTime();
			addGold = deltaTime * dg.getGold();
			int jingyanPer = 0;
			long now = userRedisService.now();
			long today0 = userRedisService.caltoday(now, 0);
			LibaoList libaolist = shopService.getLibaoShop(user, false);
			Map<Integer, YueKa> map = shopService.getYueKas();
			for(Libao libao : libaolist.getLibaoList()){
				long time = 0;
				if(libao.hasValidtime() && libao.getValidtime().length() > 5){
					try {
						time = new SimpleDateFormat(TimeConst.DEFAULT_DATETIME_FORMAT).parse(libao.getValidtime()).getTime()/1000L;
					} catch (Exception e) {
						logger.error(time+"", e);
					}
				}
				if(time >= today0){
					Rmb rmb = rechargeRedisService.getRmb(libao.getRechargeid());
					YueKa yueka = map.get(rmb.getItemid());
					if(yueka == null){
						logger.error("ivalid yueka type "+libao.getRechargeid());
						continue;
					}
					jingyanPer += yueka.getJingyanPer();
				}
			}
			addExp = deltaTime * dg.getExperience();
			addExp *= (100 + jingyanPer) / 100.f;
			rewardService.doReward(user, RewardConst.COIN, addGold);
			rewardService.doReward(user, RewardConst.EXP, addExp);
		}
		user.setLastLootTime((int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND));
		userService.updateUser(user);
		
		return user;
	}
	
	private void parseAndSaveConfig() {
		List<LootBean> lootList = LootBean.xmlParse();
		if (lootList != null && lootList.size() != 0) {
			lootRedisService.setLootList(lootList);
		}
	}
}
