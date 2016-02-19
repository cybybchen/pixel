package com.trans.pixel.controller.chain;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.RequestAddFriendCommand;
import com.trans.pixel.protoc.Commands.RequestAddHeroEquipCommand;
import com.trans.pixel.protoc.Commands.RequestAddTeamCommand;
import com.trans.pixel.protoc.Commands.RequestApplyUnionCommand;
import com.trans.pixel.protoc.Commands.RequestAreaCommand;
import com.trans.pixel.protoc.Commands.RequestAttackBossCommand;
import com.trans.pixel.protoc.Commands.RequestAttackLadderModeCommand;
import com.trans.pixel.protoc.Commands.RequestAttackMonsterCommand;
import com.trans.pixel.protoc.Commands.RequestAttackRelativeCommand;
import com.trans.pixel.protoc.Commands.RequestAttackResourceCommand;
import com.trans.pixel.protoc.Commands.RequestAttackResourceMineCommand;
import com.trans.pixel.protoc.Commands.RequestAttackUnionCommand;
import com.trans.pixel.protoc.Commands.RequestBlackShopCommand;
import com.trans.pixel.protoc.Commands.RequestBlackShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestBlackShopRefreshCommand;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestCreateMessageBoardCommand;
import com.trans.pixel.protoc.Commands.RequestCreateUnionCommand;
import com.trans.pixel.protoc.Commands.RequestDailyShopCommand;
import com.trans.pixel.protoc.Commands.RequestDailyShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestDailyShopRefreshCommand;
import com.trans.pixel.protoc.Commands.RequestDefendUnionCommand;
import com.trans.pixel.protoc.Commands.RequestDeleteMailCommand;
import com.trans.pixel.protoc.Commands.RequestDeleteUnionCommand;
import com.trans.pixel.protoc.Commands.RequestEquipLevelUpCommand;
import com.trans.pixel.protoc.Commands.RequestGetLadderRankListCommand;
import com.trans.pixel.protoc.Commands.RequestGetUserLadderRankListCommand;
import com.trans.pixel.protoc.Commands.RequestPVPShopCommand;
import com.trans.pixel.protoc.Commands.RequestPVPShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestPVPShopRefreshCommand;
import com.trans.pixel.protoc.Commands.RequestExpeditionShopCommand;
import com.trans.pixel.protoc.Commands.RequestExpeditionShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestExpeditionShopRefreshCommand;
import com.trans.pixel.protoc.Commands.RequestPurchaseCoinCommand;
//add import here
import com.trans.pixel.protoc.Commands.RequestGetUserMailListCommand;
import com.trans.pixel.protoc.Commands.RequestHandleUnionMemberCommand;
import com.trans.pixel.protoc.Commands.RequestHeroLevelUpCommand;
import com.trans.pixel.protoc.Commands.RequestLadderShopCommand;
import com.trans.pixel.protoc.Commands.RequestLadderShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestLadderShopRefreshCommand;
import com.trans.pixel.protoc.Commands.RequestLevelLootResultCommand;
import com.trans.pixel.protoc.Commands.RequestLevelLootStartCommand;
import com.trans.pixel.protoc.Commands.RequestLevelPauseCommand;
import com.trans.pixel.protoc.Commands.RequestLevelPrepareCommand;
import com.trans.pixel.protoc.Commands.RequestLevelResultCommand;
import com.trans.pixel.protoc.Commands.RequestLevelStartCommand;
import com.trans.pixel.protoc.Commands.RequestLoginCommand;
import com.trans.pixel.protoc.Commands.RequestLootResultCommand;
import com.trans.pixel.protoc.Commands.RequestLotteryCommand;
import com.trans.pixel.protoc.Commands.RequestMessageBoardListCommand;
import com.trans.pixel.protoc.Commands.RequestQuitUnionCommand;
import com.trans.pixel.protoc.Commands.RequestReadMailCommand;
import com.trans.pixel.protoc.Commands.RequestReceiveFriendCommand;
import com.trans.pixel.protoc.Commands.RequestRefreshRelatedUserCommand;
import com.trans.pixel.protoc.Commands.RequestRegisterCommand;
import com.trans.pixel.protoc.Commands.RequestReplyMessageCommand;
import com.trans.pixel.protoc.Commands.RequestReplyUnionCommand;
import com.trans.pixel.protoc.Commands.RequestShopCommand;
import com.trans.pixel.protoc.Commands.RequestShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestUnionInfoCommand;
import com.trans.pixel.protoc.Commands.RequestUnionListCommand;
import com.trans.pixel.protoc.Commands.RequestUnionShopCommand;
import com.trans.pixel.protoc.Commands.RequestUnionShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestUnionShopRefreshCommand;
import com.trans.pixel.protoc.Commands.RequestUpdateTeamCommand;
import com.trans.pixel.protoc.Commands.RequestUpgradeUnionCommand;
import com.trans.pixel.protoc.Commands.RequestUserTeamListCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.command.AreaCommandService;
import com.trans.pixel.service.command.FriendCommandService;
import com.trans.pixel.service.command.HeroLevelUpCommandService;
import com.trans.pixel.service.command.LadderCommandService;
import com.trans.pixel.service.command.LevelCommandService;
import com.trans.pixel.service.command.LootCommandService;
import com.trans.pixel.service.command.LotteryEquipCommandService;
import com.trans.pixel.service.command.LotteryCommandService;
import com.trans.pixel.service.command.MailCommandService;
import com.trans.pixel.service.command.MessageCommandService;
import com.trans.pixel.service.command.PvpCommandService;
import com.trans.pixel.service.command.ShopCommandService;
import com.trans.pixel.service.command.TeamCommandService;
import com.trans.pixel.service.command.UnionCommandService;
import com.trans.pixel.service.command.UserCommandService;

@Service
public class GameDataScreen extends RequestScreen {
//	@Resource
//	private AccountCommandService accountCommandService;
	@Resource
	private UserCommandService userCommandService;
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
	@Resource
	private UnionCommandService unionCommandService;
	@Resource
	private ShopCommandService shopCommandService;
	@Resource
	private MessageCommandService messageCommandService;
	
	@Override
	protected boolean handleRegisterCommand(RequestCommand cmd,
			Builder responseBuilder) {
		userCommandService.register(cmd, responseBuilder);
		return true;
	}

	@Override
	protected boolean handleLoginCommand(RequestCommand cmd,
			Builder responseBuilder) {	
		userCommandService.login(cmd, responseBuilder);
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
	protected boolean handleCommand(RequestLotteryCommand cmd,
			Builder responseBuilder, UserBean user) {
		lotteryCommandService.lottery(cmd, responseBuilder, user);
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
	protected boolean handleCommand(RequestEquipLevelUpCommand cmd,
			Builder responseBuilder, UserBean user) {
		heroLevelUpCommandService.equipLevelup(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestAreaCommand cmd,
			Builder responseBuilder, UserBean user) {
		areaCommandService.Areas(responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestAttackBossCommand cmd,
			Builder responseBuilder, UserBean user) {
		areaCommandService.AttackBoss(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestAttackMonsterCommand cmd,
			Builder responseBuilder, UserBean user) {
		areaCommandService.AttackMonster(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestAttackResourceCommand cmd,
			Builder responseBuilder, UserBean user) {
		areaCommandService.AttackResource(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestAttackResourceMineCommand cmd,
			Builder responseBuilder, UserBean user) {
		areaCommandService.AttackResourceMine(cmd, responseBuilder, user);
		return true;
	}
	
	@Override
	protected boolean handleCommand(RequestRegisterCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	protected boolean handleCommand(RequestLoginCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	protected boolean handleCommand(RequestGetUserLadderRankListCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	protected boolean handleCommand(RequestDeleteUnionCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	protected boolean handleCommand(RequestHandleUnionMemberCommand cmd, Builder responseBuilder, UserBean user) {
		unionCommandService.handleMember(cmd, responseBuilder, user);
		return true;
	}
	
	@Override
	protected boolean handleCommand(RequestUnionInfoCommand cmd, Builder responseBuilder, UserBean user) {
		unionCommandService.getUnion(cmd, responseBuilder, user);
		return true;
	}
	
	@Override
	protected boolean handleCommand(RequestUpgradeUnionCommand cmd, Builder responseBuilder, UserBean user) {
		unionCommandService.upgrade(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestLevelPrepareCommand cmd,
			Builder responseBuilder, UserBean user) {
		levelCommandService.levelPrepara(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestLevelPauseCommand cmd,
			Builder responseBuilder, UserBean user) {
		levelCommandService.levelPause(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestApplyUnionCommand cmd,
			Builder responseBuilder, UserBean user) {
		unionCommandService.apply(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestCreateUnionCommand cmd,
			Builder responseBuilder, UserBean user) {
		unionCommandService.create(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestReplyUnionCommand cmd,
			Builder responseBuilder, UserBean user) {
		unionCommandService.reply(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestMessageBoardListCommand cmd,
			Builder responseBuilder, UserBean user) {
		messageCommandService.getMessageBoardList(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestCreateMessageBoardCommand cmd,
			Builder responseBuilder, UserBean user) {
		messageCommandService.createUnion(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestReplyMessageCommand cmd,
			Builder responseBuilder, UserBean user) {
		messageCommandService.replyMessage(cmd, responseBuilder, user);
		return true;
	}
	
	@Override
	protected boolean handleCommand(RequestUnionListCommand cmd, Builder responseBuilder, UserBean user) {
		unionCommandService.getUnions(cmd, responseBuilder, user);
		return true;
	}
	
	@Override
	protected boolean handleCommand(RequestQuitUnionCommand cmd, Builder responseBuilder, UserBean user) {
		unionCommandService.quit(cmd, responseBuilder, user);
		return true;
	}
	
	@Override
	protected boolean handleCommand(RequestAttackUnionCommand cmd, Builder responseBuilder, UserBean user) {
		unionCommandService.attack(cmd, responseBuilder, user);
		return true;
	}
	
	@Override
	protected boolean handleCommand(RequestDefendUnionCommand cmd, Builder responseBuilder, UserBean user) {
		unionCommandService.defend(cmd, responseBuilder, user);
		return true;
	}
	@Override//DailyShopCommand
	protected boolean handleCommand(RequestDailyShopCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.DailyShop(cmd, responseBuilder, user);
		return true;//DailyShopCommand
	}//DailyShopCommand
	@Override//DailyShopPurchaseCommand
	protected boolean handleCommand(RequestDailyShopPurchaseCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.DailyShopPurchase(cmd, responseBuilder, user);
		return true;//DailyShopPurchaseCommand
	}//DailyShopPurchaseCommand
	@Override//DailyShopRefreshCommand
	protected boolean handleCommand(RequestDailyShopRefreshCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.DailyShopRefresh(cmd, responseBuilder, user);
		return true;//DailyShopRefreshCommand
	}//DailyShopRefreshCommand
	@Override//ShopCommand
	protected boolean handleCommand(RequestShopCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.Shop(cmd, responseBuilder, user);
		return true;//ShopCommand
	}//ShopCommand
	@Override//ShopPurchaseCommand
	protected boolean handleCommand(RequestShopPurchaseCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.ShopPurchase(cmd, responseBuilder, user);
		return true;//ShopPurchaseCommand
	}//ShopPurchaseCommand
	@Override//BlackShopCommand
	protected boolean handleCommand(RequestBlackShopCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.BlackShop(cmd, responseBuilder, user);
		return true;//BlackShopCommand
	}//BlackShopCommand
	@Override//BlackShopPurchaseCommand
	protected boolean handleCommand(RequestBlackShopPurchaseCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.BlackShopPurchase(cmd, responseBuilder, user);
		return true;//BlackShopPurchaseCommand
	}//BlackShopPurchaseCommand
	@Override//BlackShopRefreshCommand
	protected boolean handleCommand(RequestBlackShopRefreshCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.BlackShopRefresh(cmd, responseBuilder, user);
		return true;//BlackShopRefreshCommand
	}//BlackShopRefreshCommand
	@Override//UnionShopCommand
	protected boolean handleCommand(RequestUnionShopCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.UnionShop(cmd, responseBuilder, user);
		return true;//UnionShopCommand
	}//UnionShopCommand
	@Override//UnionShopPurchaseCommand
	protected boolean handleCommand(RequestUnionShopPurchaseCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.UnionShopPurchase(cmd, responseBuilder, user);
		return true;//UnionShopPurchaseCommand
	}//UnionShopPurchaseCommand
	@Override//UnionShopRefreshCommand
	protected boolean handleCommand(RequestUnionShopRefreshCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.UnionShopRefresh(cmd, responseBuilder, user);
		return true;//UnionShopRefreshCommand
	}//UnionShopRefreshCommand
	@Override//LadderShopCommand
	protected boolean handleCommand(RequestLadderShopCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.LadderShop(cmd, responseBuilder, user);
		return true;//LadderShopCommand
	}//LadderShopCommand
	@Override//LadderShopPurchaseCommand
	protected boolean handleCommand(RequestLadderShopPurchaseCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.LadderShopPurchase(cmd, responseBuilder, user);
		return true;//LadderShopPurchaseCommand
	}//LadderShopPurchaseCommand
	@Override//LadderShopRefreshCommand
	protected boolean handleCommand(RequestLadderShopRefreshCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.LadderShopRefresh(cmd, responseBuilder, user);
		return true;//LadderShopRefreshCommand
	}//LadderShopRefreshCommand
	@Override//PVPShopCommand
	protected boolean handleCommand(RequestPVPShopCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.PVPShop(cmd, responseBuilder, user);
		return true;//PVPShopCommand
	}//PVPShopCommand
	@Override//PVPShopPurchaseCommand
	protected boolean handleCommand(RequestPVPShopPurchaseCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.PVPShopPurchase(cmd, responseBuilder, user);
		return true;//PVPShopPurchaseCommand
	}//PVPShopPurchaseCommand
	@Override//PVPShopRefreshCommand
	protected boolean handleCommand(RequestPVPShopRefreshCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.PVPShopRefresh(cmd, responseBuilder, user);
		return true;//PVPShopRefreshCommand
	}//PVPShopRefreshCommand
	@Override//ExpeditionShopCommand
	protected boolean handleCommand(RequestExpeditionShopCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.ExpeditionShop(cmd, responseBuilder, user);
		return true;//ExpeditionShopCommand
	}//ExpeditionShopCommand
	@Override//ExpeditionShopPurchaseCommand
	protected boolean handleCommand(RequestExpeditionShopPurchaseCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.ExpeditionShopPurchase(cmd, responseBuilder, user);
		return true;//ExpeditionShopPurchaseCommand
	}//ExpeditionShopPurchaseCommand
	@Override//ExpeditionShopRefreshCommand
	protected boolean handleCommand(RequestExpeditionShopRefreshCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.ExpeditionShopRefresh(cmd, responseBuilder, user);
		return true;//ExpeditionShopRefreshCommand
	}//ExpeditionShopRefreshCommand
	@Override//PurchaseCoinCommand
	protected boolean handleCommand(RequestPurchaseCoinCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.PurchaseCoin(cmd, responseBuilder, user);
		return true;//PurchaseCoinCommand
	}//PurchaseCoinCommand
	//add handleCommand here

	@Override
	protected boolean handleCommand(RequestAddTeamCommand cmd,
			Builder responseBuilder, UserBean user) {
		teamCommandService.addUserTeam(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestUserTeamListCommand cmd,
			Builder responseBuilder, UserBean user) {
		teamCommandService.getUserTeamList(cmd, responseBuilder, user);
		return true;
	}

}
