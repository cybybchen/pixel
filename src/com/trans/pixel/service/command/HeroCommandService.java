package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.FenjieHeroInfo;
import com.trans.pixel.protoc.Commands.RequestAddHeroEquipCommand;
import com.trans.pixel.protoc.Commands.RequestBuyHeroPackageCommand;
import com.trans.pixel.protoc.Commands.RequestEquipLevelUpCommand;
import com.trans.pixel.protoc.Commands.RequestFenjieHeroCommand;
import com.trans.pixel.protoc.Commands.RequestFenjieHeroEquipCommand;
import com.trans.pixel.protoc.Commands.RequestHeroLevelUpCommand;
import com.trans.pixel.protoc.Commands.RequestLockHeroCommand;
import com.trans.pixel.protoc.Commands.RequestResetHeroSkillCommand;
import com.trans.pixel.protoc.Commands.RequestSubmitComposeSkillCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseDeleteHeroCommand;
import com.trans.pixel.protoc.Commands.ResponseHeroResultCommand;
import com.trans.pixel.service.CostService;
import com.trans.pixel.service.EquipService;
import com.trans.pixel.service.HeroLevelUpService;
import com.trans.pixel.service.HeroService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.SkillService;
import com.trans.pixel.service.UserHeroService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class HeroCommandService extends BaseCommandService {

	private static final int BUY_HERO_PACKAGE_COST = 50;
	private static final int BUY_HERO_PACKAGE_COUNT = 5;
	private static final int HERO_PACKAGE_LIMIT = 500;
	
	private static final int RESET_HERO_SKILL_COST = 10000;
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
	private RewardService rewardService;
	@Resource
	private CostService costService;
	@Resource
	private UserService userService;
	@Resource
	private HeroService heroService;
	
	public void heroLevelUp(RequestHeroLevelUpCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseHeroResultCommand.Builder builder = ResponseHeroResultCommand.newBuilder();
		int levelUpType = cmd.getLevelUpType();
		int heroId = cmd.getHeroId();
		long infoId = cmd.getInfoId();
		long userId = user.getId();
		int skillId = 0;
		if (cmd.hasSkillId())
			skillId = cmd.getSkillId();
		List<Long> costInfoIds = cmd.getCostInfoIdList();
		HeroInfoBean heroInfo = userHeroService.selectUserHero(userId, infoId);
		ResultConst result = ErrorConst.HERO_NOT_EXIST;
		
		if (heroInfo != null) {
			result = heroLevelUpService.levelUpResult(user, heroInfo, levelUpType, skillId, costInfoIds);
			if (result instanceof SuccessConst)
				skillService.unlockHeroSkill(heroId, heroInfo);
		}
		
		if (result instanceof ErrorConst) {
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		if (result instanceof SuccessConst) {
			userHeroService.updateUserHero(heroInfo);
			builder.setHeroId(heroId);
			builder.addHeroInfo(heroInfo.buildHeroInfo());
			responseBuilder.setHeroResultCommand(builder.build());
			
			if (costInfoIds.size() > 0){
				ResponseDeleteHeroCommand.Builder deleteHeroBuilder = ResponseDeleteHeroCommand.newBuilder();
				for(long costId : costInfoIds){
					FenjieHeroInfo.Builder herobuilder = FenjieHeroInfo.newBuilder();
					herobuilder.setHeroId(heroId);
					herobuilder.setInfoId(costId);
					deleteHeroBuilder.addHeroInfo(herobuilder.build());
				}
				responseBuilder.setDeleteHeroCommand(deleteHeroBuilder.build());
			}
			
			pushCommandService.pushUserInfoCommand(responseBuilder, user);
		}
	}
	
	public void heroAddEquip(RequestAddHeroEquipCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseHeroResultCommand.Builder builder = ResponseHeroResultCommand.newBuilder();
		int heroId = cmd.getHeroId();
		long infoId = cmd.getInfoId();
		int armId = cmd.getArmId();
		long userId = user.getId();
		
		ResultConst result = ErrorConst.HERO_NOT_EXIST;
		HeroInfoBean heroInfo = userHeroService.selectUserHero(userId, infoId);;
		List<UserEquipBean> userEquipList = new ArrayList<UserEquipBean>();
	
		if (heroInfo != null) 
			result = heroLevelUpService.addHeroEquip(user, heroInfo, heroId, armId, userEquipList);
		
		if (result instanceof ErrorConst) {
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		if (result instanceof SuccessConst) {
			userHeroService.updateUserHero(heroInfo);
			builder.setHeroId(heroId);
			builder.addHeroInfo(heroInfo.buildHeroInfo());
			responseBuilder.setHeroResultCommand(builder.build());
			
			if (userEquipList != null && userEquipList.size() > 0)
				pushCommandService.pushUserEquipListCommand(responseBuilder, user, userEquipList);
		}
	}
	
	public void fenjieHeroEquip(RequestFenjieHeroEquipCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseHeroResultCommand.Builder builder = ResponseHeroResultCommand.newBuilder();
		int heroId = cmd.getHeroId();
		long infoId = cmd.getInfoId();
		int armId = cmd.getArmId();
		long userId = user.getId();

		HeroInfoBean heroInfo = userHeroService.selectUserHero(userId, infoId);;
		int equipId = 0;
		if (heroInfo != null) 
			equipId = heroLevelUpService.delHeroEquip(heroInfo, armId, heroId);
		
		if (equipId == 0) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.EQUIP_FENJIE_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		if (equipId > 0) {
			userHeroService.updateUserHero(heroInfo);
			builder.setHeroId(heroId);
			builder.addHeroInfo(heroInfo.buildHeroInfo());
			responseBuilder.setHeroResultCommand(builder.build());
			
			List<RewardBean> rewardList = equipService.fenjieHeroEquip(user, equipId, 1);
			if (rewardList == null || rewardList.size() == 0) {
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.EQUIP_FENJIE_ERROR);
	            responseBuilder.setErrorCommand(errorCommand);
	            return;
			}
			
			rewardService.doRewards(user, rewardList);
			
			pushCommandService.pushRewardCommand(responseBuilder, user, rewardList);
		}
	}
	
	public void fenjieHero(RequestFenjieHeroCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseDeleteHeroCommand.Builder deleteHeroBuilder = ResponseDeleteHeroCommand.newBuilder();
		ResponseDeleteHeroCommand.Builder errorHeroBuilder = ResponseDeleteHeroCommand.newBuilder();
		List<FenjieHeroInfo> fenjieList = cmd.getFenjieHeroList();
		
		long userId = user.getId();
		int addCoin = 0;
		int addExp = 0;
		List<RewardBean> rewardList = new ArrayList<RewardBean>();
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
			int heroId = entry.getKey();
			
			HeroInfoBean bean = HeroInfoBean.initHeroInfo(heroService.getHero(heroId), 1);
			for (FenjieHeroInfo hero : entry.getValue()) {
				HeroInfoBean heroInfo = userHeroService.selectUserHero(userId, hero.getInfoId());
				if (heroInfo != null) {
					if(heroInfo.isLock()){
						responseBuilder.setErrorCommand(this.buildErrorCommand(ErrorConst.HERO_LOCKED));
						return;
					}
					
					if(!bean.getEquipInfo().equals(heroInfo.getEquipInfo())){
						for (String equipIdStr : heroInfo.equipIds()) {
							int equipId = TypeTranslatedUtil.stringToInt(equipIdStr);
							if (equipId > 0)
								rewardList = rewardService.mergeReward(rewardList, equipService.fenjieHeroEquip(user, equipId, 1));
						}
					}
					addCoin += 1000 * heroInfo.getStarLevel() * heroInfo.getStarLevel();
					if(heroInfo.getLevel() > 1)
						addExp += heroService.getHeroUpgrade(heroInfo.getLevel()).getExp();
				}else{
					isError = true;
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
			if (addCoin > 0)
				rewardList.add(RewardBean.init(RewardConst.COIN, addCoin));
			
			if (addExp > 0)
				rewardList.add(RewardBean.init(RewardConst.EXP, addExp));
			
			if (rewardList.size() > 0) {
				rewardService.doRewards(user, rewardList);
				pushCommandService.pushRewardCommand(responseBuilder, user, rewardList);
			}
			responseBuilder.setDeleteHeroCommand(deleteHeroBuilder.build());
		}
	}
	
	public void equipLevelup(RequestEquipLevelUpCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseHeroResultCommand.Builder builder = ResponseHeroResultCommand.newBuilder();
		int heroId = cmd.getHeroId();
		long infoId = cmd.getInfoId();
		int armId = cmd.getArmId();
		int levelUpId = cmd.getLevelUpId();
		long userId = user.getId();
		ResultConst result = ErrorConst.HERO_NOT_EXIST;
		HeroInfoBean heroInfo = null;
		List<UserEquipBean> userEquipList = new ArrayList<UserEquipBean>();
		
		heroInfo = userHeroService.selectUserHero(userId, infoId);
		if (heroInfo != null) 
			result = heroLevelUpService.equipLevelUp(user, heroInfo, armId, levelUpId, userEquipList);
		
		if (result instanceof ErrorConst) {
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		if (result instanceof SuccessConst) {
			userHeroService.updateUserHero(heroInfo);
			builder.setHeroId(heroId);
			builder.addHeroInfo(heroInfo.buildHeroInfo());
			responseBuilder.setHeroResultCommand(builder.build());
			
			pushCommandService.pushUserEquipListCommand(responseBuilder, user, userEquipList);
		}
	}
	
	public void resetHeroSkill(RequestResetHeroSkillCommand cmd, Builder responseBuilder, UserBean user) {
//		if (!costService.costAndUpdate(user, RewardConst.COIN, RESET_HERO_SKILL_COST)) {
//			ErrorConst error = ErrorConst.NOT_ENOUGH_COIN;
//			ErrorCommand errorCommand = buildErrorCommand(error);
//            responseBuilder.setErrorCommand(errorCommand);
//			return;	
//		}
		
		ResponseHeroResultCommand.Builder builder = ResponseHeroResultCommand.newBuilder();
		int heroId = cmd.getHeroId();
		long infoId = cmd.getInfoId();
		long userId = user.getId();
		ResultConst result = ErrorConst.HERO_NOT_EXIST;
		
		HeroInfoBean heroInfo = userHeroService.selectUserHero(userId, infoId);
		if (heroInfo != null) 
			result = heroLevelUpService.resetHeroSkill(user, heroInfo);
		
		if (result instanceof ErrorConst) {
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		if (result instanceof SuccessConst) {
			int resetCoin = skillService.getResetCoin(heroInfo.getSkillInfoList());
			heroInfo.resetHeroSkill();
			userHeroService.updateUserHero(heroInfo);
			builder.setHeroId(heroId);
			builder.addHeroInfo(heroInfo.buildHeroInfo());
			responseBuilder.setHeroResultCommand(builder.build());
			
			List<RewardBean> rewardList = RewardBean.initRewardList(RewardConst.COIN, resetCoin);
			rewardService.doRewards(user, rewardList);
			pushCommandService.pushRewardCommand(responseBuilder, user, rewardList);
		}
	}

	public void lockHero(RequestLockHeroCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseHeroResultCommand.Builder builder = ResponseHeroResultCommand.newBuilder();
		int heroId = cmd.getHeroId();
		long infoId = cmd.getInfoId();
		HeroInfoBean heroInfo = userHeroService.selectUserHero(user.getId(), infoId);
		
		heroInfo = userHeroService.selectUserHero(user.getId(), infoId);
		if (heroInfo != null)
			heroInfo.setLock(cmd.getIsLock());
		
		if(heroInfo == null)
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.HERO_NOT_EXIST));
		else{
			userHeroService.updateUserHero(heroInfo);
			
			builder.setHeroId(heroId);
			builder.addHeroInfo(heroInfo.buildHeroInfo());
			responseBuilder.setHeroResultCommand(builder.build());
		}
	}
	
	public void buyHeroPackage(RequestBuyHeroPackageCommand cmd, Builder responseBuilder, UserBean user) {
		if (!costService.cost(user, RewardConst.JEWEL, BUY_HERO_PACKAGE_COST)) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_ENOUGH_JEWEL);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		if (user.getHeroLimit() + BUY_HERO_PACKAGE_COUNT > HERO_PACKAGE_LIMIT) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.HERO_LIMIT_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		user.setHeroLimit(user.getHeroLimit() + BUY_HERO_PACKAGE_COUNT);
		userService.updateUser(user);
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
		responseBuilder.setMessageCommand(this.buildMessageCommand(SuccessConst.PURCHASE_SUCCESS));
	}
	
	public void submitComposeSkill(RequestSubmitComposeSkillCommand cmd, Builder responseBuilder, UserBean user) {
		user.setComposeSkill(cmd.getComposeSkill());
		userService.updateUser(user);
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
		responseBuilder.setMessageCommand(this.buildMessageCommand(SuccessConst.SUBMIT_SUCCESS));
	}
}
