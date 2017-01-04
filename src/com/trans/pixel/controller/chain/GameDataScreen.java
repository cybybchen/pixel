package com.trans.pixel.controller.chain;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.RequestAchieveListCommand;
import com.trans.pixel.protoc.Commands.RequestAchieveRewardCommand;
import com.trans.pixel.protoc.Commands.RequestAddFriendCommand;
import com.trans.pixel.protoc.Commands.RequestAddHeroEquipCommand;
import com.trans.pixel.protoc.Commands.RequestApplyUnionCommand;
import com.trans.pixel.protoc.Commands.RequestAreaCommand;
import com.trans.pixel.protoc.Commands.RequestAreaResourceCommand;
import com.trans.pixel.protoc.Commands.RequestAttackBossCommand;
import com.trans.pixel.protoc.Commands.RequestAttackLadderModeCommand;
import com.trans.pixel.protoc.Commands.RequestAttackMonsterCommand;
import com.trans.pixel.protoc.Commands.RequestAttackPVPMineCommand;
import com.trans.pixel.protoc.Commands.RequestAttackPVPMonsterCommand;
import com.trans.pixel.protoc.Commands.RequestAttackResourceCommand;
import com.trans.pixel.protoc.Commands.RequestAttackResourceMineCommand;
import com.trans.pixel.protoc.Commands.RequestAttackResourceMineInfoCommand;
import com.trans.pixel.protoc.Commands.RequestAttackUnionCommand;
import com.trans.pixel.protoc.Commands.RequestBindAccountCommand;
import com.trans.pixel.protoc.Commands.RequestBlackShopCommand;
import com.trans.pixel.protoc.Commands.RequestBlackShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestBlackShopRefreshCommand;
import com.trans.pixel.protoc.Commands.RequestBosskillCommand;
import com.trans.pixel.protoc.Commands.RequestBrotherMineInfoCommand;
import com.trans.pixel.protoc.Commands.RequestBuyHeroPackageCommand;
import com.trans.pixel.protoc.Commands.RequestBuyLootPackageCommand;
import com.trans.pixel.protoc.Commands.RequestCdkeyCommand;
import com.trans.pixel.protoc.Commands.RequestCheatRechargeCommand;
import com.trans.pixel.protoc.Commands.RequestChoseClearInfoCommand;
import com.trans.pixel.protoc.Commands.RequestClearHeroCommand;
import com.trans.pixel.protoc.Commands.RequestCollectResourceMineCommand;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestCreateMessageBoardCommand;
import com.trans.pixel.protoc.Commands.RequestCreateUnionCommand;
import com.trans.pixel.protoc.Commands.RequestDailyShopCommand;
import com.trans.pixel.protoc.Commands.RequestDailyShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestDailyShopRefreshCommand;
import com.trans.pixel.protoc.Commands.RequestDefendUnionCommand;
import com.trans.pixel.protoc.Commands.RequestDelFriendCommand;
import com.trans.pixel.protoc.Commands.RequestDeleteMailCommand;
import com.trans.pixel.protoc.Commands.RequestEndMohuaMapCommand;
import com.trans.pixel.protoc.Commands.RequestEnterMohuaMapCommand;
import com.trans.pixel.protoc.Commands.RequestEquipComposeCommand;
import com.trans.pixel.protoc.Commands.RequestEquipLevelUpCommand;
import com.trans.pixel.protoc.Commands.RequestExpeditionShopCommand;
import com.trans.pixel.protoc.Commands.RequestExpeditionShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestExpeditionShopRefreshCommand;
import com.trans.pixel.protoc.Commands.RequestFeedFoodCommand;
import com.trans.pixel.protoc.Commands.RequestFenjieEquipCommand;
import com.trans.pixel.protoc.Commands.RequestFenjieHeroCommand;
import com.trans.pixel.protoc.Commands.RequestFenjieHeroEquipCommand;
import com.trans.pixel.protoc.Commands.RequestGetGrowExpCommand;
import com.trans.pixel.protoc.Commands.RequestGetGrowJewelCommand;
import com.trans.pixel.protoc.Commands.RequestGetLadderRankListCommand;
import com.trans.pixel.protoc.Commands.RequestGetLadderUserInfoCommand;
import com.trans.pixel.protoc.Commands.RequestGetTaskRewardCommand;
import com.trans.pixel.protoc.Commands.RequestGetTeamCommand;
import com.trans.pixel.protoc.Commands.RequestGetUserFriendListCommand;
import com.trans.pixel.protoc.Commands.RequestGetUserLadderRankListCommand;
import com.trans.pixel.protoc.Commands.RequestGetUserMailListCommand;
import com.trans.pixel.protoc.Commands.RequestGreenhandCommand;
import com.trans.pixel.protoc.Commands.RequestHandleUnionMemberCommand;
import com.trans.pixel.protoc.Commands.RequestHeartBeatCommand;
import com.trans.pixel.protoc.Commands.RequestHelpAttackPVPMineCommand;
import com.trans.pixel.protoc.Commands.RequestHeroLevelUpCommand;
import com.trans.pixel.protoc.Commands.RequestHeroLevelUpToCommand;
import com.trans.pixel.protoc.Commands.RequestHeroStrengthenCommand;
import com.trans.pixel.protoc.Commands.RequestIsAreaOwnerCommand;
import com.trans.pixel.protoc.Commands.RequestKaifu2ActivityCommand;
import com.trans.pixel.protoc.Commands.RequestKaifuListCommand;
import com.trans.pixel.protoc.Commands.RequestKaifuRewardCommand;
import com.trans.pixel.protoc.Commands.RequestLadderShopCommand;
import com.trans.pixel.protoc.Commands.RequestLadderShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestLadderShopRefreshCommand;
import com.trans.pixel.protoc.Commands.RequestLevelLootResultCommand;
import com.trans.pixel.protoc.Commands.RequestLevelLootStartCommand;
import com.trans.pixel.protoc.Commands.RequestLevelPauseCommand;
import com.trans.pixel.protoc.Commands.RequestLevelPrepareCommand;
import com.trans.pixel.protoc.Commands.RequestLevelResultCommand;
import com.trans.pixel.protoc.Commands.RequestLevelStartCommand;
import com.trans.pixel.protoc.Commands.RequestLibaoShopCommand;
import com.trans.pixel.protoc.Commands.RequestLockHeroCommand;
import com.trans.pixel.protoc.Commands.RequestLogCommand;
import com.trans.pixel.protoc.Commands.RequestLoginCommand;
import com.trans.pixel.protoc.Commands.RequestLootResultCommand;
import com.trans.pixel.protoc.Commands.RequestLotteryCommand;
import com.trans.pixel.protoc.Commands.RequestMessageBoardListCommand;
import com.trans.pixel.protoc.Commands.RequestMohuaHpRewardCommand;
import com.trans.pixel.protoc.Commands.RequestMohuaStageRewardCommand;
import com.trans.pixel.protoc.Commands.RequestMohuaSubmitStageCommand;
import com.trans.pixel.protoc.Commands.RequestOpenFetterCommand;
import com.trans.pixel.protoc.Commands.RequestPVPMapListCommand;
import com.trans.pixel.protoc.Commands.RequestPVPMineInfoCommand;
import com.trans.pixel.protoc.Commands.RequestPVPShopCommand;
import com.trans.pixel.protoc.Commands.RequestPVPShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestPVPShopRefreshCommand;
import com.trans.pixel.protoc.Commands.RequestPurchaseCoinCommand;
import com.trans.pixel.protoc.Commands.RequestPurchaseContractCommand;
import com.trans.pixel.protoc.Commands.RequestPurchaseLadderTimeCommand;
import com.trans.pixel.protoc.Commands.RequestPurchaseVipLibaoCommand;
import com.trans.pixel.protoc.Commands.RequestQueryRechargeCommand;
import com.trans.pixel.protoc.Commands.RequestQuitUnionCommand;
import com.trans.pixel.protoc.Commands.RequestRankCommand;
import com.trans.pixel.protoc.Commands.RequestReadMailCommand;
import com.trans.pixel.protoc.Commands.RequestReadyAttackLadderCommand;
import com.trans.pixel.protoc.Commands.RequestReceiveFriendCommand;
import com.trans.pixel.protoc.Commands.RequestGetBattletowerCommand;
import com.trans.pixel.protoc.Commands.RequestBattletowerShopCommand;
import com.trans.pixel.protoc.Commands.RequestBattletowerShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestBattletowerShopRefreshCommand;
//add import here
import com.trans.pixel.protoc.Commands.RequestRefreshAreaCommand;
import com.trans.pixel.protoc.Commands.RequestRefreshPVPMapCommand;
import com.trans.pixel.protoc.Commands.RequestRefreshPVPMineCommand;
import com.trans.pixel.protoc.Commands.RequestRegisterCommand;
import com.trans.pixel.protoc.Commands.RequestReplyMessageCommand;
import com.trans.pixel.protoc.Commands.RequestReplyUnionCommand;
import com.trans.pixel.protoc.Commands.RequestResetBattletowerCommand;
import com.trans.pixel.protoc.Commands.RequestResetHeroSkillCommand;
import com.trans.pixel.protoc.Commands.RequestRichangListCommand;
import com.trans.pixel.protoc.Commands.RequestRichangRewardCommand;
import com.trans.pixel.protoc.Commands.RequestSaleEquipCommand;
import com.trans.pixel.protoc.Commands.RequestSendMailCommand;
import com.trans.pixel.protoc.Commands.RequestSevenLoginSignCommand;
import com.trans.pixel.protoc.Commands.RequestShopCommand;
import com.trans.pixel.protoc.Commands.RequestShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestShouchongRewardCommand;
import com.trans.pixel.protoc.Commands.RequestSignCommand;
import com.trans.pixel.protoc.Commands.RequestStartMohuaMapCommand;
import com.trans.pixel.protoc.Commands.RequestSubmitBattletowerCommand;
import com.trans.pixel.protoc.Commands.RequestSubmitBosskillCommand;
import com.trans.pixel.protoc.Commands.RequestSubmitComposeSkillCommand;
import com.trans.pixel.protoc.Commands.RequestSubmitIconCommand;
import com.trans.pixel.protoc.Commands.RequestSubmitZhanliCommand;
import com.trans.pixel.protoc.Commands.RequestUnionBossFightCommand;
import com.trans.pixel.protoc.Commands.RequestUnionInfoCommand;
import com.trans.pixel.protoc.Commands.RequestUnionListCommand;
import com.trans.pixel.protoc.Commands.RequestUnionShopCommand;
import com.trans.pixel.protoc.Commands.RequestUnionShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestUnionShopRefreshCommand;
import com.trans.pixel.protoc.Commands.RequestUnlockAreaCommand;
import com.trans.pixel.protoc.Commands.RequestUnlockLevelCommand;
import com.trans.pixel.protoc.Commands.RequestUnlockPVPMapCommand;
import com.trans.pixel.protoc.Commands.RequestUpdateTeamCommand;
import com.trans.pixel.protoc.Commands.RequestUpgradeUnionCommand;
import com.trans.pixel.protoc.Commands.RequestUseAreaEquipCommand;
import com.trans.pixel.protoc.Commands.RequestUseMohuaCardCommand;
import com.trans.pixel.protoc.Commands.RequestUsePropCommand;
import com.trans.pixel.protoc.Commands.RequestUserPokedeCommand;
import com.trans.pixel.protoc.Commands.RequestUserTaskCommand;
import com.trans.pixel.protoc.Commands.RequestUserTeamListCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.command.AchieveCommandService;
import com.trans.pixel.service.command.ActivityCommandService;
import com.trans.pixel.service.command.AreaCommandService;
import com.trans.pixel.service.command.BattletowerCommandService;
import com.trans.pixel.service.command.BossCommandService;
import com.trans.pixel.service.command.CdkeyCommandService;
import com.trans.pixel.service.command.CheatRechargeCommandService;
import com.trans.pixel.service.command.EquipCommandService;
import com.trans.pixel.service.command.FriendCommandService;
import com.trans.pixel.service.command.HeartBeatCommandService;
import com.trans.pixel.service.command.HeroCommandService;
import com.trans.pixel.service.command.LadderCommandService;
import com.trans.pixel.service.command.LevelCommandService;
import com.trans.pixel.service.command.LibaoCommandService;
import com.trans.pixel.service.command.LogCommandService;
import com.trans.pixel.service.command.LootCommandService;
import com.trans.pixel.service.command.LotteryCommandService;
import com.trans.pixel.service.command.LotteryEquipCommandService;
import com.trans.pixel.service.command.MailCommandService;
import com.trans.pixel.service.command.MessageCommandService;
import com.trans.pixel.service.command.MohuaCommandService;
import com.trans.pixel.service.command.NoticeCommandService;
import com.trans.pixel.service.command.PokedeCommandService;
import com.trans.pixel.service.command.PropCommandService;
import com.trans.pixel.service.command.PvpCommandService;
import com.trans.pixel.service.command.RankCommandService;
import com.trans.pixel.service.command.RechargeCommandService;
import com.trans.pixel.service.command.ShopCommandService;
import com.trans.pixel.service.command.SignCommandService;
import com.trans.pixel.service.command.TaskCommandService;
import com.trans.pixel.service.command.TeamCommandService;
import com.trans.pixel.service.command.UnionCommandService;
import com.trans.pixel.service.command.UserCommandService;
import com.trans.pixel.service.command.ZhanliCommandService;

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
	private HeroCommandService heroCommandService;
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
	@Resource
	private EquipCommandService equipCommandService;
	@Resource
	private PropCommandService propCommandService;
	@Resource
	private SignCommandService signCommandService;
	@Resource
	private MohuaCommandService mohuaCommandService;
	@Resource
	private ZhanliCommandService zhanliCommandService;
	@Resource
	private AchieveCommandService AchieveCommandService;
	@Resource
	private ActivityCommandService activityCommandService;
	@Resource
	private PokedeCommandService pokedeCommandService;
	@Resource
	private RankCommandService rankCommandService;
	@Resource
	private CdkeyCommandService cdkeyCommandService;
	@Resource
	private CheatRechargeCommandService cheatRechargeCommandService;
	@Resource
	private RechargeCommandService rechargeCommandService;
	@Resource
	private NoticeCommandService noticeCommandService;
	@Resource
	private LogCommandService logCommandService;
	@Resource
	private LibaoCommandService libaoCommandService;
	@Resource
	private HeartBeatCommandService heartBeatCommmandService;
	@Resource
	private BossCommandService bossCommandService;
	@Resource
	private TaskCommandService taskCommandService;
	@Resource
	private BattletowerCommandService battletowerCommandService;
	
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
	protected boolean handleCommand(RequestLootResultCommand cmd,
			Builder responseBuilder, UserBean user) {
		lootCommandService.lootResult(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestHeroLevelUpCommand cmd,
			Builder responseBuilder, UserBean user) {
		heroCommandService.heroLevelUp(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestAddHeroEquipCommand cmd,
			Builder responseBuilder, UserBean user) {
		heroCommandService.heroAddEquip(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestUpdateTeamCommand cmd,
			Builder responseBuilder, UserBean user) {
		teamCommandService.updateUserTeam(cmd, responseBuilder, user);
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
		heroCommandService.equipLevelup(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestAreaCommand cmd,
			Builder responseBuilder, UserBean user) {
		areaCommandService.Areas(cmd, responseBuilder, user);
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
//		areaCommandService.AttackResourceMine(cmd, responseBuilder, user);
		return true;
	}
	
	@Override
	protected boolean handleCommand(RequestRegisterCommand cmd, Builder responseBuilder, UserBean user) {
		return true;
	}
	
	@Override
	protected boolean handleCommand(RequestLoginCommand cmd, Builder responseBuilder, UserBean user) {
		return true;
	}
	
	@Override
	protected boolean handleCommand(RequestGetUserLadderRankListCommand cmd, Builder responseBuilder, UserBean user) {
		ladderCommandService.handleGetUserLadderRankListCommand(cmd, responseBuilder, user);
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
		messageCommandService.createMessage(cmd, responseBuilder, user);
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
	@Override//AttackResourceMineInfoCommand
	protected boolean handleCommand(RequestAttackResourceMineInfoCommand cmd, Builder responseBuilder, UserBean user) {
//		areaCommandService.AttackResourceMineInfo(cmd, responseBuilder, user);
		return true;//AttackResourceMineInfoCommand
	}//AttackResourceMineInfoCommand
	@Override//UserMineTeamCommand
	protected boolean handleCommand(RequestGetTeamCommand cmd, Builder responseBuilder, UserBean user) {
		teamCommandService.getTeamCache(cmd, responseBuilder, user);
		return true;//UserMineTeamCommand
	}//UserMineTeamCommand
	@Override//AttackPVPMonstersCommand
	protected boolean handleCommand(RequestAttackPVPMonsterCommand cmd, Builder responseBuilder, UserBean user) {
		pvpCommandService.attackMonster(cmd, responseBuilder, user);
		return true;//AttackPVPMonstersCommand
	}//AttackPVPMonstersCommand
	@Override//PvpMapListCommand
	protected boolean handleCommand(RequestPVPMapListCommand cmd, Builder responseBuilder, UserBean user) {
		pvpCommandService.getMapList(cmd, responseBuilder, user);
		return true;//PvpMapListCommand
	}//PvpMapListCommand
	@Override//PvpMineInfoCommand
	protected boolean handleCommand(RequestPVPMineInfoCommand cmd, Builder responseBuilder, UserBean user) {
		pvpCommandService.getMineInfo(cmd, responseBuilder, user);
		return true;//PvpMineInfoCommand
	}//PvpMineInfoCommand
	@Override//AttackPVPMineCommand
	protected boolean handleCommand(RequestAttackPVPMineCommand cmd, Builder responseBuilder, UserBean user) {
		pvpCommandService.attackMine(cmd, responseBuilder, user);
		return true;//AttackPVPMineCommand
	}//AttackPVPMineCommand
	@Override//RefreshPVPMineCommand
	protected boolean handleCommand(RequestRefreshPVPMineCommand cmd, Builder responseBuilder, UserBean user) {
		pvpCommandService.refreshMine(cmd, responseBuilder, user);
		return true;//RefreshPVPMineCommand
	}//RefreshPVPMineCommand
	@Override//LockHeroCommand
	protected boolean handleCommand(RequestLockHeroCommand cmd, Builder responseBuilder, UserBean user) {
		heroCommandService.lockHero(cmd, responseBuilder, user);
		return true;//LockHeroCommand
	}//LockHeroCommand
	@Override//MohuaSubmitStageCommand
	protected boolean handleCommand(RequestMohuaSubmitStageCommand cmd, Builder responseBuilder, UserBean user) {
		mohuaCommandService.submitStage(cmd, responseBuilder, user);
		return true;//MohuaSubmitStageCommand
	}//MohuaSubmitStageCommand
	@Override//UnlockAreaCommand
	protected boolean handleCommand(RequestUnlockAreaCommand cmd, Builder responseBuilder, UserBean user) {
		areaCommandService.unlockArea(cmd, responseBuilder, user);
		return true;//UnlockAreaCommand
	}//UnlockAreaCommand
	@Override//UnlockPvpMapCommand
	protected boolean handleCommand(RequestUnlockPVPMapCommand cmd, Builder responseBuilder, UserBean user) {
		pvpCommandService.unlockMap(cmd, responseBuilder, user);
		return true;//UnlockPvpMapCommand
	}//UnlockPvpMapCommand
	@Override//CollectResourceMineCommand
	protected boolean handleCommand(RequestCollectResourceMineCommand cmd, Builder responseBuilder, UserBean user) {
//		areaCommandService.collectMine(cmd, responseBuilder, user);
		return true;//CollectResourceMineCommand
	}//CollectResourceMineCommand
	@Override//AreaResourceCommand
	protected boolean handleCommand(RequestAreaResourceCommand cmd, Builder responseBuilder, UserBean user) {
		areaCommandService.resourceInfo(cmd, responseBuilder, user);
		return true;//AreaResourceCommand
	}//AreaResourceCommand
	@Override//UnlockLevelCommand
	protected boolean handleCommand(RequestUnlockLevelCommand cmd, Builder responseBuilder, UserBean user) {
		levelCommandService.levelUnlock(cmd, responseBuilder, user);
		return true;//UnlockLevelCommand
	}//UnlockLevelCommand
	@Override//UseAreaEquipCommand
	protected boolean handleCommand(RequestUseAreaEquipCommand cmd, Builder responseBuilder, UserBean user) {
		areaCommandService.useAreaEquips(cmd, responseBuilder, user);
		return true;//UseAreaEquipCommand
	}//UseAreaEquipCommand
	@Override//BuyHeroPackageCommand
	protected boolean handleCommand(RequestBuyHeroPackageCommand cmd, Builder responseBuilder, UserBean user) {
		heroCommandService.buyHeroPackage(cmd, responseBuilder, user);
		return true;//BuyHeroPackageCommand
	}//BuyHeroPackageCommand
	@Override//SubmitComposeSkillCommand
	protected boolean handleCommand(RequestSubmitComposeSkillCommand cmd, Builder responseBuilder, UserBean user) {
		heroCommandService.submitComposeSkill(cmd, responseBuilder, user);
		return true;//SubmitComposeSkillCommand
	}//SubmitComposeSkillCommand
	@Override//BuyLootPackageCommand
	protected boolean handleCommand(RequestBuyLootPackageCommand cmd, Builder responseBuilder, UserBean user) {
		levelCommandService.buyLootPackage(cmd, responseBuilder, user);
		return true;//BuyLootPackageCommand
	}//BuyLootPackageCommand
	@Override//CdkeyCommand
	protected boolean handleCommand(RequestCdkeyCommand cmd, Builder responseBuilder, UserBean user) {
		cdkeyCommandService.useCdkey(cmd, responseBuilder, user);
		return true;//CdkeyCommand
	}//CdkeyCommand
	@Override//SubmitIconCommand
	protected boolean handleCommand(RequestSubmitIconCommand cmd, Builder responseBuilder, UserBean user) {
		userCommandService.submitIcon(cmd, responseBuilder, user);
		return true;//SubmitIconCommand
	}//SubmitIconCommand
	@Override//RechargeCommand
	protected boolean handleCommand(RequestCheatRechargeCommand cmd, Builder responseBuilder, UserBean user) {
		cheatRechargeCommandService.cheatRecharge(cmd, responseBuilder, user);
		return true;//RechargeCommand
	}//RechargeCommand
	@Override//RefreshPvpMapCommand
	protected boolean handleCommand(RequestRefreshPVPMapCommand cmd, Builder responseBuilder, UserBean user) {
		pvpCommandService.refreshMap(cmd, responseBuilder, user);
		return true;//RefreshPvpMapCommand
	}//RefreshPvpMapCommand
	@Override//ReadyAttackLadderCommand
	protected boolean handleCommand(RequestReadyAttackLadderCommand cmd, Builder responseBuilder, UserBean user) {
		ladderCommandService.readyAttackLadder(cmd, responseBuilder, user);
		return true;//ReadyAttackLadderCommand
	}//ReadyAttackLadderCommand
	@Override//BindAccountCommand
	protected boolean handleCommand(RequestBindAccountCommand cmd, Builder responseBuilder, UserBean user) {
		userCommandService.bindAccount(cmd, responseBuilder, user);
		return true;//BindAccountCommand
	}//BindAccountCommand
	@Override//PurchaseVipLibaoCommand
	protected boolean handleCommand(RequestPurchaseVipLibaoCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.VipLibaoPurchase(cmd, responseBuilder, user);
		return true;//PurchaseVipLibaoCommand
	}//PurchaseVipLibaoCommand
	@Override//QueryRechargeCommand
	protected boolean handleCommand(RequestQueryRechargeCommand cmd, Builder responseBuilder, UserBean user) {
		rechargeCommandService.recharge(cmd, responseBuilder, user);
		return true;//QueryRechargeCommand
	}//QueryRechargeCommand
	@Override//LibaoShopCommand
	protected boolean handleCommand(RequestLibaoShopCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.LibaoShop(cmd, responseBuilder, user);
		return true;//LibaoShopCommand
	}//LibaoShopCommand
	@Override//ShouchongRewardCommand
	protected boolean handleCommand(RequestShouchongRewardCommand cmd, Builder responseBuilder, UserBean user) {
		activityCommandService.shouchongReward(cmd, responseBuilder, user);
		return true;//ShouchongRewardCommand
	}//ShouchongRewardCommand
	@Override//HeartBeatCommand
	protected boolean handleCommand(RequestHeartBeatCommand cmd, Builder responseBuilder, UserBean user) {
		heartBeatCommmandService.heartBeat(cmd, responseBuilder, user);
		return true;//HeartBeatCommand
	}//HeartBeatCommand
	@Override//GreenhandCommand
	protected boolean handleCommand(RequestGreenhandCommand cmd, Builder responseBuilder, UserBean user) {
		userCommandService.submitGreenhand(cmd, responseBuilder, user);
		return true;//GreenhandCommand
	}//GreenhandCommand
	@Override//LogCommand
	protected boolean handleCommand(RequestLogCommand cmd, Builder responseBuilder, UserBean user) {
		logCommandService.log(cmd, responseBuilder, user);
		return true;//LogCommand
	}//LogCommand
	@Override//GetGrowJewelCommand
	protected boolean handleCommand(RequestGetGrowJewelCommand cmd, Builder responseBuilder, UserBean user) {
		libaoCommandService.getGrowJewel(cmd, responseBuilder, user);
		return true;//GetGrowJewelCommand
	}//GetGrowJewelCommand
	@Override//GetGrowExpCommand
	protected boolean handleCommand(RequestGetGrowExpCommand cmd, Builder responseBuilder, UserBean user) {
		libaoCommandService.getGrowExp(cmd, responseBuilder, user);
		return true;//GetGrowExpCommand
	}//GetGrowExpCommand
	@Override//FeedFoodCommand
	protected boolean handleCommand(RequestFeedFoodCommand cmd, Builder responseBuilder, UserBean user) {
		pokedeCommandService.feedFood(cmd, responseBuilder, user);
		return true;//FeedFoodCommand
	}//FeedFoodCommand
	@Override//ClearHeroCommand
	protected boolean handleCommand(RequestClearHeroCommand cmd, Builder responseBuilder, UserBean user) {
		pokedeCommandService.clearPokede(cmd, responseBuilder, user);
		return true;//ClearHeroCommand
	}//ClearHeroCommand
	@Override//ChoseClearInfoCommand
	protected boolean handleCommand(RequestChoseClearInfoCommand cmd, Builder responseBuilder, UserBean user) {
		pokedeCommandService.choseClearInfo(cmd, responseBuilder, user);
		return true;//ChoseClearInfoCommand
	}//ChoseClearInfoCommand
	@Override//SubmitBosskillCommand
	protected boolean handleCommand(RequestSubmitBosskillCommand cmd, Builder responseBuilder, UserBean user) {
		bossCommandService.bossKill(cmd, responseBuilder, user);
		return true;//SubmitBosskillCommand
	}//SubmitBosskillCommand
	@Override//BosskillCommand
	protected boolean handleCommand(RequestBosskillCommand cmd, Builder responseBuilder, UserBean user) {
		bossCommandService.getBosskillRecord(responseBuilder, user);
		return true;//BosskillCommand
	}//BosskillCommand
	@Override//UnionBossFightCommand
	protected boolean handleCommand(RequestUnionBossFightCommand cmd, Builder responseBuilder, UserBean user) {
		unionCommandService.attackUnionBoss(cmd, responseBuilder, user);
		return true;//UnionBossFightCommand
	}//UnionBossFightCommand
	@Override//HeroStrengthenCommand
	protected boolean handleCommand(RequestHeroStrengthenCommand cmd, Builder responseBuilder, UserBean user) {
		pokedeCommandService.heroStrengthen(cmd, responseBuilder, user);
		return true;//HeroStrengthenCommand
	}//HeroStrengthenCommand
	@Override//SevenLoginSignCommand
	protected boolean handleCommand(RequestSevenLoginSignCommand cmd, Builder responseBuilder, UserBean user) {
		signCommandService.sevenSign(cmd, responseBuilder, user);
		return true;//SevenLoginSignCommand
	}//SevenLoginSignCommand
	@Override//IsAreaOwnerCommand
	protected boolean handleCommand(RequestIsAreaOwnerCommand cmd, Builder responseBuilder, UserBean user) {
		areaCommandService.isAreaOwner(cmd, responseBuilder, user);
		return true;//IsAreaOwnerCommand
	}//IsAreaOwnerCommand
	@Override//OpenFetterCommand
	protected boolean handleCommand(RequestOpenFetterCommand cmd, Builder responseBuilder, UserBean user) {
		pokedeCommandService.openFetters(cmd, responseBuilder, user);
		return true;//OpenFetterCommand
	}//OpenFetterCommand
	@Override//UserTaskCommand
	protected boolean handleCommand(RequestUserTaskCommand cmd, Builder responseBuilder, UserBean user) {
		taskCommandService.userTask(cmd, responseBuilder, user);
		return true;//UserTaskCommand
	}//UserTaskCommand
	@Override//GetTaskRewardCommand
	protected boolean handleCommand(RequestGetTaskRewardCommand cmd, Builder responseBuilder, UserBean user) {
		taskCommandService.taskReward(cmd, responseBuilder, user);
		return true;//GetTaskRewardCommand
	}//GetTaskRewardCommand
	@Override//SubmitBattletowerCommand
	protected boolean handleCommand(RequestSubmitBattletowerCommand cmd, Builder responseBuilder, UserBean user) {
		battletowerCommandService.submitBattletower(cmd, responseBuilder, user);
		return true;//SubmitBattletowerCommand
	}//SubmitBattletowerCommand
	@Override//ResetBattletowerCommand
	protected boolean handleCommand(RequestResetBattletowerCommand cmd, Builder responseBuilder, UserBean user) {
		battletowerCommandService.resetBattletower(cmd, responseBuilder, user);
		return true;//ResetBattletowerCommand
	}//ResetBattletowerCommand
	@Override//GetBattletowerCommand
	protected boolean handleCommand(RequestGetBattletowerCommand cmd, Builder responseBuilder, UserBean user) {
		battletowerCommandService.getBattletower(cmd, responseBuilder, user);
		return true;//GetBattletowerCommand
	}//GetBattletowerCommand
	@Override//BattletowerShopCommand
	protected boolean handleCommand(RequestBattletowerShopCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.battletowerShop(cmd, responseBuilder, user);
		return true;//BattletowerShopCommand
	}//BattletowerShopCommand
	@Override//BattletowerShopPurchaseCommand
	protected boolean handleCommand(RequestBattletowerShopPurchaseCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.battletowerShopPurchase(cmd, responseBuilder, user);
		return true;//BattletowerShopPurchaseCommand
	}//BattletowerShopPurchaseCommand
	@Override//BattletowerShopRefreshCommand
	protected boolean handleCommand(RequestBattletowerShopRefreshCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.battletowerShopRefresh(cmd, responseBuilder, user);
		return true;//BattletowerShopRefreshCommand
	}//BattletowerShopRefreshCommand
	//add handleCommand here
	
	@Override
	protected boolean handleCommand(RequestRefreshAreaCommand cmd, Builder responseBuilder, UserBean user) {
		areaCommandService.refreshArea(cmd, responseBuilder, user);
		return true;
	}
	
	@Override
	protected boolean handleCommand(RequestPurchaseContractCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.purchaseContract(cmd, responseBuilder, user);
		return true;
	}
	
	@Override
	protected boolean handleCommand(RequestPurchaseLadderTimeCommand cmd, Builder responseBuilder, UserBean user) {
		ladderCommandService.purchaseLadderTime(cmd, responseBuilder, user);
		return true;
	}
	
	@Override
	protected boolean handleCommand(RequestHeroLevelUpToCommand cmd, Builder responseBuilder, UserBean user) {
		heroCommandService.heroLevelUpTo(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestUserTeamListCommand cmd,
			Builder responseBuilder, UserBean user) {
		teamCommandService.getUserTeamList(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestGetLadderUserInfoCommand cmd,
			Builder responseBuilder, UserBean user) {
		ladderCommandService.getLadderUserInfo(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestGetUserFriendListCommand cmd,
			Builder responseBuilder, UserBean user) {
		friendCommandService.getUserFriendList(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestEquipComposeCommand cmd,
			Builder responseBuilder, UserBean user) {
		equipCommandService.equipLevelup(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestUsePropCommand cmd,
			Builder responseBuilder, UserBean user) {
		propCommandService.useProp(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestFenjieEquipCommand cmd,
			Builder responseBuilder, UserBean user) {
		equipCommandService.fenjie(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestFenjieHeroEquipCommand cmd,
			Builder responseBuilder, UserBean user) {
		heroCommandService.fenjieHeroEquip(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestSignCommand cmd,
			Builder responseBuilder, UserBean user) {
		signCommandService.sign(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestHelpAttackPVPMineCommand cmd,
			Builder responseBuilder, UserBean user) {
		pvpCommandService.attackMine(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestFenjieHeroCommand cmd,
			Builder responseBuilder, UserBean user) {
		heroCommandService.fenjieHero(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestResetHeroSkillCommand cmd,
			Builder responseBuilder, UserBean user) {
		heroCommandService.resetHeroSkill(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestSendMailCommand cmd,
			Builder responseBuilder, UserBean user) {
		mailCommandService.sendMail(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestBrotherMineInfoCommand cmd,
			Builder responseBuilder, UserBean user) {
		pvpCommandService.getBrotherMineInfo(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestEnterMohuaMapCommand cmd,
			Builder responseBuilder, UserBean user) {
		mohuaCommandService.enterMohuaMap(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestStartMohuaMapCommand cmd,
			Builder responseBuilder, UserBean user) {
		mohuaCommandService.startMohuaUserData(cmd, responseBuilder, user);
		return true;
	}
	
	@Override
	protected boolean handleCommand(RequestEndMohuaMapCommand cmd,
			Builder responseBuilder, UserBean user) {
		mohuaCommandService.endMohuaUserData(cmd, responseBuilder, user);
		return true;
	}


	@Override
	protected boolean handleCommand(RequestUseMohuaCardCommand cmd,
			Builder responseBuilder, UserBean user) {
		mohuaCommandService.useMohuaCard(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestMohuaStageRewardCommand cmd,
			Builder responseBuilder, UserBean user) {
		mohuaCommandService.rewardStage(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestMohuaHpRewardCommand cmd,
			Builder responseBuilder, UserBean user) {
		mohuaCommandService.rewardHp(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestSaleEquipCommand cmd,
			Builder responseBuilder, UserBean user) {
		equipCommandService.saleEquip(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestDelFriendCommand cmd,
			Builder responseBuilder, UserBean user) {
		friendCommandService.delUserFriend(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestSubmitZhanliCommand cmd,
			Builder responseBuilder, UserBean user) {
		zhanliCommandService.submitZhanli(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestAchieveRewardCommand cmd,
			Builder responseBuilder, UserBean user) {
		AchieveCommandService.achieveReward(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestAchieveListCommand cmd,
			Builder responseBuilder, UserBean user) {
		AchieveCommandService.achieveList(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestRichangRewardCommand cmd,
			Builder responseBuilder, UserBean user) {
		activityCommandService.richangReward(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestRichangListCommand cmd,
			Builder responseBuilder, UserBean user) {
		activityCommandService.richangList(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestKaifu2ActivityCommand cmd,
			Builder responseBuilder, UserBean user) {
		activityCommandService.kaifu2Activity(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestKaifuRewardCommand cmd,
			Builder responseBuilder, UserBean user) {
		activityCommandService.kaifuReward(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestKaifuListCommand cmd,
			Builder responseBuilder, UserBean user) {
		activityCommandService.kaifuList(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestUserPokedeCommand cmd,
			Builder responseBuilder, UserBean user) {
		pokedeCommandService.getUserPokedeList(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestRankCommand cmd,
			Builder responseBuilder, UserBean user) {
		rankCommandService.getRankList(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean pushNoticeCommand(Builder responseBuilder, UserBean user) {
		noticeCommandService.pushNotices(responseBuilder, user);
		return true;
	}

}
