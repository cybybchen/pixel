package com.trans.pixel.controller.chain;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.RequestAddHeroEquipCommand;
import com.trans.pixel.protoc.Commands.RequestAttackRelativeCommand;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestHeroLevelUpCommand;
import com.trans.pixel.protoc.Commands.RequestLevelLootResultCommand;
import com.trans.pixel.protoc.Commands.RequestLevelLootStartCommand;
import com.trans.pixel.protoc.Commands.RequestLevelResultCommand;
import com.trans.pixel.protoc.Commands.RequestLevelStartCommand;
import com.trans.pixel.protoc.Commands.RequestLootResultCommand;
import com.trans.pixel.protoc.Commands.RequestLotteryHeroCommand;
import com.trans.pixel.protoc.Commands.RequestRefreshRelatedUserCommand;
import com.trans.pixel.protoc.Commands.RequestUpdateTeamCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseUserInfoCommand;
import com.trans.pixel.service.command.AccountCommandService;
import com.trans.pixel.service.command.HeroLevelUpCommandService;
import com.trans.pixel.service.command.LevelCommandService;
import com.trans.pixel.service.command.LoginCommandService;
import com.trans.pixel.service.command.LootCommandService;
import com.trans.pixel.service.command.LotteryCommandService;
import com.trans.pixel.service.command.PvpCommandService;
import com.trans.pixel.service.command.TeamCommandService;

@Service
public class GameDataScreen extends RequestScreen {
	private static final Logger log = LoggerFactory.getLogger(GameDataScreen.class);
	
	@Resource
	private AccountCommandService accountCommandService;
	@Resource
	private LoginCommandService loginCommandService;
	@Resource
	private LevelCommandService levelCommandService;
	@Resource
	private PvpCommandService pvpCommandService;
	@Resource
	private TeamCommandService teamCommandService;
	@Resource
	private LootCommandService lootCommandService;
	@Resource
	private HeroLevelUpCommandService heroLevelUpCommandService;
	@Resource
	private LotteryCommandService lotteryCommandService;
	@Override
	protected boolean handleRegisterCommand(RequestCommand cmd,
			Builder responseBuilder) {
		accountCommandService.register(cmd, responseBuilder);
		return true;
	}

	@Override
	protected boolean handleLoginCommand(RequestCommand cmd,
			Builder responseBuilder) {	
		loginCommandService.login(cmd, responseBuilder);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestLevelResultCommand cmd,
			Builder responseBuilder, UserBean user) {
		levelCommandService.levelResultFirstTime(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestLevelStartCommand cmd,
			Builder responseBuilder, UserBean user) {
		levelCommandService.levelStartFirstTime(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestLevelLootStartCommand cmd,
			Builder responseBuilder, UserBean user) {
		levelCommandService.levelLootStart(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestLevelLootResultCommand cmd,
			Builder responseBuilder, UserBean user) {
		levelCommandService.levelLootResult(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestAttackRelativeCommand cmd,
			Builder responseBuilder, UserBean user) {
		pvpCommandService.attackRelatedUser(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestLootResultCommand cmd,
			Builder responseBuilder, UserBean user) {
		lootCommandService.lootResult(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestHeroLevelUpCommand cmd,
			Builder responseBuilder, UserBean user) {
		heroLevelUpCommandService.heroLevelUp(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestAddHeroEquipCommand cmd,
			Builder responseBuilder, UserBean user) {
		heroLevelUpCommandService.heroAddEquip(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestUpdateTeamCommand cmd,
			Builder responseBuilder, UserBean user) {
		teamCommandService.updateUserTeam(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestRefreshRelatedUserCommand cmd,
			Builder responseBuilder, UserBean user) {
		pvpCommandService.refreshRelatedUser(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestLotteryHeroCommand cmd,
			Builder responseBuilder, UserBean user) {
		lotteryCommandService.lotteryHero(cmd, responseBuilder, user);
		return true;
	}

}
