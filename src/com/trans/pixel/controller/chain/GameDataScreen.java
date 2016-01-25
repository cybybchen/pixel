package com.trans.pixel.controller.chain;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.RequestAddFriendCommand;
import com.trans.pixel.protoc.Commands.RequestAddHeroEquipCommand;
import com.trans.pixel.protoc.Commands.RequestAttackLadderModeCommand;
import com.trans.pixel.protoc.Commands.RequestAreaCommand;
import com.trans.pixel.protoc.Commands.RequestAttackBossCommand;
import com.trans.pixel.protoc.Commands.RequestAttackMonsterCommand;
import com.trans.pixel.protoc.Commands.RequestAttackRelativeCommand;
import com.trans.pixel.protoc.Commands.RequestAttackResourceCommand;
import com.trans.pixel.protoc.Commands.RequestAttackResourceMineCommand;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestDeleteMailCommand;
import com.trans.pixel.protoc.Commands.RequestEquipLevelUpCommand;
import com.trans.pixel.protoc.Commands.RequestGetLadderRankListCommand;
import com.trans.pixel.protoc.Commands.RequestGetUserMailListCommand;
import com.trans.pixel.protoc.Commands.RequestHeroLevelUpCommand;
import com.trans.pixel.protoc.Commands.RequestLevelLootResultCommand;
import com.trans.pixel.protoc.Commands.RequestLevelLootStartCommand;
import com.trans.pixel.protoc.Commands.RequestLevelResultCommand;
import com.trans.pixel.protoc.Commands.RequestLevelStartCommand;
import com.trans.pixel.protoc.Commands.RequestLootResultCommand;
import com.trans.pixel.protoc.Commands.RequestLotteryEquipCommand;
import com.trans.pixel.protoc.Commands.RequestLotteryHeroCommand;
import com.trans.pixel.protoc.Commands.RequestReadMailCommand;
import com.trans.pixel.protoc.Commands.RequestReceiveFriendCommand;
import com.trans.pixel.protoc.Commands.RequestRefreshRelatedUserCommand;
import com.trans.pixel.protoc.Commands.RequestUpdateTeamCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.command.AccountCommandService;
import com.trans.pixel.service.command.FriendCommandService;
import com.trans.pixel.service.command.AreaCommandService;
import com.trans.pixel.service.command.HeroLevelUpCommandService;
import com.trans.pixel.service.command.LadderCommandService;
import com.trans.pixel.service.command.LevelCommandService;
import com.trans.pixel.service.command.LoginCommandService;
import com.trans.pixel.service.command.LootCommandService;
import com.trans.pixel.service.command.LotteryEquipCommandService;
import com.trans.pixel.service.command.LotteryHeroCommandService;
import com.trans.pixel.service.command.MailCommandService;
import com.trans.pixel.service.command.PvpCommandService;
import com.trans.pixel.service.command.TeamCommandService;
//add import here

@Service
public class GameDataScreen extends RequestScreen {
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
	private LotteryHeroCommandService lotteryCommandService;
	@Resource
	private MailCommandService mailCommandService;
	@Resource
	private FriendCommandService friendCommandService;
	@Resource
	private LadderCommandService ladderCommandService;
	@Resource
	private LotteryEquipCommandService lotteryEquipCommandService;
	@Resource
	private AreaCommandService areaCommandService;
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

	@Override
	protected boolean handleCommand(RequestGetLadderRankListCommand cmd,
			Builder responseBuilder, UserBean user) {
		ladderCommandService.handleGetLadderRankListCommand(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestAttackLadderModeCommand cmd,
			Builder responseBuilder, UserBean user) {
		ladderCommandService.attackLadderMode(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestGetUserMailListCommand cmd,
			Builder responseBuilder, UserBean user) {
		mailCommandService.handleGetUserMailListCommand(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestReadMailCommand cmd,
			Builder responseBuilder, UserBean user) {
		mailCommandService.handleReadMailCommand(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestDeleteMailCommand cmd,
			Builder responseBuilder, UserBean user) {
		mailCommandService.handleDeleteMailCommand(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestAddFriendCommand cmd,
			Builder responseBuilder, UserBean user) {
		friendCommandService.handleAddFriendCommand(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestReceiveFriendCommand cmd,
			Builder responseBuilder, UserBean user) {
		friendCommandService.handleReceiveFriendCommand(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestLotteryEquipCommand cmd,
			Builder responseBuilder, UserBean user) {
		lotteryEquipCommandService.lotteryEquip(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestEquipLevelUpCommand cmd,
			Builder responseBuilder, UserBean user) {
		heroLevelUpCommandService.equipLevelup(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestAreaCommand cmd,
			Builder responseBuilder, UserBean user) {
		areaCommandService.Area(responseBuilder, user);
		return false;
	}

	@Override
	protected boolean handleCommand(RequestAttackBossCommand cmd,
			Builder responseBuilder, UserBean user) {
		areaCommandService.AttackBoss(cmd, responseBuilder, user);
		return false;
	}

	@Override
	protected boolean handleCommand(RequestAttackMonsterCommand cmd,
			Builder responseBuilder, UserBean user) {
		areaCommandService.AttackMonster(cmd, responseBuilder, user);
		return false;
	}

	@Override
	protected boolean handleCommand(RequestAttackResourceCommand cmd,
			Builder responseBuilder, UserBean user) {
		areaCommandService.AttackResource(cmd, responseBuilder, user);
		return false;
	}

	@Override
	protected boolean handleCommand(RequestAttackResourceMineCommand cmd,
			Builder responseBuilder, UserBean user) {
		areaCommandService.AttackResourceMine(cmd, responseBuilder, user);
		return false;
	}
	//add handleCommand here

}
