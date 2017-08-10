package com.trans.pixel.controller.chain;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.ActivityProto.RequestAchieveListCommand;
import com.trans.pixel.protoc.ActivityProto.RequestAchieveRewardCommand;
import com.trans.pixel.protoc.ActivityProto.RequestCipherRewardCommand;
import com.trans.pixel.protoc.ActivityProto.RequestKaifu2ActivityCommand;
import com.trans.pixel.protoc.ActivityProto.RequestKaifuListCommand;
import com.trans.pixel.protoc.ActivityProto.RequestKaifuRewardCommand;
import com.trans.pixel.protoc.ActivityProto.RequestLotteryCommand;
import com.trans.pixel.protoc.ActivityProto.RequestRankCommand;
import com.trans.pixel.protoc.ActivityProto.RequestRichangListCommand;
import com.trans.pixel.protoc.ActivityProto.RequestRichangRewardCommand;
import com.trans.pixel.protoc.AreaProto.RequestAreaCommand;
import com.trans.pixel.protoc.AreaProto.RequestAreaResourceCommand;
import com.trans.pixel.protoc.AreaProto.RequestAttackBossCommand;
import com.trans.pixel.protoc.AreaProto.RequestAttackMonsterCommand;
import com.trans.pixel.protoc.AreaProto.RequestAttackResourceCommand;
import com.trans.pixel.protoc.AreaProto.RequestAttackResourceMineCommand;
import com.trans.pixel.protoc.AreaProto.RequestAttackResourceMineInfoCommand;
import com.trans.pixel.protoc.AreaProto.RequestCollectResourceMineCommand;
import com.trans.pixel.protoc.AreaProto.RequestIsAreaOwnerCommand;
import com.trans.pixel.protoc.AreaProto.RequestRefreshAreaCommand;
import com.trans.pixel.protoc.AreaProto.RequestUnlockAreaCommand;
import com.trans.pixel.protoc.AreaProto.RequestUseAreaEquipCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.EquipProto.RequestAddHeroEquipCommand;
import com.trans.pixel.protoc.EquipProto.RequestEquipComposeCommand;
import com.trans.pixel.protoc.EquipProto.RequestEquipPokedeCommand;
import com.trans.pixel.protoc.EquipProto.RequestEquipStrenthenCommand;
import com.trans.pixel.protoc.EquipProto.RequestEquipupCommand;
import com.trans.pixel.protoc.EquipProto.RequestFenjieEquipCommand;
import com.trans.pixel.protoc.EquipProto.RequestMaterialComposeCommand;
import com.trans.pixel.protoc.EquipProto.RequestSaleEquipCommand;
import com.trans.pixel.protoc.EquipProto.RequestSubmitZhanliCommand;
import com.trans.pixel.protoc.EquipProto.RequestSynthetiseComposeCommand;
import com.trans.pixel.protoc.EquipProto.RequestUseMaterialCommand;
import com.trans.pixel.protoc.EquipProto.RequestUsePropCommand;
import com.trans.pixel.protoc.HeroProto.RequestBuyHeroPackageCommand;
import com.trans.pixel.protoc.HeroProto.RequestChoseClearInfoCommand;
import com.trans.pixel.protoc.HeroProto.RequestClearHeroCommand;
import com.trans.pixel.protoc.HeroProto.RequestFeedFoodCommand;
import com.trans.pixel.protoc.HeroProto.RequestFenjieHeroCommand;
import com.trans.pixel.protoc.HeroProto.RequestGetTeamCommand;
import com.trans.pixel.protoc.HeroProto.RequestHeroLevelUpCommand;
import com.trans.pixel.protoc.HeroProto.RequestHeroLevelUpToCommand;
import com.trans.pixel.protoc.HeroProto.RequestHeroSpUpCommand;
import com.trans.pixel.protoc.HeroProto.RequestHeroStrengthenCommand;
import com.trans.pixel.protoc.HeroProto.RequestLockHeroCommand;
import com.trans.pixel.protoc.HeroProto.RequestOpenFetterCommand;
import com.trans.pixel.protoc.HeroProto.RequestResetHeroSkillCommand;
import com.trans.pixel.protoc.HeroProto.RequestSubmitComposeSkillCommand;
import com.trans.pixel.protoc.HeroProto.RequestTalentChangeEquipCommand;
import com.trans.pixel.protoc.HeroProto.RequestTalentChangeSkillCommand;
import com.trans.pixel.protoc.HeroProto.RequestTalentChangeUseCommand;
import com.trans.pixel.protoc.HeroProto.RequestTalentResetSkillCommand;
import com.trans.pixel.protoc.HeroProto.RequestTalentSkillLevelupCommand;
import com.trans.pixel.protoc.HeroProto.RequestTalentSpUpCommand;
import com.trans.pixel.protoc.HeroProto.RequestTalentupgradeCommand;
import com.trans.pixel.protoc.HeroProto.RequestUpdateTeamCommand;
import com.trans.pixel.protoc.HeroProto.RequestUserPokedeCommand;
import com.trans.pixel.protoc.HeroProto.RequestUserTeamListCommand;
import com.trans.pixel.protoc.HeroProto.RequestZanHeroMessageBoardCommand;
import com.trans.pixel.protoc.LadderProto.RequestAttackLadderModeCommand;
import com.trans.pixel.protoc.LadderProto.RequestFightInfoCommand;
import com.trans.pixel.protoc.LadderProto.RequestGetFightInfoCommand;
import com.trans.pixel.protoc.LadderProto.RequestGetLadderRankListCommand;
import com.trans.pixel.protoc.LadderProto.RequestGetLadderUserInfoCommand;
import com.trans.pixel.protoc.LadderProto.RequestGetUserLadderRankListCommand;
import com.trans.pixel.protoc.LadderProto.RequestLadderEnemyCommand;
import com.trans.pixel.protoc.LadderProto.RequestLadderInfoCommand;
import com.trans.pixel.protoc.LadderProto.RequestLadderSeasonRewardCommand;
import com.trans.pixel.protoc.LadderProto.RequestLadderTaskRewardCommand;
import com.trans.pixel.protoc.LadderProto.RequestPurchaseLadderTimeCommand;
import com.trans.pixel.protoc.LadderProto.RequestReadyAttackLadderCommand;
import com.trans.pixel.protoc.LadderProto.RequestRefreshLadderEnemyCommand;
import com.trans.pixel.protoc.LadderProto.RequestSaveFightInfoCommand;
import com.trans.pixel.protoc.LadderProto.RequestSubmitLadderResultCommand;
import com.trans.pixel.protoc.MailProto.RequestAddFriendCommand;
import com.trans.pixel.protoc.MailProto.RequestDelFriendCommand;
import com.trans.pixel.protoc.MailProto.RequestDeleteMailCommand;
import com.trans.pixel.protoc.MailProto.RequestGetUserFriendListCommand;
import com.trans.pixel.protoc.MailProto.RequestGetUserMailListCommand;
import com.trans.pixel.protoc.MailProto.RequestReadMailCommand;
import com.trans.pixel.protoc.MailProto.RequestReceiveFriendCommand;
import com.trans.pixel.protoc.MessageBoardProto.RequestCreateMessageBoardCommand;
import com.trans.pixel.protoc.MessageBoardProto.RequestGreenhandCommand;
import com.trans.pixel.protoc.MessageBoardProto.RequestHeartBeatCommand;
import com.trans.pixel.protoc.MessageBoardProto.RequestLogCommand;
import com.trans.pixel.protoc.MessageBoardProto.RequestMessageBoardListCommand;
import com.trans.pixel.protoc.MessageBoardProto.RequestQueryNoticeBoardCommand;
import com.trans.pixel.protoc.MessageBoardProto.RequestReplyMessageCommand;
import com.trans.pixel.protoc.MohuaProto.RequestEndMohuaMapCommand;
import com.trans.pixel.protoc.MohuaProto.RequestEnterMohuaMapCommand;
import com.trans.pixel.protoc.MohuaProto.RequestMohuaHpRewardCommand;
import com.trans.pixel.protoc.MohuaProto.RequestMohuaStageRewardCommand;
import com.trans.pixel.protoc.MohuaProto.RequestMohuaSubmitStageCommand;
import com.trans.pixel.protoc.MohuaProto.RequestStartMohuaMapCommand;
import com.trans.pixel.protoc.MohuaProto.RequestUseMohuaCardCommand;
import com.trans.pixel.protoc.PVPProto.RequestAttackPVPMineCommand;
import com.trans.pixel.protoc.PVPProto.RequestAttackPVPMonsterCommand;
import com.trans.pixel.protoc.PVPProto.RequestBrotherMineInfoCommand;
import com.trans.pixel.protoc.PVPProto.RequestHelpAttackPVPMineCommand;
import com.trans.pixel.protoc.PVPProto.RequestHelpLevelCommand;
import com.trans.pixel.protoc.PVPProto.RequestPVPInbreakListCommand;
import com.trans.pixel.protoc.PVPProto.RequestPVPMapListCommand;
import com.trans.pixel.protoc.PVPProto.RequestPVPMineInfoCommand;
import com.trans.pixel.protoc.PVPProto.RequestRefreshPVPMapCommand;
import com.trans.pixel.protoc.PVPProto.RequestRefreshPVPMineCommand;
import com.trans.pixel.protoc.PVPProto.RequestSendMailCommand;
import com.trans.pixel.protoc.PVPProto.RequestUnlockPVPMapCommand;
import com.trans.pixel.protoc.RechargeProto.RequestBindAccountCommand;
import com.trans.pixel.protoc.RechargeProto.RequestCanRechargeCommand;
import com.trans.pixel.protoc.RechargeProto.RequestCdkeyCommand;
import com.trans.pixel.protoc.RechargeProto.RequestCheatRechargeCommand;
import com.trans.pixel.protoc.RechargeProto.RequestGetGrowExpCommand;
import com.trans.pixel.protoc.RechargeProto.RequestGetGrowJewelCommand;
import com.trans.pixel.protoc.RechargeProto.RequestPurchaseVipLibaoCommand;
import com.trans.pixel.protoc.RechargeProto.RequestQueryRechargeCommand;
import com.trans.pixel.protoc.RechargeProto.RequestRechargeCommand;
import com.trans.pixel.protoc.RechargeProto.RequestSevenLoginSignCommand;
import com.trans.pixel.protoc.RechargeProto.RequestShouchongRewardCommand;
import com.trans.pixel.protoc.RechargeProto.RequestSignCommand;
import com.trans.pixel.protoc.RechargeProto.RequestSubmitIconCommand;
import com.trans.pixel.protoc.Request.RequestCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestChangePositionCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestCreateRewardTaskRoomCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestGiveupRewardTaskCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestInviteToRewardTaskRoomCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestLootRewardTaskCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestQuitRewardTaskRoomCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestRewardTaskRewardCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestSubmitRewardTaskScoreCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestUserRewardTaskCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestUserRewardTaskRoomCommand;
import com.trans.pixel.protoc.ShopProto.RequestBattletowerShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestBattletowerShopPurchaseCommand;
import com.trans.pixel.protoc.ShopProto.RequestBattletowerShopRefreshCommand;
import com.trans.pixel.protoc.ShopProto.RequestBlackShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestBlackShopPurchaseCommand;
import com.trans.pixel.protoc.ShopProto.RequestBlackShopRefreshCommand;
import com.trans.pixel.protoc.ShopProto.RequestDailyShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestDailyShopPurchaseCommand;
import com.trans.pixel.protoc.ShopProto.RequestDailyShopRefreshCommand;
import com.trans.pixel.protoc.ShopProto.RequestExpeditionShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestExpeditionShopPurchaseCommand;
import com.trans.pixel.protoc.ShopProto.RequestExpeditionShopRefreshCommand;
import com.trans.pixel.protoc.ShopProto.RequestLadderShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestLadderShopPurchaseCommand;
import com.trans.pixel.protoc.ShopProto.RequestLadderShopRefreshCommand;
import com.trans.pixel.protoc.ShopProto.RequestLibaoShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestPVPShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestPVPShopPurchaseCommand;
import com.trans.pixel.protoc.ShopProto.RequestPVPShopRefreshCommand;
import com.trans.pixel.protoc.ShopProto.RequestPurchaseCoinCommand;
import com.trans.pixel.protoc.ShopProto.RequestPurchaseContractCommand;
import com.trans.pixel.protoc.ShopProto.RequestRaidShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestRaidShopPurchaseCommand;
import com.trans.pixel.protoc.ShopProto.RequestRaidShopRefreshCommand;
import com.trans.pixel.protoc.ShopProto.RequestShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestShopPurchaseCommand;
import com.trans.pixel.protoc.ShopProto.RequestUnionShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestUnionShopPurchaseCommand;
import com.trans.pixel.protoc.ShopProto.RequestUnionShopRefreshCommand;
import com.trans.pixel.protoc.TaskProto.RequestGetTaskRewardCommand;
import com.trans.pixel.protoc.TaskProto.RequestOpenRaidCommand;
import com.trans.pixel.protoc.TaskProto.RequestStartRaidCommand;
import com.trans.pixel.protoc.TaskProto.RequestUserTaskCommand;
import com.trans.pixel.protoc.UnionProto.RequestApplyUnionCommand;
import com.trans.pixel.protoc.UnionProto.RequestAttackUnionCommand;
import com.trans.pixel.protoc.UnionProto.RequestBloodEnterCommand;
import com.trans.pixel.protoc.UnionProto.RequestBloodXiazhuCommand;
import com.trans.pixel.protoc.UnionProto.RequestBossRoomInfoCommand;
import com.trans.pixel.protoc.UnionProto.RequestBosskillCommand;
import com.trans.pixel.protoc.UnionProto.RequestCreateBossRoomCommand;
import com.trans.pixel.protoc.UnionProto.RequestCreateUnionCommand;
import com.trans.pixel.protoc.UnionProto.RequestDefendUnionCommand;
import com.trans.pixel.protoc.UnionProto.RequestGetBattletowerCommand;
import com.trans.pixel.protoc.UnionProto.RequestHandleUnionMemberCommand;
import com.trans.pixel.protoc.UnionProto.RequestInviteFightBossCommand;
import com.trans.pixel.protoc.UnionProto.RequestQuitFightBossCommand;
import com.trans.pixel.protoc.UnionProto.RequestQuitUnionCommand;
import com.trans.pixel.protoc.UnionProto.RequestReplyUnionCommand;
import com.trans.pixel.protoc.UnionProto.RequestResetBattletowerCommand;
import com.trans.pixel.protoc.UnionProto.RequestSearchUnionCommand;
import com.trans.pixel.protoc.UnionProto.RequestSetUnionAnnounceCommand;
import com.trans.pixel.protoc.UnionProto.RequestStartBossRoomCommand;
import com.trans.pixel.protoc.UnionProto.RequestSubmitBattletowerCommand;
import com.trans.pixel.protoc.UnionProto.RequestSubmitBossRoomScoreCommand;
import com.trans.pixel.protoc.UnionProto.RequestSubmitBosskillCommand;
import com.trans.pixel.protoc.UnionProto.RequestUnionBossFightCommand;
import com.trans.pixel.protoc.UnionProto.RequestUnionFightApplyCommand;
import com.trans.pixel.protoc.UnionProto.RequestUnionFightCommand;
import com.trans.pixel.protoc.UnionProto.RequestUnionInfoCommand;
import com.trans.pixel.protoc.UnionProto.RequestUnionListCommand;
import com.trans.pixel.protoc.UnionProto.RequestUpgradeUnionCommand;
import com.trans.pixel.protoc.UnionProto.RequestViewUnionFightFightInfoCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestBindRecommandCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestBuySavingBoxCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestChangeUserNameCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestEventBuyCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestEventCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestEventQuickFightCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestEventResultCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestExtraRewardCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestLevelLootResultCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestLevelStartCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestLoginCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestRecommandCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestRegisterCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestRemoveRecommandCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestSignNameCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestSubmitRiteCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestUserInfoCommand;
import com.trans.pixel.service.command.AchieveCommandService;
import com.trans.pixel.service.command.ActivityCommandService;
import com.trans.pixel.service.command.AreaCommandService;
import com.trans.pixel.service.command.BattletowerCommandService;
import com.trans.pixel.service.command.BossCommandService;
import com.trans.pixel.service.command.CdkeyCommandService;
import com.trans.pixel.service.command.CheatRechargeCommandService;
import com.trans.pixel.service.command.EquipCommandService;
import com.trans.pixel.service.command.EquipPokedeCommandService;
import com.trans.pixel.service.command.FriendCommandService;
import com.trans.pixel.service.command.HeartBeatCommandService;
import com.trans.pixel.service.command.HeroCommandService;
import com.trans.pixel.service.command.LadderCommandService;
import com.trans.pixel.service.command.LadderModeCommandService;
import com.trans.pixel.service.command.LevelCommandService;
import com.trans.pixel.service.command.LibaoCommandService;
import com.trans.pixel.service.command.LogCommandService;
import com.trans.pixel.service.command.LootRewardTaskCommandService;
import com.trans.pixel.service.command.LotteryCommandService;
import com.trans.pixel.service.command.MailCommandService;
import com.trans.pixel.service.command.MessageCommandService;
import com.trans.pixel.service.command.MohuaCommandService;
import com.trans.pixel.service.command.NoticeCommandService;
import com.trans.pixel.service.command.PokedeCommandService;
import com.trans.pixel.service.command.PropCommandService;
import com.trans.pixel.service.command.PvpCommandService;
import com.trans.pixel.service.command.RaidCommandService;
import com.trans.pixel.service.command.RankCommandService;
import com.trans.pixel.service.command.RechargeCommandService;
import com.trans.pixel.service.command.RewardTaskCommandService;
import com.trans.pixel.service.command.ShopCommandService;
import com.trans.pixel.service.command.SignCommandService;
import com.trans.pixel.service.command.TalentCommandService;
import com.trans.pixel.service.command.TaskCommandService;
import com.trans.pixel.service.command.TeamCommandService;
import com.trans.pixel.service.command.UnionCommandService;
import com.trans.pixel.service.command.UserCommandService;
import com.trans.pixel.service.command.ZhanliCommandService;
//add import here

@Service
public class GameDataScreen extends RequestScreen {
	@Resource
	private UserCommandService userCommandService;
	@Resource
	private LevelCommandService levelCommandService;
	@Resource
	private PvpCommandService pvpCommandService;
	@Resource
	private TeamCommandService teamCommandService;
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
	@Resource
	private TalentCommandService talentCommandService;
	@Resource
	private EquipPokedeCommandService equipPokedeCommandService;
	@Resource
	private RaidCommandService raidCommandService;
	@Resource
	private RewardTaskCommandService rewardTaskCommandService;
	@Resource
	private LadderModeCommandService ladderModeCommandService;
	@Resource
	private LootRewardTaskCommandService lootRewardTaskCommandService;
	
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
	protected boolean handleCommand(RequestLevelLootResultCommand cmd,
			Builder responseBuilder, UserBean user) {
		levelCommandService.levelLootResult(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestLevelStartCommand cmd, Builder responseBuilder, UserBean user) {
		levelCommandService.levelStart(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestEventResultCommand cmd,
			Builder responseBuilder, UserBean user) {
		levelCommandService.eventResult(cmd, responseBuilder, user);
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
	protected boolean handleCommand(RequestSetUnionAnnounceCommand cmd, Builder responseBuilder, UserBean user) {
		unionCommandService.setAnnounce(cmd, responseBuilder, user);
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


	@Override
	protected boolean handleCommand(RequestRaidShopCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.RaidShop(responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestRaidShopRefreshCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.RaidShopRefresh(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestRaidShopPurchaseCommand cmd, Builder responseBuilder, UserBean user) {
		shopCommandService.RaidShopPurchase(cmd, responseBuilder, user);
		return true;
	}
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
		rechargeCommandService.queryRecharge(cmd, responseBuilder, user);
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
	@Override//BloodEnterCommand
	protected boolean handleCommand(RequestBloodEnterCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO BloodEnterCommand method
		return true;//BloodEnterCommand
	}//BloodEnterCommand
	@Override//BloodXiazhuCommand
	protected boolean handleCommand(RequestBloodXiazhuCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO BloodXiazhuCommand method
		return true;//BloodXiazhuCommand
	}//BloodXiazhuCommand
	@Override//QueryNoticeBoardCommand
	protected boolean handleCommand(RequestQueryNoticeBoardCommand cmd, Builder responseBuilder, UserBean user) {
		messageCommandService.queryNoticeBoard(cmd, responseBuilder, user);
		return true;//QueryNoticeBoardCommand
	}//QueryNoticeBoardCommand
	@Override//HelpLevelCommand
	protected boolean handleCommand(RequestHelpLevelCommand cmd, Builder responseBuilder, UserBean user) {
		levelCommandService.helpLevelResult(cmd, responseBuilder, user);
		return true;//HelpLevelCommand
	}//HelpLevelCommand
	@Override//InviteFightBossCommand
	protected boolean handleCommand(RequestInviteFightBossCommand cmd, Builder responseBuilder, UserBean user) {
		bossCommandService.createFightBossRoom(cmd, responseBuilder, user);
		return true;//InviteFightBossCommand
	}//InviteFightBossCommand
	@Override//QuitFightBossCommand
	protected boolean handleCommand(RequestQuitFightBossCommand cmd, Builder responseBuilder, UserBean user) {
		bossCommandService.quitFightBossRoom(cmd, responseBuilder, user);
		return true;//QuitFightBossCommand
	}//QuitFightBossCommand
	@Override//SubmitBossScoreCommand
	protected boolean handleCommand(RequestSubmitBossRoomScoreCommand cmd, Builder responseBuilder, UserBean user) {
		bossCommandService.submitBossRoomScore(cmd, responseBuilder, user);
		return true;//SubmitBossScoreCommand
	}//SubmitBossScoreCommand
	@Override//UserInfoCommand
	protected boolean handleCommand(RequestUserInfoCommand cmd, Builder responseBuilder, UserBean user) {
		userCommandService.userInfo(cmd, responseBuilder, user);
		return true;//UserInfoCommand
	}//UserInfoCommand
	@Override//RechargeCommand
	protected boolean handleCommand(RequestRechargeCommand cmd, Builder responseBuilder, UserBean user) {
		rechargeCommandService.recharge(cmd, responseBuilder, user);
		return true;//RechargeCommand
	}//RechargeCommand
	@Override//TalentupgradeCommand
	protected boolean handleCommand(RequestTalentupgradeCommand cmd, Builder responseBuilder, UserBean user) {
		talentCommandService.talentupgrade(cmd, responseBuilder, user);
		return true;//TalentupgradeCommand
	}//TalentupgradeCommand
	@Override//TalentChangeUseCommand
	protected boolean handleCommand(RequestTalentChangeUseCommand cmd, Builder responseBuilder, UserBean user) {
		talentCommandService.talentChangeUse(cmd, responseBuilder, user);
		return true;//TalentChangeUseCommand
	}//TalentChangeUseCommand
	@Override//TalentChangeSkillCommand
	protected boolean handleCommand(RequestTalentChangeSkillCommand cmd, Builder responseBuilder, UserBean user) {
		talentCommandService.talentChangeSkill(cmd, responseBuilder, user);
		return true;//TalentChangeSkillCommand
	}//TalentChangeSkillCommand
	@Override//StartBossRoomCommand
	protected boolean handleCommand(RequestStartBossRoomCommand cmd, Builder responseBuilder, UserBean user) {
		bossCommandService.startBossRoom(cmd, responseBuilder, user);
		return true;//StartBossRoomCommand
	}//StartBossRoomCommand
	@Override//CreateBossRoomCommand
	protected boolean handleCommand(RequestCreateBossRoomCommand cmd, Builder responseBuilder, UserBean user) {
		bossCommandService.createBossRoom(cmd, responseBuilder, user);
		return true;//CreateBossRoomCommand
	}//CreateBossRoomCommand
	@Override//BossRoomInfoCommand
	protected boolean handleCommand(RequestBossRoomInfoCommand cmd, Builder responseBuilder, UserBean user) {
		bossCommandService.bossRoomInfo(responseBuilder, user);
		return true;//BossRoomInfoCommand
	}//BossRoomInfoCommand
	@Override//EquipStrenthenCommand
	protected boolean handleCommand(RequestEquipStrenthenCommand cmd, Builder responseBuilder, UserBean user) {
		equipPokedeCommandService.pokedeStrengthen(cmd, responseBuilder, user);
		return true;//EquipStrenthenCommand
	}//EquipStrenthenCommand
	@Override//EquipPokedeCommand
	protected boolean handleCommand(RequestEquipPokedeCommand cmd, Builder responseBuilder, UserBean user) {
		equipPokedeCommandService.getUserEquipPokedeList(cmd, responseBuilder, user);
		return true;//EquipPokedeCommand
	}//EquipPokedeCommand
	@Override//TalentChangeEquipCommand
	protected boolean handleCommand(RequestTalentChangeEquipCommand cmd, Builder responseBuilder, UserBean user) {
		talentCommandService.talentChangeEquip(cmd, responseBuilder, user);
		return true;//TalentChangeEquipCommand
	}//TalentChangeEquipCommand
	@Override//ZanHeroMessageBoardCommand
	protected boolean handleCommand(RequestZanHeroMessageBoardCommand cmd, Builder responseBuilder, UserBean user) {
		messageCommandService.zanHeroMessage(cmd, responseBuilder, user);
		return true;//ZanHeroMessageBoardCommand
	}//ZanHeroMessageBoardCommand
	@Override//OpenRaidCommand
	protected boolean handleCommand(RequestOpenRaidCommand cmd, Builder responseBuilder, UserBean user) {
		raidCommandService.openRaid(cmd, responseBuilder, user);
		return true;//OpenRaidCommand
	}//OpenRaidCommand
	@Override//RaidCommand
	protected boolean handleCommand(RequestStartRaidCommand cmd, Builder responseBuilder, UserBean user) {
		raidCommandService.startRaid(cmd, responseBuilder, user);
		return true;//RaidCommand
	}//RaidCommand
	@Override//CreateRewardTaskRoomCommand
	protected boolean handleCommand(RequestCreateRewardTaskRoomCommand cmd, Builder responseBuilder, UserBean user) {
		rewardTaskCommandService.createRoom(cmd, responseBuilder, user);
		return true;//CreateRewardTaskRoomCommand
	}//CreateRewardTaskRoomCommand
	@Override//QuitRewardTaskRoomCommand
	protected boolean handleCommand(RequestQuitRewardTaskRoomCommand cmd, Builder responseBuilder, UserBean user) {
		rewardTaskCommandService.quitRoom(cmd, responseBuilder, user);
		return true;//QuitRewardTaskRoomCommand
	}//QuitRewardTaskRoomCommand
	@Override//InviteToRewardTaskRoomCommand
	protected boolean handleCommand(RequestInviteToRewardTaskRoomCommand cmd, Builder responseBuilder, UserBean user) {
		rewardTaskCommandService.inviteRoom(cmd, responseBuilder, user);
		return true;//InviteToRewardTaskRoomCommand
	}//InviteToRewardTaskRoomCommand
	@Override//SubmitRewardTaskScoreCommand
	protected boolean handleCommand(RequestSubmitRewardTaskScoreCommand cmd, Builder responseBuilder, UserBean user) {
		rewardTaskCommandService.submitScore(cmd, responseBuilder, user);
		return true;//SubmitRewardTaskScoreCommand
	}//SubmitRewardTaskScoreCommand
	@Override//UserRewardTaskCommand
	protected boolean handleCommand(RequestUserRewardTaskCommand cmd, Builder responseBuilder, UserBean user) {
		rewardTaskCommandService.getUserRewardTask(cmd, responseBuilder, user);
		return true;//UserRewardTaskCommand
	}//UserRewardTaskCommand
	@Override//RewardTaskRewardCommand
	protected boolean handleCommand(RequestRewardTaskRewardCommand cmd, Builder responseBuilder, UserBean user) {
		rewardTaskCommandService.getUserRewardTaskReward(cmd, responseBuilder, user);
		return true;//RewardTaskRewardCommand
	}//RewardTaskRewardCommand
	@Override//UserRewardTaskRoomCommand
	protected boolean handleCommand(RequestUserRewardTaskRoomCommand cmd, Builder responseBuilder, UserBean user) {
		rewardTaskCommandService.getUserRewardTaskRoom(cmd, responseBuilder, user);
		return true;//UserRewardTaskRoomCommand
	}//UserRewardTaskRoomCommand
	//add handleCommand here
	
	@Override
	protected boolean handleCommand(RequestGetFightInfoCommand cmd, Builder responseBuilder, UserBean user) {
		teamCommandService.getFightInfo(cmd, responseBuilder, user);
		return true;
	}
	
	@Override
	protected boolean handleCommand(RequestFightInfoCommand cmd, Builder responseBuilder, UserBean user) {
		teamCommandService.submitFightInfo(cmd, responseBuilder, user);
		return true;
	}
	
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

	@Override
	protected boolean handleCommand(RequestSynthetiseComposeCommand cmd,
			Builder responseBuilder, UserBean user) {
		propCommandService.synthetiseCompose(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestEventCommand cmd, Builder responseBuilder, UserBean user) {
		levelCommandService.getEvent(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestGiveupRewardTaskCommand cmd,
			Builder responseBuilder, UserBean user) {
		rewardTaskCommandService.giveupRewardTask(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestExtraRewardCommand cmd,
			Builder responseBuilder, UserBean user) {
		userCommandService.extra(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestEventBuyCommand cmd,
			Builder responseBuilder, UserBean user) {
		levelCommandService.eventBuy(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestSubmitLadderResultCommand cmd,
			Builder responseBuilder, UserBean user) {
		ladderModeCommandService.submitLadderResult(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestRefreshLadderEnemyCommand cmd,
			Builder responseBuilder, UserBean user) {
		ladderModeCommandService.refreshLadderEnemy(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestLadderInfoCommand cmd,
			Builder responseBuilder, UserBean user) {
		ladderModeCommandService.ladderInfo(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestLadderTaskRewardCommand cmd,
			Builder responseBuilder, UserBean user) {
		ladderModeCommandService.ladderTaskReward(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestLadderSeasonRewardCommand cmd,
			Builder responseBuilder, UserBean user) {
		ladderModeCommandService.ladderSeasonReward(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestLadderEnemyCommand cmd,
			Builder responseBuilder, UserBean user) {
		ladderModeCommandService.ladderenemy(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestHeroSpUpCommand cmd, Builder responseBuilder, UserBean user) {
		heroCommandService.heroSpUp(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestBuySavingBoxCommand cmd,
			Builder responseBuilder, UserBean user) {
		levelCommandService.buySavingBox(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestEquipupCommand cmd,
			Builder responseBuilder, UserBean user) {
		equipPokedeCommandService.equipup(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestMaterialComposeCommand cmd,
			Builder responseBuilder, UserBean user) {
		propCommandService.materialCompose(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestPVPInbreakListCommand cmd, Builder responseBuilder, UserBean user) {
		pvpCommandService.getInbreakList(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestUseMaterialCommand cmd,
			Builder responseBuilder, UserBean user) {
		equipCommandService.useMaterial(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestChangePositionCommand cmd,
			Builder responseBuilder, UserBean user) {
		rewardTaskCommandService.changePosition(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestTalentSpUpCommand cmd,
			Builder responseBuilder, UserBean user) {
		talentCommandService.talentSpUp(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestTalentSkillLevelupCommand cmd,
			Builder responseBuilder, UserBean user) {
		talentCommandService.talentSkillLevelup(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestTalentResetSkillCommand cmd,
			Builder responseBuilder, UserBean user) {
		talentCommandService.talentResetSkill(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestRecommandCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO RequestRecommandCommand method
		userCommandService.recommand(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestBindRecommandCommand cmd,
			Builder responseBuilder, UserBean user) {
		userCommandService.bindRecommand(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestChangeUserNameCommand cmd,
			Builder responseBuilder, UserBean user) {
		userCommandService.changeName(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestCipherRewardCommand cmd,
			Builder responseBuilder, UserBean user) {
		cdkeyCommandService.cipherReward(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestEventQuickFightCommand cmd,
			Builder responseBuilder, UserBean user) {
		levelCommandService.eventQuickFight(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestSignNameCommand cmd,
			Builder responseBuilder, UserBean user) {
		userCommandService.signName(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestSubmitRiteCommand cmd,
			Builder responseBuilder, UserBean user) {
		userCommandService.submitRite(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestSearchUnionCommand cmd, Builder responseBuilder, UserBean user) {
		unionCommandService.searchUnion(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestRemoveRecommandCommand cmd,
			Builder responseBuilder, UserBean user) {
		userCommandService.removeRecommand(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestSaveFightInfoCommand cmd,
			Builder responseBuilder, UserBean user) {
		teamCommandService.saveFightInfo(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestUnionFightApplyCommand cmd,
			Builder responseBuilder, UserBean user) {
		unionCommandService.applyFight(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestLootRewardTaskCommand cmd,
			Builder responseBuilder, UserBean user) {
		lootRewardTaskCommandService.lootRewardTask(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestUnionFightCommand cmd,
			Builder responseBuilder, UserBean user) {
		unionCommandService.unionFight(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestViewUnionFightFightInfoCommand cmd,
			Builder responseBuilder, UserBean user) {
		unionCommandService.viewFightInfo(cmd, responseBuilder, user);
		return true;
	}

	@Override
	protected boolean handleCommand(RequestCanRechargeCommand cmd, Builder responseBuilder, UserBean user) {
		rechargeCommandService.canRecharge(cmd, responseBuilder, user);
		return true;
	}

}
