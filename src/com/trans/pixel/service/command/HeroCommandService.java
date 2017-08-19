package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.LogString;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.HeroInfoBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.model.userinfo.UserPropBean;
import com.trans.pixel.model.userinfo.UserTeamBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.Base.UserTalent;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.EquipProto.RequestAddHeroEquipCommand;
import com.trans.pixel.protoc.ExtraProto.Fenjie;
import com.trans.pixel.protoc.HeroProto.FenjieHeroInfo;
import com.trans.pixel.protoc.HeroProto.Hero;
import com.trans.pixel.protoc.HeroProto.RequestBuyHeroPackageCommand;
import com.trans.pixel.protoc.HeroProto.RequestChaijieHeroCommand;
import com.trans.pixel.protoc.HeroProto.RequestFenjieHeroCommand;
import com.trans.pixel.protoc.HeroProto.RequestHeroLevelUpCommand;
import com.trans.pixel.protoc.HeroProto.RequestHeroLevelUpToCommand;
import com.trans.pixel.protoc.HeroProto.RequestHeroSpUpCommand;
import com.trans.pixel.protoc.HeroProto.RequestLockHeroCommand;
import com.trans.pixel.protoc.HeroProto.RequestResetHeroSkillCommand;
import com.trans.pixel.protoc.HeroProto.RequestSubmitComposeSkillCommand;
import com.trans.pixel.protoc.HeroProto.ResponseDeleteHeroCommand;
import com.trans.pixel.protoc.HeroProto.ResponseHeroResultCommand;
import com.trans.pixel.protoc.HeroProto.UserTeam.EquipRecord;
import com.trans.pixel.service.CostService;
import com.trans.pixel.service.EquipService;
import com.trans.pixel.service.HeroLevelUpService;
import com.trans.pixel.service.HeroService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.SkillService;
import com.trans.pixel.service.TalentService;
import com.trans.pixel.service.UserEquipService;
import com.trans.pixel.service.UserHeroService;
import com.trans.pixel.service.UserPropService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.UserTalentService;
import com.trans.pixel.service.UserTeamService;
import com.trans.pixel.service.redis.FenjieRedisService;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.service.redis.StarRedisService;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class HeroCommandService extends BaseCommandService {
	private Logger logger = Logger.getLogger(HeroCommandService.class);
	
	private static final int BUY_HERO_PACKAGE_COST = 50;
	private static final int BUY_HERO_PACKAGE_COUNT = 5;
	private static final int HERO_PACKAGE_LIMIT = 500;
	
//	private static final int RESET_HERO_SKILL_COST = 10000;
	@Resource
	private HeroLevelUpService heroLevelUpService;
	@Resource
	private UserHeroService userHeroService;
	@Resource
	private SkillService skillService;
	@Resource
	private PushCommandService pushCommandService;
	@Resource
	private EquipService equipService;
	@Resource
	private UserEquipService userEquipService;
	@Resource
	private CostService costService;
	@Resource
	private UserService userService;
	@Resource
	private HeroService heroService;
	@Resource
	private UserTeamService userTeamService;
	@Resource
	private LogService logService;
	@Resource
	private UserTalentService userTalentService;
	@Resource
	private FenjieRedisService fenjieRedisService;
	@Resource
	private StarRedisService starService;
	@Resource
	private TalentService talentService;
	@Resource
	private UserPropService userPropService;
	
	public void heroLevelUpTo(RequestHeroLevelUpToCommand cmd, Builder responseBuilder, UserBean user) {
		HeroInfoBean heroInfo = userHeroService.selectUserHero(user.getId(), cmd.getInfoId());
		ResultConst result = heroLevelUpService.levelUpHeroTo(user, heroInfo, cmd.getLevelUp());
		if (result instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
		}else{
			userHeroService.updateUserHero(heroInfo);
		}

		ResponseHeroResultCommand.Builder builder = ResponseHeroResultCommand.newBuilder();
		builder.setHeroId(cmd.getHeroId());
		builder.addHeroInfo(heroInfo.buildHeroInfo());
		responseBuilder.setHeroResultCommand(builder.build());
			
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}
	public void heroLevelUp(RequestHeroLevelUpCommand cmd, Builder responseBuilder, UserBean user) {
		int levelUpType = cmd.getLevelUpType();
		int heroId = cmd.getHeroId();
		long infoId = cmd.getInfoId();
		long userId = user.getId();
		int skillId = 0;
		if (cmd.hasSkillId())
			skillId = cmd.getSkillId();
		List<Long> costInfoIds = new ArrayList<Long>();
		List<UserEquipBean> equipList = new ArrayList<UserEquipBean>();
		costInfoIds.addAll(cmd.getCostInfoIdList());
		HeroInfoBean heroInfo = userHeroService.selectUserHero(userId, infoId);
		ResultConst result = ErrorConst.HERO_NOT_EXIST;
		if (heroInfo != null) {
			result = heroLevelUpService.levelUpResult(user, heroInfo, levelUpType, skillId, costInfoIds, equipList);
			if (result instanceof SuccessConst)
				skillService.unlockHeroSkill(heroId, heroInfo);
		}
		if (result instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
		}else if (result instanceof SuccessConst) {
			userHeroService.updateUserHero(heroInfo);
			if (costInfoIds.size() > 0){
//				long addExp = 0;
//				int addCoin = 0;
//				List<RewardBean> rewardList = new ArrayList<RewardBean>();
				ResponseDeleteHeroCommand.Builder deleteHeroBuilder = ResponseDeleteHeroCommand.newBuilder();
				for(long costId : costInfoIds){
					HeroInfoBean delHeroInfo = userHeroService.selectUserHero(userId, costId);
					FenjieHeroInfo.Builder herobuilder = FenjieHeroInfo.newBuilder();
					herobuilder.setHeroId(0);
					if (delHeroInfo != null) {	
//						if (delHeroInfo.getRank() > 0) {
//							rewardList = rewardService.mergeReward(rewardList, heroService.getHeroRareEquip(delHeroInfo));
//						}
//						if(delHeroInfo.getLevel() > 1)
//							addExp += heroService.getDeleteExp(delHeroInfo.getLevel());
//						
//						addCoin += skillService.getResetCoin(delHeroInfo.getSkillInfoList());//升级技能消耗金币
						
						herobuilder.setHeroId(delHeroInfo.getHeroId());
					}
					
					herobuilder.setInfoId(costId);
					deleteHeroBuilder.addHeroInfo(herobuilder.build());
				}
//				if (addExp > 0)
//					rewardList.add(RewardBean.init(RewardConst.EXP, addExp));
//				if (addCoin > 0)
//					rewardList.add(RewardBean.init(RewardConst.COIN, addCoin));
				
				userHeroService.delUserHero(user.getId(), costInfoIds);
				
//				if (rewardList.size() > 0) {
//					handleRewards(responseBuilder, user, rewardList);
//				}
				responseBuilder.setDeleteHeroCommand(deleteHeroBuilder.build());
			}
		}
		ResponseHeroResultCommand.Builder builder = ResponseHeroResultCommand.newBuilder();
		builder.setHeroId(heroId);
		builder.addHeroInfo(heroInfo.buildHeroInfo());
		responseBuilder.setHeroResultCommand(builder.build());
			
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
		if (equipList.size() > 0)
			pushCommandService.pushUserEquipListCommand(responseBuilder, user, equipList);
	}
	
	//not useful
	public void heroAddEquip(RequestAddHeroEquipCommand cmd, Builder responseBuilder, UserBean user) {
		int heroId = cmd.getHeroId();
		long infoId = cmd.getInfoId();
		int equipId = cmd.getEquipId();
		long userId = user.getId();
		
		ResultConst result = ErrorConst.HERO_NOT_EXIST;
		HeroInfoBean heroInfo = userHeroService.selectUserHero(userId, infoId);;
	
		if (heroInfo != null)
			result = heroLevelUpService.addHeroEquip(user, heroInfo, heroId, equipId);
		else{
            responseBuilder.setErrorCommand(buildErrorCommand(result));
			ResponseDeleteHeroCommand.Builder deleteHeroBuilder = ResponseDeleteHeroCommand.newBuilder();
			FenjieHeroInfo.Builder herobuilder = FenjieHeroInfo.newBuilder();
			herobuilder.setHeroId(heroId);
			herobuilder.setInfoId(infoId);
			deleteHeroBuilder.addHeroInfo(herobuilder.build());
			responseBuilder.setDeleteHeroCommand(deleteHeroBuilder.build());
			return;
		}
		
		if (result instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
		} else {
			userHeroService.updateUserHero(heroInfo);
		}
		ResponseHeroResultCommand.Builder builder = ResponseHeroResultCommand.newBuilder();
		builder.setHeroId(heroId);
		builder.addHeroInfo(heroInfo.buildHeroInfo());
		responseBuilder.setHeroResultCommand(builder.build());
	}

	public void chaijieHero(RequestChaijieHeroCommand cmd, Builder responseBuilder, UserBean user) {
		UserPropBean prop = userPropService.selectUserProp(user.getId(), 39027);
		if(prop == null || prop.getPropCount() < 1) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH_PROP);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENOUGH_PROP));
			return;
		}
		ResponseDeleteHeroCommand.Builder deleteHeroBuilder = ResponseDeleteHeroCommand.newBuilder();
		ResponseDeleteHeroCommand.Builder errorHeroBuilder = ResponseDeleteHeroCommand.newBuilder();
		
		long userId = user.getId();
		FenjieHeroInfo hero  = cmd.getFenjieHero();
		boolean isError = false;
		List<Long> costInfoIds = new ArrayList<Long>();
		HeroInfoBean heroInfo = userHeroService.selectUserHero(userId, hero.getInfoId());
		if (heroInfo != null) {
			if(heroInfo.isLock()){
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.HERO_LOCKED);
				responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.HERO_LOCKED));
				return;
			}else if(heroInfo.getStarLevel() < 5){
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.HERO_STAR_FIRST);
				responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.HERO_STAR_FIRST));
				return;
			}else if(heroInfo.getRank() < 10) {
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.HERO_RANK_FIRST);
				responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.HERO_RANK_FIRST));
				return;
			}
			prop.setPropCount(prop.getPropCount()-1);
			userPropService.updateUserProp(prop);
			pushCommandService.pushUserPropCommand(responseBuilder, user, prop);//返回盖亚女神的怀抱
			Hero heroconfig = heroService.getHero(heroInfo.getHeroId());
			Fenjie fenjie = fenjieRedisService.getFenjie(heroconfig.getQuality());
			RewardInfo.Builder reward = RewardInfo.newBuilder();
			reward.setItemid(fenjie.getLootlist().getItemid());//技能石
			reward.setCount(skillService.getSP(heroInfo)+heroInfo.getSp());
			MultiReward.Builder rewards = MultiReward.newBuilder();
			rewards.addLoot(reward);
			reward = RewardInfo.newBuilder();
			reward.setItemid(39013+heroInfo.getStarLevel()-5);//升星书
			reward.setCount(1);
			rewards.addLoot(reward);
			reward = RewardInfo.newBuilder();
			reward.setItemid(39016+heroInfo.getRank()-10);//进阶药水
			reward.setCount(1);
			rewards.addLoot(reward);
			if(heroconfig.getQuality() == 4) {
				reward = RewardInfo.newBuilder();//紫色自选包
				reward.setItemid(34091);
				reward.setCount(20);
				rewards.addLoot(reward);
			}else if(heroconfig.getQuality() == 5) {
				reward = RewardInfo.newBuilder();//橙色自选包
				reward.setItemid(34092);
				reward.setCount(25);
				rewards.addLoot(reward);
			}else if(heroconfig.getQuality() == 6) {
				reward = RewardInfo.newBuilder();//红色自选包
				reward.setItemid(34093);
				reward.setCount(30);
				rewards.addLoot(reward);
			}
			handleRewards(responseBuilder, user, rewards);
			
			Map<String, String> params = new HashMap<String, String>();
			params.put(LogString.SERVERID, "" + user.getServerId());
			params.put(LogString.USERID, "" + user.getId());
			params.put(LogString.HEROID, "" + heroInfo.getHeroId());
			params.put(LogString.LEVEL, "" + heroInfo.getLevel());
			params.put(LogString.GRADE, "" + heroInfo.getRank());
			params.put(LogString.STAR, "" + heroInfo.getStarLevel());
//			Hero heroconfig = heroService.getHero(heroInfo.getHeroId());
			params.put(LogString.RARE, "" + heroconfig.getQuality());
			
			logService.sendLog(params, LogString.LOGTYPE_HERORES);
		}else{
			isError = true;
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.HERO_HAS_FENJIE);
			responseBuilder.setErrorCommand(this.buildErrorCommand(ErrorConst.HERO_HAS_FENJIE));
			errorHeroBuilder.addHeroInfo(hero);
		}
		if(!isError){
			costInfoIds.add(hero.getInfoId());
			deleteHeroBuilder.addHeroInfo(hero);
		}
		
		userHeroService.delUserHero(user.getId(), costInfoIds);
		
		if(isError){
			responseBuilder.setDeleteHeroCommand(errorHeroBuilder.build());
			pushCommandService.pushUserInfoCommand(responseBuilder, user);
		}else{
			responseBuilder.setDeleteHeroCommand(deleteHeroBuilder.build());
		}
	}
	
	public void fenjieHero(RequestFenjieHeroCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseDeleteHeroCommand.Builder deleteHeroBuilder = ResponseDeleteHeroCommand.newBuilder();
		ResponseDeleteHeroCommand.Builder errorHeroBuilder = ResponseDeleteHeroCommand.newBuilder();
		List<FenjieHeroInfo> fenjieList = cmd.getFenjieHeroList();
		
		long userId = user.getId();
//		Map<Integer, Fenjie> fenjiemap = fenjieRedisService.getFenjieList();
//		int addCoin = 0;
//		long addExp = 0;
//		List<RewardBean> rewardList = new ArrayList<RewardBean>();
		Map<Integer, RewardInfo.Builder> rewardmap = new HashMap<Integer, RewardInfo.Builder>();
		Map<Integer, List<FenjieHeroInfo>> map = new HashMap<Integer, List<FenjieHeroInfo>>();
		for (FenjieHeroInfo hero : fenjieList) {
			List<FenjieHeroInfo> list = map.get(hero.getHeroId());
			if(list == null)
				list = new ArrayList<FenjieHeroInfo>();
			list.add(hero);
			map.put(hero.getHeroId(), list);
		}
		boolean isError = false;
		List<Long> costInfoIds = new ArrayList<Long>();
		for(Entry<Integer, List<FenjieHeroInfo>> entry : map.entrySet()){
			for (FenjieHeroInfo hero : entry.getValue()) {
				HeroInfoBean heroInfo = userHeroService.selectUserHero(userId, hero.getInfoId());
				if (heroInfo != null) {
					if(heroInfo.isLock()){
						logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.HERO_LOCKED);
						
						responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.HERO_LOCKED));
						return;
					}
					Hero heroconf = heroService.getHero(heroInfo.getHeroId());
					Fenjie fenjie = fenjieRedisService.getFenjie(heroconf.getQuality());
					RewardInfo.Builder reward = rewardmap.get(fenjie.getLootlist().getItemid());
					if(reward == null){
						reward = RewardInfo.newBuilder();
						reward.setItemid(fenjie.getLootlist().getItemid());
						rewardmap.put(fenjie.getLootlist().getItemid(), reward);
					}
					reward.setCount(reward.getCount() + fenjie.getLootlist().getCount());
//					if (heroInfo.getRank() > 0) {
//						rewardList = rewardService.mergeReward(rewardList, heroService.getHeroRareEquip(heroInfo));
//					}
					
//					addCoin += 1000 * heroInfo.getStarLevel() * heroInfo.getStarLevel();
//					if(heroInfo.getLevel() > 1)
//						addExp += heroService.getDeleteExp(heroInfo.getLevel());
//					addCoin += skillService.getResetCoin(heroInfo.getSkillInfoList());//升级技能消耗金币
					
					Map<String, String> params = new HashMap<String, String>();
					params.put(LogString.SERVERID, "" + user.getServerId());
					params.put(LogString.USERID, "" + user.getId());
					params.put(LogString.HEROID, "" + heroInfo.getHeroId());
					params.put(LogString.LEVEL, "" + heroInfo.getLevel());
					params.put(LogString.GRADE, "" + heroInfo.getRank());
					params.put(LogString.STAR, "" + heroInfo.getStarLevel());
					Hero heroconfig = heroService.getHero(heroInfo.getHeroId());
					params.put(LogString.RARE, "" + heroconfig.getQuality());
					
					logService.sendLog(params, LogString.LOGTYPE_HERORES);
				}else{
					isError = true;
					logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.HERO_HAS_FENJIE);
					responseBuilder.setErrorCommand(this.buildErrorCommand(ErrorConst.HERO_HAS_FENJIE));
					errorHeroBuilder.addHeroInfo(hero);
				}
				if(!isError){
					costInfoIds.add(hero.getInfoId());
					deleteHeroBuilder.addHeroInfo(hero);
				}
			}
		}
		
		userHeroService.delUserHero(user.getId(), costInfoIds);
		
		if(isError){
			responseBuilder.setDeleteHeroCommand(errorHeroBuilder.build());
			pushCommandService.pushUserInfoCommand(responseBuilder, user);
		}else{
//			if (addCoin > 0)
//				rewardList.add(RewardBean.init(RewardConst.COIN, addCoin));
//			
//			if (addExp > 0)
//				rewardList.add(RewardBean.init(RewardConst.EXP, addExp));
			
//			if (rewardList.size() > 0) {
			MultiReward.Builder rewards = MultiReward.newBuilder();
			for(RewardInfo.Builder builder: rewardmap.values())
				rewards.addLoot(builder);
			handleRewards(responseBuilder, user, rewards);
//			}
			responseBuilder.setDeleteHeroCommand(deleteHeroBuilder.build());
		}
	}

	public void heroSpUp(RequestHeroSpUpCommand cmd, Builder responseBuilder, UserBean user) {
		int heroId = cmd.getHeroId();
		long infoId = cmd.getInfoId();
		HeroInfoBean heroInfo = userHeroService.selectUserHero(user.getId(), infoId);
		Hero hero = heroService.getHero(heroId);
//		Star star = starService.getStar(7);
		int sp = skillService.getSP(heroInfo);
		final int count = Math.min(starService.getStar(heroInfo.getStarLevel()).getMaxskill() - sp - heroInfo.getSp(), cmd.getCount());
		List<UserEquipBean> equipList = new ArrayList<UserEquipBean>();
		if(count > 0) {
			int itemid = fenjieRedisService.getFenjie(hero.getQuality()).getLootlist().getItemid();
			UserEquipBean equip1 = userEquipService.selectUserEquip(user.getId(), itemid);
			if(equip1 == null)
				equip1 = UserEquipBean.init(user.getId(), itemid, 0);
			itemid = fenjieRedisService.getFenjie(0).getLootlist().getItemid();
			UserEquipBean equip2 = userEquipService.selectUserEquip(user.getId(), itemid);
			if(equip2 == null)
				equip2 = UserEquipBean.init(user.getId(), itemid, 0);
			equipList.add(equip1);
			equipList.add(equip2);
			if (count > equip1.getEquipCount() + equip2.getEquipCount())
				responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.ERROR_SKILL_STONE));
			else{
				if(count > equip1.getEquipCount()) {
					int leftcount = count - equip1.getEquipCount();
					equip1.setEquipCount(0);
					equip2.setEquipCount(equip2.getEquipCount()-leftcount);
					userEquipService.updateUserEquip(equip1);
					userEquipService.updateUserEquip(equip2);
				}else{
					equip1.setEquipCount(equip1.getEquipCount()-count);
					userEquipService.updateUserEquip(equip1);
				}
				heroInfo.setSp(heroInfo.getSp() + count);
				userHeroService.updateUserHero(heroInfo);
			}
		}
		ResponseHeroResultCommand.Builder builder = ResponseHeroResultCommand.newBuilder();
		builder.setHeroId(heroId);
		builder.addHeroInfo(heroInfo.buildHeroInfo());
		responseBuilder.setHeroResultCommand(builder.build());
		pushCommandService.pushUserEquipListCommand(responseBuilder, user, equipList);
	}
	
	public void resetHeroSkill(RequestResetHeroSkillCommand cmd, Builder responseBuilder, UserBean user) {
		int heroId = cmd.getHeroId();
		long infoId = cmd.getInfoId();
		long userId = user.getId();
		ResultConst result = ErrorConst.HERO_NOT_EXIST;
		
		HeroInfoBean heroInfo = userHeroService.selectUserHero(userId, infoId);
		if (heroInfo != null) 
			result = heroLevelUpService.resetHeroSkill(user, heroInfo);
		else{
            responseBuilder.setErrorCommand(buildErrorCommand((ErrorConst)result));
			ResponseDeleteHeroCommand.Builder deleteHeroBuilder = ResponseDeleteHeroCommand.newBuilder();
			FenjieHeroInfo.Builder herobuilder = FenjieHeroInfo.newBuilder();
			herobuilder.setHeroId(heroId);
			herobuilder.setInfoId(infoId);
			deleteHeroBuilder.addHeroInfo(herobuilder.build());
			responseBuilder.setDeleteHeroCommand(deleteHeroBuilder.build());
			return;
		}
		
		if (result instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
            pushCommandService.pushUserInfoCommand(responseBuilder, user);
		}else {
//			int resetCoin = skillService.getResetCoin(heroInfo.getSkillInfoList());
			heroInfo.setSp(heroInfo.getSp()+skillService.getSP(heroInfo));
			heroInfo.resetHeroSkill();
			userHeroService.updateUserHero(heroInfo);
			
//			List<RewardBean> rewardList = RewardBean.initRewardList(RewardConst.COIN, resetCoin);
//			handleRewards(responseBuilder, user, rewardList);
			pushCommandService.pushUserInfoCommand(responseBuilder, user);
		}
		ResponseHeroResultCommand.Builder builder = ResponseHeroResultCommand.newBuilder();
		builder.setHeroId(heroId);
		builder.addHeroInfo(heroInfo.buildHeroInfo());
		responseBuilder.setHeroResultCommand(builder.build());
	}

	public void lockHero(RequestLockHeroCommand cmd, Builder responseBuilder, UserBean user) {
		int heroId = cmd.getHeroId();
		long infoId = cmd.getInfoId();
		HeroInfoBean heroInfo = userHeroService.selectUserHero(user.getId(), infoId);
		
		heroInfo = userHeroService.selectUserHero(user.getId(), infoId);
		if (heroInfo != null){
			heroInfo.setLock(cmd.getIsLock());
			userHeroService.updateUserHero(heroInfo);
		}else{
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.HERO_NOT_EXIST);
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.HERO_NOT_EXIST));
			ResponseDeleteHeroCommand.Builder deleteHeroBuilder = ResponseDeleteHeroCommand.newBuilder();
			FenjieHeroInfo.Builder herobuilder = FenjieHeroInfo.newBuilder();
			herobuilder.setHeroId(heroId);
			herobuilder.setInfoId(infoId);
			deleteHeroBuilder.addHeroInfo(herobuilder.build());
			responseBuilder.setDeleteHeroCommand(deleteHeroBuilder.build());
			return;
		}
		ResponseHeroResultCommand.Builder builder = ResponseHeroResultCommand.newBuilder();
		builder.setHeroId(heroId);
		builder.addHeroInfo(heroInfo.buildHeroInfo());
		responseBuilder.setHeroResultCommand(builder.build());
	}
	
	public void buyHeroPackage(RequestBuyHeroPackageCommand cmd, Builder responseBuilder, UserBean user) {
		if (user.getHeroLimit() + BUY_HERO_PACKAGE_COUNT > HERO_PACKAGE_LIMIT) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.HERO_LIMIT_ERROR);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.HERO_LIMIT_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
		} else if (!costService.cost(user, RewardConst.JEWEL, BUY_HERO_PACKAGE_COST)) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH_JEWEL);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_ENOUGH_JEWEL);
            responseBuilder.setErrorCommand(errorCommand);
		} else {
			user.setHeroLimit(user.getHeroLimit() + BUY_HERO_PACKAGE_COUNT);
			userService.updateUser(user);
			responseBuilder.setMessageCommand(this.buildMessageCommand(SuccessConst.PURCHASE_SUCCESS));
		}
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}
	
	public void submitComposeSkill(RequestSubmitComposeSkillCommand cmd, Builder responseBuilder, UserBean user) {
		user.setComposeSkill(cmd.getComposeSkill());
		user.setCurrentTeamid(TypeTranslatedUtil.stringToLong(cmd.getComposeSkill()));
		userTeamService.changeUserTeam(user, TypeTranslatedUtil.stringToInt(cmd.getComposeSkill()));
		
//		UserTeamBean userTeam = userTeamService.getUserTeam(user.getId(), user.getCurrentTeamid());
//		List<EquipRecord> talentEquipRecords = UserTeamBean.buildEquipRecord(userTeam.getTalentEquip());
//		List<EquipRecord> heroEquipRecords = UserTeamBean.buildEquipRecord(userTeam.getHeroEquip());
		
//		UserTalent userTalent = talentService.updateUserTalentEquip(user, talentEquipRecords);
//		if (userTalent != null)
//			pushCommandService.pushUserTalent(responseBuilder, user, userTalent);
//		
//		List<HeroInfoBean> heroList = heroLevelUpService.updateHerosEquip(user, heroEquipRecords);
//		pushCommandService.pushUserHeroListCommand(responseBuilder, user, heroList);
		
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
		responseBuilder.setMessageCommand(this.buildMessageCommand(SuccessConst.SUBMIT_SUCCESS));
	}
}
