package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserHeroBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.RequestAddHeroEquipCommand;
import com.trans.pixel.protoc.Commands.RequestEquipLevelUpCommand;
import com.trans.pixel.protoc.Commands.RequestHeroLevelUpCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseHeroResultCommand;
import com.trans.pixel.service.HeroLevelUpService;
import com.trans.pixel.service.UserHeroService;

@Service
public class HeroLevelUpCommandService extends BaseCommandService {

	@Resource
	private HeroLevelUpService heroLevelUpService;
	@Resource
	private UserHeroService userHeroService;
	
	public void heroLevelUp(RequestHeroLevelUpCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseHeroResultCommand.Builder builder = ResponseHeroResultCommand.newBuilder();
		int levelUpType = cmd.getLevelUpType();
		int heroId = cmd.getHeroId();
		int infoId = cmd.getInfoId();
		long userId = user.getId();
		UserHeroBean userHero = userHeroService.selectUserHero(userId, heroId);
		ResultConst result = ErrorConst.HERO_NOT_EXIST;
		HeroInfoBean heroInfo = null;
		
		if (userHero != null) {
			heroInfo = userHero.getHeroInfoByInfoId(infoId);
			if (heroInfo != null) 
				result = heroLevelUpService.levelUpResult(user, heroInfo, levelUpType);
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
			builder.setHeroId(infoId);
			builder.setHeroInfo(heroInfo.buildHeroInfo());
			responseBuilder.setHeroResultCommand(builder.build());
		}
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
			builder.setHeroId(infoId);
			builder.setHeroInfo(heroInfo.buildHeroInfo());
			responseBuilder.setHeroResultCommand(builder.build());
		}
	}
}
