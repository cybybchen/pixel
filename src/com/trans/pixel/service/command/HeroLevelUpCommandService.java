package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserHeroBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.RequestAddHeroEquipCommand;
import com.trans.pixel.protoc.Commands.RequestEquipLevelUpCommand;
import com.trans.pixel.protoc.Commands.RequestFenjieHeroCommand;
import com.trans.pixel.protoc.Commands.RequestFenjieHeroEquipCommand;
import com.trans.pixel.protoc.Commands.RequestHeroLevelUpCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseFenjieEquipCommand;
import com.trans.pixel.protoc.Commands.ResponseHeroResultCommand;
import com.trans.pixel.service.EquipService;
import com.trans.pixel.service.HeroLevelUpService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.SkillService;
import com.trans.pixel.service.UserHeroService;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class HeroLevelUpCommandService extends BaseCommandService {

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
	
	public void heroLevelUp(RequestHeroLevelUpCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseHeroResultCommand.Builder builder = ResponseHeroResultCommand.newBuilder();
		int levelUpType = cmd.getLevelUpType();
		int heroId = cmd.getHeroId();
		int infoId = cmd.getInfoId();
		long userId = user.getId();
		int skillId = 0;
		if (cmd.hasSkillId())
			skillId = cmd.getSkillId();
		List<Integer> costInfoIds = cmd.getCostInfoIdList();
		UserHeroBean userHero = userHeroService.selectUserHero(userId, heroId);
		ResultConst result = ErrorConst.HERO_NOT_EXIST;
		HeroInfoBean heroInfo = null;
		
		if (userHero != null) {
			heroInfo = userHero.getHeroInfoByInfoId(infoId);
			if (heroInfo != null) {
				result = heroLevelUpService.levelUpResult(user, heroInfo, levelUpType, skillId, costInfoIds, userHero);
				if (result instanceof SuccessConst)
					skillService.unlockHeroSkill(heroId, heroInfo);
			}
		}
		
		if (result instanceof ErrorConst) {
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		if (result instanceof SuccessConst) {
			userHero.updateHeroInfo(heroInfo);
			userHeroService.updateUserHero(userHero);
			builder.setHeroId(heroId);
			builder.setHeroInfo(heroInfo.buildHeroInfo());
			responseBuilder.setHeroResultCommand(builder.build());
			pushCommandService.pushUserHeroListCommand(responseBuilder, user);
		}
	}
	
	public void heroAddEquip(RequestAddHeroEquipCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseHeroResultCommand.Builder builder = ResponseHeroResultCommand.newBuilder();
		int heroId = cmd.getHeroId();
		int infoId = cmd.getInfoId();
		int armId = cmd.getArmId();
		long userId = user.getId();
		UserHeroBean userHero = userHeroService.selectUserHero(userId, heroId);
		ResultConst result = ErrorConst.HERO_NOT_EXIST;
		HeroInfoBean heroInfo = null;
		
		if (userHero != null) {
			heroInfo = userHero.getHeroInfoByInfoId(infoId);
			if (heroInfo != null) 
				result = heroLevelUpService.addHeroEquip(user, heroInfo, heroId, armId);
		}
		
		if (result instanceof ErrorConst) {
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		if (result instanceof SuccessConst) {
			userHero.updateHeroInfo(heroInfo);
			userHeroService.updateUserHero(userHero);
			builder.setHeroId(heroId);
			builder.setHeroInfo(heroInfo.buildHeroInfo());
			responseBuilder.setHeroResultCommand(builder.build());
			pushCommandService.pushUserHeroListCommand(responseBuilder, user);
			pushCommandService.pushUserEquipListCommand(responseBuilder, user);
		}
	}
	
	public void fenjieHeroEquip(RequestFenjieHeroEquipCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseHeroResultCommand.Builder builder = ResponseHeroResultCommand.newBuilder();
		int heroId = cmd.getHeroId();
		int infoId = cmd.getInfoId();
		int armId = cmd.getArmId();
		long userId = user.getId();
		UserHeroBean userHero = userHeroService.selectUserHero(userId, heroId);
		HeroInfoBean heroInfo = null;
		
		int equipId = 0;
		if (userHero != null) {
			heroInfo = userHero.getHeroInfoByInfoId(infoId);
			if (heroInfo != null) 
				equipId = heroLevelUpService.delHeroEquip(heroInfo, armId);
		}
		
		if (equipId == 0) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.EQUIP_FENJIE_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		if (equipId > 0) {
			userHero.updateHeroInfo(heroInfo);
			userHeroService.updateUserHero(userHero);
			builder.setHeroId(heroId);
			builder.setHeroInfo(heroInfo.buildHeroInfo());
			responseBuilder.setHeroResultCommand(builder.build());
			
			ResponseFenjieEquipCommand.Builder fenjieBuilder = ResponseFenjieEquipCommand.newBuilder();
			List<RewardBean> rewardList = equipService.fenjieHeroEquip(user, equipId, 1);
			if (rewardList == null || rewardList.size() == 0) {
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.EQUIP_FENJIE_ERROR);
	            responseBuilder.setErrorCommand(errorCommand);
	            return;
			}
			
			rewardService.doRewards(user, rewardList);
			
			fenjieBuilder.addAllReward(RewardBean.buildRewardInfoList(rewardList));
			responseBuilder.setFenjieEquipCommand(fenjieBuilder.build());
			pushCommandService.pushUserEquipListCommand(responseBuilder, user);
			pushCommandService.pushUserHeroListCommand(responseBuilder, user);
		}
	}
	
	public void fenjieHero(RequestFenjieHeroCommand cmd, Builder responseBuilder, UserBean user) {
		int heroId = cmd.getHeroId();
		int infoId = cmd.getInfoId();
		
		long userId = user.getId();
		UserHeroBean userHero = userHeroService.selectUserHero(userId, heroId);
		HeroInfoBean heroInfo = null;
		
		String[] equipIds = null;
		int addCoin = 0;
		if (userHero != null) {
			heroInfo = userHero.getHeroInfoByInfoId(infoId);
			if (heroInfo != null) {
				equipIds = heroInfo.getEquipIds();
				addCoin = 1000 * heroInfo.getStarLevel() * heroInfo.getStarLevel();
			}
		}
		
		if (addCoin == 0) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.EQUIP_FENJIE_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		ResponseFenjieEquipCommand.Builder fenjieBuilder = ResponseFenjieEquipCommand.newBuilder();
		List<RewardBean> rewardList = new ArrayList<RewardBean>();
		for (String equipIdStr : equipIds) {
			int equipId = TypeTranslatedUtil.stringToInt(equipIdStr);
			if (equipId > 0)
				rewardList = equipService.fenjieHeroEquip(user, equipId, 1);
		}
		if (rewardList != null && rewardList.size() > 0) {
			rewardService.doRewards(user, rewardList);
			fenjieBuilder.addAllReward(RewardBean.buildRewardInfoList(rewardList));
		}
		
		List<Integer> costInfoIds = new ArrayList<Integer>();
		costInfoIds.add(infoId);
		userHero.delHeros(costInfoIds);
		userHeroService.updateUserHero(userHero);
		
		rewardService.doReward(user, RewardConst.COIN, addCoin);
		
		responseBuilder.setFenjieEquipCommand(fenjieBuilder.build());
		pushCommandService.pushUserHeroListCommand(responseBuilder, user);
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}
	
	public void equipLevelup(RequestEquipLevelUpCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseHeroResultCommand.Builder builder = ResponseHeroResultCommand.newBuilder();
		int heroId = cmd.getHeroId();
		int infoId = cmd.getInfoId();
		int armId = cmd.getArmId();
		int levelUpId = cmd.getLevelUpId();
		long userId = user.getId();
		UserHeroBean userHero = userHeroService.selectUserHero(userId, heroId);
		ResultConst result = ErrorConst.HERO_NOT_EXIST;
		HeroInfoBean heroInfo = null;
		
		if (userHero != null) {
			heroInfo = userHero.getHeroInfoByInfoId(infoId);
			if (heroInfo != null) 
				result = heroLevelUpService.equipLevelUp(user, heroInfo, armId, levelUpId);
		}
		
		if (result instanceof ErrorConst) {
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		if (result instanceof SuccessConst) {
			userHero.updateHeroInfo(heroInfo);
			userHeroService.updateUserHero(userHero);
			builder.setHeroId(heroId);
			builder.setHeroInfo(heroInfo.buildHeroInfo());
			responseBuilder.setHeroResultCommand(builder.build());
			pushCommandService.pushUserHeroListCommand(responseBuilder, user);
			pushCommandService.pushUserEquipListCommand(responseBuilder, user);
		}
	}
}
