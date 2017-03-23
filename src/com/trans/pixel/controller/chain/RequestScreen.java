package com.trans.pixel.controller.chain;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.ActivityProto.RequestAchieveListCommand;
import com.trans.pixel.protoc.ActivityProto.RequestAchieveRewardCommand;
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
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.EquipProto.RequestAddHeroEquipCommand;
import com.trans.pixel.protoc.EquipProto.RequestEquipComposeCommand;
import com.trans.pixel.protoc.EquipProto.RequestEquipPokedeCommand;
import com.trans.pixel.protoc.EquipProto.RequestEquipStrenthenCommand;
import com.trans.pixel.protoc.EquipProto.RequestFenjieEquipCommand;
import com.trans.pixel.protoc.EquipProto.RequestSaleEquipCommand;
import com.trans.pixel.protoc.EquipProto.RequestSubmitZhanliCommand;
import com.trans.pixel.protoc.EquipProto.RequestTalentChangeEquipCommand;
import com.trans.pixel.protoc.EquipProto.RequestUsePropCommand;
import com.trans.pixel.protoc.HeroProto.RequestBuyHeroPackageCommand;
import com.trans.pixel.protoc.HeroProto.RequestChoseClearInfoCommand;
import com.trans.pixel.protoc.HeroProto.RequestClearHeroCommand;
import com.trans.pixel.protoc.HeroProto.RequestFeedFoodCommand;
import com.trans.pixel.protoc.HeroProto.RequestFenjieHeroCommand;
import com.trans.pixel.protoc.HeroProto.RequestGetTeamCommand;
import com.trans.pixel.protoc.HeroProto.RequestHeroLevelUpCommand;
import com.trans.pixel.protoc.HeroProto.RequestHeroLevelUpToCommand;
import com.trans.pixel.protoc.HeroProto.RequestHeroStrengthenCommand;
import com.trans.pixel.protoc.HeroProto.RequestLockHeroCommand;
import com.trans.pixel.protoc.HeroProto.RequestOpenFetterCommand;
import com.trans.pixel.protoc.HeroProto.RequestResetHeroSkillCommand;
import com.trans.pixel.protoc.HeroProto.RequestSubmitComposeSkillCommand;
import com.trans.pixel.protoc.HeroProto.RequestTalentChangeSkillCommand;
import com.trans.pixel.protoc.HeroProto.RequestTalentChangeUseCommand;
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
import com.trans.pixel.protoc.LadderProto.RequestPurchaseLadderTimeCommand;
import com.trans.pixel.protoc.LadderProto.RequestReadyAttackLadderCommand;
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
import com.trans.pixel.protoc.PVPProto.RequestPVPMapListCommand;
import com.trans.pixel.protoc.PVPProto.RequestPVPMineInfoCommand;
import com.trans.pixel.protoc.PVPProto.RequestRefreshPVPMapCommand;
import com.trans.pixel.protoc.PVPProto.RequestRefreshPVPMineCommand;
import com.trans.pixel.protoc.PVPProto.RequestSendMailCommand;
import com.trans.pixel.protoc.PVPProto.RequestUnlockPVPMapCommand;
import com.trans.pixel.protoc.RechargeProto.RequestBindAccountCommand;
import com.trans.pixel.protoc.RechargeProto.RequestBuyLootPackageCommand;
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
import com.trans.pixel.protoc.RewardTaskProto.RequestCreateRewardTaskRoomCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestInviteToRewardTaskRoomCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestQuitRewardTaskRoomCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestRewardTaskRewardCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestSubmitRewardTaskScoreCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestUserRewardTaskCommand;
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
import com.trans.pixel.protoc.UnionProto.RequestStartBossRoomCommand;
import com.trans.pixel.protoc.UnionProto.RequestSubmitBattletowerCommand;
import com.trans.pixel.protoc.UnionProto.RequestSubmitBossRoomScoreCommand;
import com.trans.pixel.protoc.UnionProto.RequestSubmitBosskillCommand;
import com.trans.pixel.protoc.UnionProto.RequestUnionBossFightCommand;
import com.trans.pixel.protoc.UnionProto.RequestUnionInfoCommand;
import com.trans.pixel.protoc.UnionProto.RequestUnionListCommand;
import com.trans.pixel.protoc.UnionProto.RequestUpgradeUnionCommand;
import com.trans.pixel.protoc.UserInfoProto.HeadInfo;
import com.trans.pixel.protoc.UserInfoProto.RequestEventResultCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestLevelLootResultCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestLevelStartCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestLoginCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestRegisterCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestUserInfoCommand;
import com.trans.pixel.service.ServerService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.command.PushCommandService;
import com.trans.pixel.utils.TypeTranslatedUtil;
//add import here
//add import here


public abstract class RequestScreen implements RequestHandle {
	private static final Logger log = LoggerFactory.getLogger(RequestScreen.class);
	@Resource
    private UserService userService;
	@Resource
	private ServerService serverService;
	@Resource
	private PushCommandService pushCommandService;
	
//	private RequestHandle nullUserErrorHandle = new NullUserErrorHandle();
	
	protected abstract boolean handleRegisterCommand(RequestCommand cmd, Builder responseBuilder);
	
	protected abstract boolean handleLoginCommand(RequestCommand cmd, Builder responseBuilder);
	
	protected abstract boolean handleCommand(RequestLevelStartCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestEventResultCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestLevelLootResultCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestHeroLevelUpCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestAddHeroEquipCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestUpdateTeamCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestLotteryCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestGetLadderRankListCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestAttackLadderModeCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestGetUserMailListCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestReadMailCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestDeleteMailCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestAddFriendCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestReceiveFriendCommand cmd, Builder responseBuilder, UserBean user);

	protected abstract boolean handleCommand(RequestAreaCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestAttackBossCommand cmd, Builder responseBuilder, UserBean user);

	protected abstract boolean handleCommand(RequestAttackMonsterCommand cmd, Builder responseBuilder, UserBean user);

	protected abstract boolean handleCommand(RequestAttackResourceCommand cmd, Builder responseBuilder, UserBean user);

	protected abstract boolean handleCommand(RequestAttackResourceMineCommand cmd, Builder responseBuilder, UserBean user);
    
	protected abstract boolean handleCommand(RequestRegisterCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestLoginCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestGetUserLadderRankListCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestApplyUnionCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestCreateUnionCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestReplyUnionCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestMessageBoardListCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestCreateMessageBoardCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestReplyMessageCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestHandleUnionMemberCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestUnionInfoCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestUpgradeUnionCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestUnionListCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestQuitUnionCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestUserTeamListCommand cmd, Builder responseBuilder, UserBean user);

	protected abstract boolean handleCommand(RequestAttackUnionCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestGetLadderUserInfoCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestGetUserFriendListCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestEquipComposeCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestUsePropCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestFenjieEquipCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestSignCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestHelpAttackPVPMineCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestFenjieHeroCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestResetHeroSkillCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestSendMailCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestBrotherMineInfoCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestEnterMohuaMapCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestStartMohuaMapCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestEndMohuaMapCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestUseMohuaCardCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestMohuaStageRewardCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestMohuaHpRewardCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestSaleEquipCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestDelFriendCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestSubmitZhanliCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestAchieveRewardCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestAchieveListCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestRichangRewardCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestRichangListCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestKaifu2ActivityCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestKaifuRewardCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestKaifuListCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestUserPokedeCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestRankCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean pushNoticeCommand(Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestDefendUnionCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestDailyShopCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestDailyShopPurchaseCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestDailyShopRefreshCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestShopCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestShopPurchaseCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestBlackShopCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestBlackShopPurchaseCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestBlackShopRefreshCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestUnionShopCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestUnionShopPurchaseCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestUnionShopRefreshCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestLadderShopCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestLadderShopPurchaseCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestLadderShopRefreshCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestPVPShopCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestPVPShopPurchaseCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestPVPShopRefreshCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestExpeditionShopCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestExpeditionShopPurchaseCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestExpeditionShopRefreshCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestPurchaseCoinCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestAttackResourceMineInfoCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestGetTeamCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestAttackPVPMonsterCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestPVPMapListCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestPVPMineInfoCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestAttackPVPMineCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestRefreshPVPMineCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestLockHeroCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestMohuaSubmitStageCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestUnlockAreaCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestUnlockPVPMapCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestCollectResourceMineCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestAreaResourceCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestUseAreaEquipCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestBuyHeroPackageCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestSubmitComposeSkillCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestBuyLootPackageCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestCdkeyCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestSubmitIconCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestCheatRechargeCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestRefreshPVPMapCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestReadyAttackLadderCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestBindAccountCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestPurchaseVipLibaoCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestQueryRechargeCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestLibaoShopCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestShouchongRewardCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestHeartBeatCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestGreenhandCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestLogCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestGetGrowJewelCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestGetGrowExpCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestFeedFoodCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestClearHeroCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestChoseClearInfoCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestSubmitBosskillCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestBosskillCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestUnionBossFightCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestHeroStrengthenCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestSevenLoginSignCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestIsAreaOwnerCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestOpenFetterCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestUserTaskCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestGetTaskRewardCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestSubmitBattletowerCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestResetBattletowerCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestGetBattletowerCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestBattletowerShopCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestBattletowerShopPurchaseCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestBattletowerShopRefreshCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestBloodEnterCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestBloodXiazhuCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestQueryNoticeBoardCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestHelpLevelCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestInviteFightBossCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestQuitFightBossCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestSubmitBossRoomScoreCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestUserInfoCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestRechargeCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestTalentupgradeCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestTalentChangeUseCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestTalentChangeSkillCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestStartBossRoomCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestCreateBossRoomCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestBossRoomInfoCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestEquipStrenthenCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestEquipPokedeCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestTalentChangeEquipCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestOpenRaidCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestStartRaidCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestZanHeroMessageBoardCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestCreateRewardTaskRoomCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestQuitRewardTaskRoomCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestInviteToRewardTaskRoomCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestSubmitRewardTaskScoreCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestUserRewardTaskCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestRewardTaskRewardCommand cmd, Builder responseBuilder, UserBean user);
	//add handleCommand here
	
	protected abstract boolean handleCommand(RequestGetFightInfoCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestFightInfoCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestRefreshAreaCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestPurchaseContractCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestPurchaseLadderTimeCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestHeroLevelUpToCommand cmd, Builder responseBuilder, UserBean user);
	
	private HeadInfo buildHeadInfo(HeadInfo head) {
		HeadInfo.Builder nHead = HeadInfo.newBuilder(head);
		nHead.setServerstarttime(serverService.getKaifuTime(head.getServerId()));
		nHead.setDatetime(System.currentTimeMillis() / 1000);
		nHead.setOnlineStatus(serverService.getOnlineStatus("" + head.getVersion()));
		nHead.setGameVersion(serverService.getGameVersion());
		
		return nHead.build();
	}

	@Override
    public boolean handleRequest(PixelRequest req, PixelResponse rep) {
    	RequestCommand request = req.command;
        HeadInfo head = buildHeadInfo(request.getHead());
        rep.command.setHead(head);
        ResponseCommand.Builder responseBuilder = rep.command;
        UserBean user = null;

        if (request.hasRegisterCommand()) {
        	handleRegisterCommand(request, responseBuilder);
        	return false;
        } else if (request.hasLoginCommand()) {
        	handleLoginCommand(request, responseBuilder);
        	return false;
        } else {
	        long userId = head.getUserId();
	        req.user = userService.getUser(userId);
        	user = req.user;

	        if (request.hasLogCommand()) {
	            RequestLogCommand cmd = request.getLogCommand();
	            handleCommand(cmd, responseBuilder, user);//LogCommand
	        }//LogCommand
	        
        	if (user == null || !user.getSession().equals(head.getSession())) {
	        	ErrorCommand.Builder erBuilder = ErrorCommand.newBuilder();
	            erBuilder.setCode(String.valueOf(ErrorConst.USER_NEED_LOGIN.getCode()));
	            erBuilder.setMessage(ErrorConst.USER_NEED_LOGIN.getMesssage());
	            if(!request.hasLogCommand())
	        		rep.command.setErrorCommand(erBuilder.build());
	            log.info("cmd user need login:" + req);
	            return false;
	        }
        }

        boolean result = true;
        if (request.hasAddHeroEquipCommand()) {
        	RequestAddHeroEquipCommand cmd = request.getAddHeroEquipCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasHeroLevelUpCommand()) {
        	RequestHeroLevelUpCommand cmd = request.getHeroLevelUpCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasLevelLootResultCommand()) {
        	RequestLevelLootResultCommand cmd = request.getLevelLootResultCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasLevelStartCommand()) {
        	RequestLevelStartCommand cmd = request.getLevelStartCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasEventResultCommand()) {
        	RequestEventResultCommand cmd = request.getEventResultCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasUpdateTeamCommand()) {
        	RequestUpdateTeamCommand cmd = request.getUpdateTeamCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasLotteryCommand()) {
        	RequestLotteryCommand cmd = request.getLotteryCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasGetLadderRankListCommand()) {
        	RequestGetLadderRankListCommand cmd = request.getGetLadderRankListCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasGetUserMailListCommand()) {
        	RequestGetUserMailListCommand cmd = request.getGetUserMailListCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasReadMailCommand()) {
        	RequestReadMailCommand cmd = request.getReadMailCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasDeleteMailCommand()) {
        	RequestDeleteMailCommand cmd = request.getDeleteMailCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasAddFriendCommand()) {
        	RequestAddFriendCommand cmd = request.getAddFriendCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasReceiveFriendCommand()) {
        	RequestReceiveFriendCommand cmd = request.getReceiveFriendCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasAreaCommand()) {
        	RequestAreaCommand cmd = request.getAreaCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasAttackMonsterCommand()) {
        	RequestAttackMonsterCommand cmd = request.getAttackMonsterCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasAttackBossCommand()) {
        	RequestAttackBossCommand cmd = request.getAttackBossCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasAttackResourceCommand()) {
        	RequestAttackResourceCommand cmd = request.getAttackResourceCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasAttackResourceMineCommand()) {
        	RequestAttackResourceMineCommand cmd = request.getAttackResourceMineCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
//        if (request.hasRegisterCommand()) {
//            RequestRegisterCommand cmd = request.getRegisterCommand();
//            if (result)
//                result = handleCommand(cmd, responseBuilder, user);
//        }
//        if (request.hasLoginCommand()) {
//            RequestLoginCommand cmd = request.getLoginCommand();
//            if (result)
//                result = handleCommand(cmd, responseBuilder, user);
//        }
        if (request.hasGetUserLadderRankListCommand()) {
            RequestGetUserLadderRankListCommand cmd = request.getGetUserLadderRankListCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasApplyUnionCommand()) {
            RequestApplyUnionCommand cmd = request.getApplyUnionCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasCreateUnionCommand()) {
            RequestCreateUnionCommand cmd = request.getCreateUnionCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasReplyUnionCommand()) {
            RequestReplyUnionCommand cmd = request.getReplyUnionCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasMessageBoardListCommand()) {
            RequestMessageBoardListCommand cmd = request.getMessageBoardListCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasCreateMessageBoardCommand()) {
            RequestCreateMessageBoardCommand cmd = request.getCreateMessageBoardCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasReplyMessageCommand()) {
            RequestReplyMessageCommand cmd = request.getReplyMessageCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasHandleUnionCommand()) {
            RequestHandleUnionMemberCommand cmd = request.getHandleUnionCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasUnionInfoCommand()) {
            RequestUnionInfoCommand cmd = request.getUnionInfoCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasUpgradeUnionCommand()) {
            RequestUpgradeUnionCommand cmd = request.getUpgradeUnionCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasUnionListCommand()) {
            RequestUnionListCommand cmd = request.getUnionListCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasQuitUnionCommand()) {
            RequestQuitUnionCommand cmd = request.getQuitUnionCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasUserTeamListCommand()) {
            RequestUserTeamListCommand cmd = request.getUserTeamListCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasAttackUnionCommand()) {
            RequestAttackUnionCommand cmd = request.getAttackUnionCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasDefendUnionCommand()) {
            RequestDefendUnionCommand cmd = request.getDefendUnionCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasLadderUserInfoCommand()) {
        	RequestGetLadderUserInfoCommand cmd = request.getLadderUserInfoCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasUserFriendListCommand()) {
        	RequestGetUserFriendListCommand cmd = request.getUserFriendListCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasEquipComposeCommand()) {
        	RequestEquipComposeCommand cmd = request.getEquipComposeCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasUsePropCommand()) {
        	RequestUsePropCommand cmd = request.getUsePropCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasFenjieEquipCommand()) {
        	RequestFenjieEquipCommand cmd = request.getFenjieEquipCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasSignCommand()) {
        	RequestSignCommand cmd = request.getSignCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasHelpAttackPVPMineCommand()) {
        	RequestHelpAttackPVPMineCommand cmd = request.getHelpAttackPVPMineCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasFenjieHeroCommand()) {
        	RequestFenjieHeroCommand cmd = request.getFenjieHeroCommand();
        	if (result)
        		result = handleCommand(cmd,responseBuilder, user);
        }
        if (request.hasResetHeroSkillCommand()) {
        	RequestResetHeroSkillCommand cmd = request.getResetHeroSkillCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasSendMailCommand()) {
        	RequestSendMailCommand cmd = request.getSendMailCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasBrotherMineInfoCommand()) {
        	RequestBrotherMineInfoCommand cmd = request.getBrotherMineInfoCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasEnterMohuaMapCommand()) {
        	RequestEnterMohuaMapCommand cmd = request.getEnterMohuaMapCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasStartMohuaMapCommand()) {
        	RequestStartMohuaMapCommand cmd = request.getStartMohuaMapCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasEndMohuaMapCommand()) {
        	RequestEndMohuaMapCommand cmd = request.getEndMohuaMapCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasUseMohuaCardCommand()) {
        	RequestUseMohuaCardCommand cmd = request.getUseMohuaCardCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasMohuaStageRewardCommand()) {
        	RequestMohuaStageRewardCommand cmd = request.getMohuaStageRewardCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasMohuaHpRewardCommand()) {
        	RequestMohuaHpRewardCommand cmd = request.getMohuaHpRewardCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasSaleEquipCommand()) {
        	RequestSaleEquipCommand cmd = request.getSaleEquipCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasDelFriendCommand()) {
        	RequestDelFriendCommand cmd = request.getDelFriendCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasSubmitZhanliCommand()) {
        	RequestSubmitZhanliCommand cmd = request.getSubmitZhanliCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasAchieveRewardCommand()) {
        	RequestAchieveRewardCommand cmd = request.getAchieveRewardCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasAchieveListCommand()) {
        	RequestAchieveListCommand cmd = request.getAchieveListCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasRichangRewardCommand()) {
        	RequestRichangRewardCommand cmd = request.getRichangRewardCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasRichangListCommand()) {
        	RequestRichangListCommand cmd = request.getRichangListCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasKaifu2ActivityCommand()) {
        	RequestKaifu2ActivityCommand cmd = request.getKaifu2ActivityCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasKaifuRewardCommand()) {
        	RequestKaifuRewardCommand cmd = request.getKaifuRewardCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasKaifuListCommand()) {
        	RequestKaifuListCommand cmd = request.getKaifuListCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasUserPokedeCommand()) {
        	RequestUserPokedeCommand cmd = request.getUserPokedeCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasRankCommand()) {
        	RequestRankCommand cmd = request.getRankCommand();
        	if (result)
        		result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasDailyShopCommand()) {
            RequestDailyShopCommand cmd = request.getDailyShopCommand();
            if (result)//DailyShopCommand
                result = handleCommand(cmd, responseBuilder, user);//DailyShopCommand
        }//DailyShopCommand
        if (request.hasDailyShopPurchaseCommand()) {
            RequestDailyShopPurchaseCommand cmd = request.getDailyShopPurchaseCommand();
            if (result)//DailyShopPurchaseCommand
                result = handleCommand(cmd, responseBuilder, user);//DailyShopPurchaseCommand
        }//DailyShopPurchaseCommand
        if (request.hasDailyShopRefreshCommand()) {
            RequestDailyShopRefreshCommand cmd = request.getDailyShopRefreshCommand();
            if (result)//DailyShopRefreshCommand
                result = handleCommand(cmd, responseBuilder, user);//DailyShopRefreshCommand
        }//DailyShopRefreshCommand
        if (request.hasShopCommand()) {
            RequestShopCommand cmd = request.getShopCommand();
            if (result)//ShopCommand
                result = handleCommand(cmd, responseBuilder, user);//ShopCommand
        }//ShopCommand
        if (request.hasShopPurchaseCommand()) {
            RequestShopPurchaseCommand cmd = request.getShopPurchaseCommand();
            if (result)//ShopPurchaseCommand
                result = handleCommand(cmd, responseBuilder, user);//ShopPurchaseCommand
        }//ShopPurchaseCommand
        if (request.hasBlackShopCommand()) {
            RequestBlackShopCommand cmd = request.getBlackShopCommand();
            if (result)//BlackShopCommand
                result = handleCommand(cmd, responseBuilder, user);//BlackShopCommand
        }//BlackShopCommand
        if (request.hasBlackShopPurchaseCommand()) {
            RequestBlackShopPurchaseCommand cmd = request.getBlackShopPurchaseCommand();
            if (result)//BlackShopPurchaseCommand
                result = handleCommand(cmd, responseBuilder, user);//BlackShopPurchaseCommand
        }//BlackShopPurchaseCommand
        if (request.hasBlackShopRefreshCommand()) {
            RequestBlackShopRefreshCommand cmd = request.getBlackShopRefreshCommand();
            if (result)//BlackShopRefreshCommand
                result = handleCommand(cmd, responseBuilder, user);//BlackShopRefreshCommand
        }//BlackShopRefreshCommand
        if (request.hasUnionShopCommand()) {
            RequestUnionShopCommand cmd = request.getUnionShopCommand();
            if (result)//UnionShopCommand
                result = handleCommand(cmd, responseBuilder, user);//UnionShopCommand
        }//UnionShopCommand
        if (request.hasUnionShopPurchaseCommand()) {
            RequestUnionShopPurchaseCommand cmd = request.getUnionShopPurchaseCommand();
            if (result)//UnionShopPurchaseCommand
                result = handleCommand(cmd, responseBuilder, user);//UnionShopPurchaseCommand
        }//UnionShopPurchaseCommand
        if (request.hasUnionShopRefreshCommand()) {
            RequestUnionShopRefreshCommand cmd = request.getUnionShopRefreshCommand();
            if (result)//UnionShopRefreshCommand
                result = handleCommand(cmd, responseBuilder, user);//UnionShopRefreshCommand
        }//UnionShopRefreshCommand
        if (request.hasLadderShopCommand()) {
            RequestLadderShopCommand cmd = request.getLadderShopCommand();
            if (result)//LadderShopCommand
                result = handleCommand(cmd, responseBuilder, user);//LadderShopCommand
        }//LadderShopCommand
        if (request.hasLadderShopPurchaseCommand()) {
            RequestLadderShopPurchaseCommand cmd = request.getLadderShopPurchaseCommand();
            if (result)//LadderShopPurchaseCommand
                result = handleCommand(cmd, responseBuilder, user);//LadderShopPurchaseCommand
        }//LadderShopPurchaseCommand
        if (request.hasLadderShopRefreshCommand()) {
            RequestLadderShopRefreshCommand cmd = request.getLadderShopRefreshCommand();
            if (result)//LadderShopRefreshCommand
                result = handleCommand(cmd, responseBuilder, user);//LadderShopRefreshCommand
        }//LadderShopRefreshCommand
        if (request.hasPVPShopCommand()) {
            RequestPVPShopCommand cmd = request.getPVPShopCommand();
            if (result)//PVPShopCommand
                result = handleCommand(cmd, responseBuilder, user);//PVPShopCommand
        }//PVPShopCommand
        if (request.hasPVPShopPurchaseCommand()) {
            RequestPVPShopPurchaseCommand cmd = request.getPVPShopPurchaseCommand();
            if (result)//PVPShopPurchaseCommand
                result = handleCommand(cmd, responseBuilder, user);//PVPShopPurchaseCommand
        }//PVPShopPurchaseCommand
        if (request.hasPVPShopRefreshCommand()) {
            RequestPVPShopRefreshCommand cmd = request.getPVPShopRefreshCommand();
            if (result)//PVPShopRefreshCommand
                result = handleCommand(cmd, responseBuilder, user);//PVPShopRefreshCommand
        }//PVPShopRefreshCommand
        if (request.hasExpeditionShopCommand()) {
            RequestExpeditionShopCommand cmd = request.getExpeditionShopCommand();
            if (result)//ExpeditionShopCommand
                result = handleCommand(cmd, responseBuilder, user);//ExpeditionShopCommand
        }//ExpeditionShopCommand
        if (request.hasExpeditionShopPurchaseCommand()) {
            RequestExpeditionShopPurchaseCommand cmd = request.getExpeditionShopPurchaseCommand();
            if (result)//ExpeditionShopPurchaseCommand
                result = handleCommand(cmd, responseBuilder, user);//ExpeditionShopPurchaseCommand
        }//ExpeditionShopPurchaseCommand
        if (request.hasExpeditionShopRefreshCommand()) {
            RequestExpeditionShopRefreshCommand cmd = request.getExpeditionShopRefreshCommand();
            if (result)//ExpeditionShopRefreshCommand
                result = handleCommand(cmd, responseBuilder, user);//ExpeditionShopRefreshCommand
        }//ExpeditionShopRefreshCommand
        if (request.hasPurchaseCoinCommand()) {
            RequestPurchaseCoinCommand cmd = request.getPurchaseCoinCommand();
            if (result)//PurchaseCoinCommand
                result = handleCommand(cmd, responseBuilder, user);//PurchaseCoinCommand
        }//PurchaseCoinCommand
        if (request.hasAttackResourceMineInfoCommand()) {
            RequestAttackResourceMineInfoCommand cmd = request.getAttackResourceMineInfoCommand();
            if (result)//AttackResourceMineInfoCommand
                result = handleCommand(cmd, responseBuilder, user);//AttackResourceMineInfoCommand
        }//AttackResourceMineInfoCommand
        if (request.hasTeamCommand()) {
            RequestGetTeamCommand cmd = request.getTeamCommand();
            if (result)//UserMineTeamCommand
                result = handleCommand(cmd, responseBuilder, user);//UserMineTeamCommand
        }//UserMineTeamCommand
        if (request.hasAttackPVPMonsterCommand()) {
            RequestAttackPVPMonsterCommand cmd = request.getAttackPVPMonsterCommand();
            if (result)//AttackPVPMonstersCommand
                result = handleCommand(cmd, responseBuilder, user);//AttackPVPMonstersCommand
        }//AttackPVPMonstersCommand
        if (request.hasPvpMapListCommand()) {
            RequestPVPMapListCommand cmd = request.getPvpMapListCommand();
            if (result)//PvpMapListCommand
                result = handleCommand(cmd, responseBuilder, user);//PvpMapListCommand
        }//PvpMapListCommand
        if (request.hasPvpMineInfoCommand()) {
            RequestPVPMineInfoCommand cmd = request.getPvpMineInfoCommand();
            if (result)//PvpMineInfoCommand
                result = handleCommand(cmd, responseBuilder, user);//PvpMineInfoCommand
        }//PvpMineInfoCommand
        if (request.hasAttackPVPMineCommand()) {
            RequestAttackPVPMineCommand cmd = request.getAttackPVPMineCommand();
            if (result)//AttackPVPMineCommand
                result = handleCommand(cmd, responseBuilder, user);//AttackPVPMineCommand
        }//AttackPVPMineCommand
        if (request.hasRefreshPVPMineCommand()) {
            RequestRefreshPVPMineCommand cmd = request.getRefreshPVPMineCommand();
            if (result)//RefreshPVPMineCommand
                result = handleCommand(cmd, responseBuilder, user);//RefreshPVPMineCommand
        }//RefreshPVPMineCommand
        if (request.hasLockHeroCommand()) {
            RequestLockHeroCommand cmd = request.getLockHeroCommand();
            if (result)//LockHeroCommand
                result = handleCommand(cmd, responseBuilder, user);//LockHeroCommand
        }//LockHeroCommand
        if (request.hasMohuaSubmitStageCommand()) {
            RequestMohuaSubmitStageCommand cmd = request.getMohuaSubmitStageCommand();
            if (result)//MohuaSubmitStageCommand
                result = handleCommand(cmd, responseBuilder, user);//MohuaSubmitStageCommand
        }//MohuaSubmitStageCommand
        if (request.hasUnlockAreaCommand()) {
            RequestUnlockAreaCommand cmd = request.getUnlockAreaCommand();
            if (result)//UnlockAreaCommand
                result = handleCommand(cmd, responseBuilder, user);//UnlockAreaCommand
        }//UnlockAreaCommand
        if (request.hasUnlockPvpMapCommand()) {
            RequestUnlockPVPMapCommand cmd = request.getUnlockPvpMapCommand();
            if (result)//UnlockPvpMapCommand
                result = handleCommand(cmd, responseBuilder, user);//UnlockPvpMapCommand
        }//UnlockPvpMapCommand
        if (request.hasCollectResourceMineCommand()) {
            RequestCollectResourceMineCommand cmd = request.getCollectResourceMineCommand();
            if (result)//CollectResourceMineCommand
                result = handleCommand(cmd, responseBuilder, user);//CollectResourceMineCommand
        }//CollectResourceMineCommand
        if (request.hasAreaResourceCommand()) {
            RequestAreaResourceCommand cmd = request.getAreaResourceCommand();
            if (result)//AreaResourceCommand
                result = handleCommand(cmd, responseBuilder, user);//AreaResourceCommand
        }//AreaResourceCommand
        if (request.hasUseAreaEquipCommand()) {
            RequestUseAreaEquipCommand cmd = request.getUseAreaEquipCommand();
            if (result)//UseAreaEquipCommand
                result = handleCommand(cmd, responseBuilder, user);//UseAreaEquipCommand
        }//UseAreaEquipCommand
        if (request.hasBuyHeroPackageCommand()) {
            RequestBuyHeroPackageCommand cmd = request.getBuyHeroPackageCommand();
            if (result)//BuyHeroPackageCommand
                result = handleCommand(cmd, responseBuilder, user);//BuyHeroPackageCommand
        }//BuyHeroPackageCommand
        if (request.hasSubmitComposeSkillCommand()) {
            RequestSubmitComposeSkillCommand cmd = request.getSubmitComposeSkillCommand();
            if (result)//SubmitComposeSkillCommand
                result = handleCommand(cmd, responseBuilder, user);//SubmitComposeSkillCommand
        }//SubmitComposeSkillCommand
        if (request.hasBuyLootPackageCommand()) {
            RequestBuyLootPackageCommand cmd = request.getBuyLootPackageCommand();
            if (result)//BuyLootPackageCommand
                result = handleCommand(cmd, responseBuilder, user);//BuyLootPackageCommand
        }//BuyLootPackageCommand
        if (request.hasCdkeyCommand()) {
            RequestCdkeyCommand cmd = request.getCdkeyCommand();
            if (result)//CdkeyCommand
                result = handleCommand(cmd, responseBuilder, user);//CdkeyCommand
        }//CdkeyCommand
        if (request.hasSubmitIconCommand()) {
            RequestSubmitIconCommand cmd = request.getSubmitIconCommand();
            if (result)//SubmitIconCommand
                result = handleCommand(cmd, responseBuilder, user);//SubmitIconCommand
        }//SubmitIconCommand
        if (request.hasCheatrechargeCommand()) {
            RequestCheatRechargeCommand cmd = request.getCheatrechargeCommand();
            if (result)//RechargeCommand
                result = handleCommand(cmd, responseBuilder, user);//RechargeCommand
        }//RechargeCommand
        if (request.hasRefreshPvpMapCommand()) {
            RequestRefreshPVPMapCommand cmd = request.getRefreshPvpMapCommand();
            if (result)//RefreshPvpMapCommand
                result = handleCommand(cmd, responseBuilder, user);//RefreshPvpMapCommand
        }//RefreshPvpMapCommand
        if (request.hasReadyAttackLadderCommand()) {
            RequestReadyAttackLadderCommand cmd = request.getReadyAttackLadderCommand();
            if (result)//ReadyAttackLadderCommand
                result = handleCommand(cmd, responseBuilder, user);//ReadyAttackLadderCommand
        }//ReadyAttackLadderCommand
        if (request.hasAttackLadderModeCommand()) {
        	RequestAttackLadderModeCommand cmd = request.getAttackLadderModeCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasBindAccountCommand()) {
            RequestBindAccountCommand cmd = request.getBindAccountCommand();
            if (result)//BindAccountCommand
                result = handleCommand(cmd, responseBuilder, user);//BindAccountCommand
        }//BindAccountCommand
        if (request.hasPurchaseVipLibaoCommand()) {
            RequestPurchaseVipLibaoCommand cmd = request.getPurchaseVipLibaoCommand();
            if (result)//PurchaseVipLibaoCommand
                result = handleCommand(cmd, responseBuilder, user);//PurchaseVipLibaoCommand
        }//PurchaseVipLibaoCommand
        if (request.hasQueryRechargeCommand()) {
            RequestQueryRechargeCommand cmd = request.getQueryRechargeCommand();
            if (result)//QueryRechargeCommand
                result = handleCommand(cmd, responseBuilder, user);//QueryRechargeCommand
        }//QueryRechargeCommand
        if (request.hasLibaoShopCommand()) {
            RequestLibaoShopCommand cmd = request.getLibaoShopCommand();
            if (result)//LibaoShopCommand
                result = handleCommand(cmd, responseBuilder, user);//LibaoShopCommand
        }//LibaoShopCommand
        if (request.hasShouchongRewardCommand()) {
            RequestShouchongRewardCommand cmd = request.getShouchongRewardCommand();
            if (result)//ShouchongRewardCommand
                result = handleCommand(cmd, responseBuilder, user);//ShouchongRewardCommand
        }//ShouchongRewardCommand
        if (request.hasHeartBeatCommand()) {
            RequestHeartBeatCommand cmd = request.getHeartBeatCommand();
            if (result)//HeartBeatCommand
                result = handleCommand(cmd, responseBuilder, user);//HeartBeatCommand
        }//HeartBeatCommand
        if (request.hasGreenhandCommand()) {
            RequestGreenhandCommand cmd = request.getGreenhandCommand();
            if (result)//GreenhandCommand
                result = handleCommand(cmd, responseBuilder, user);//GreenhandCommand
        }//GreenhandCommand
        if (request.hasGetGrowJewelCommand()) {
            RequestGetGrowJewelCommand cmd = request.getGetGrowJewelCommand();
            if (result)//GetGrowJewelCommand
                result = handleCommand(cmd, responseBuilder, user);//GetGrowJewelCommand
        }//GetGrowJewelCommand
        if (request.hasGetGrowExpCommand()) {
            RequestGetGrowExpCommand cmd = request.getGetGrowExpCommand();
            if (result)//GetGrowExpCommand
                result = handleCommand(cmd, responseBuilder, user);//GetGrowExpCommand
        }//GetGrowExpCommand
        if (request.hasFeedFoodCommand()) {
            RequestFeedFoodCommand cmd = request.getFeedFoodCommand();
            if (result)//FeedFoodCommand
                result = handleCommand(cmd, responseBuilder, user);//FeedFoodCommand
        }//FeedFoodCommand
        if (request.hasClearHeroCommand()) {
            RequestClearHeroCommand cmd = request.getClearHeroCommand();
            if (result)//ClearHeroCommand
                result = handleCommand(cmd, responseBuilder, user);//ClearHeroCommand
        }//ClearHeroCommand
        if (request.hasChoseClearInfoCommand()) {
            RequestChoseClearInfoCommand cmd = request.getChoseClearInfoCommand();
            if (result)//ChoseClearInfoCommand
                result = handleCommand(cmd, responseBuilder, user);//ChoseClearInfoCommand
        }//ChoseClearInfoCommand
        if (request.hasSubmitBosskillCommand()) {
            RequestSubmitBosskillCommand cmd = request.getSubmitBosskillCommand();
            if (result)//SubmitBosskillCommand
                result = handleCommand(cmd, responseBuilder, user);//SubmitBosskillCommand
        }//SubmitBosskillCommand
        if (request.hasBosskillCommand()) {
            RequestBosskillCommand cmd = request.getBosskillCommand();
            if (result)//BosskillCommand
                result = handleCommand(cmd, responseBuilder, user);//BosskillCommand
        }//BosskillCommand
        if (request.hasUnionBossFightCommand()) {
            RequestUnionBossFightCommand cmd = request.getUnionBossFightCommand();
            if (result)//UnionBossFightCommand
                result = handleCommand(cmd, responseBuilder, user);//UnionBossFightCommand
        }//UnionBossFightCommand
        if (request.hasHeroStrengthenCommand()) {
            RequestHeroStrengthenCommand cmd = request.getHeroStrengthenCommand();
            if (result)//HeroStrengthenCommand
                result = handleCommand(cmd, responseBuilder, user);//HeroStrengthenCommand
        }//HeroStrengthenCommand
        if (request.hasSevenLoginSignCommand()) {
            RequestSevenLoginSignCommand cmd = request.getSevenLoginSignCommand();
            if (result)//SevenLoginSignCommand
                result = handleCommand(cmd, responseBuilder, user);//SevenLoginSignCommand
        }//SevenLoginSignCommand
        if (request.hasIsAreaOwnerCommand()) {
            RequestIsAreaOwnerCommand cmd = request.getIsAreaOwnerCommand();
            if (result)//IsAreaOwnerCommand
                result = handleCommand(cmd, responseBuilder, user);//IsAreaOwnerCommand
        }//IsAreaOwnerCommand
        if (request.hasOpenFetterCommand()) {
            RequestOpenFetterCommand cmd = request.getOpenFetterCommand();
            if (result)//OpenFetterCommand
                result = handleCommand(cmd, responseBuilder, user);//OpenFetterCommand
        }//OpenFetterCommand
        if (request.hasUserTaskCommand()) {
            RequestUserTaskCommand cmd = request.getUserTaskCommand();
            if (result)//UserTaskCommand
                result = handleCommand(cmd, responseBuilder, user);//UserTaskCommand
        }//UserTaskCommand
        if (request.hasGetTaskRewardCommand()) {
            RequestGetTaskRewardCommand cmd = request.getGetTaskRewardCommand();
            if (result)//GetTaskRewardCommand
                result = handleCommand(cmd, responseBuilder, user);//GetTaskRewardCommand
        }//GetTaskRewardCommand
        if (request.hasSubmitBattletowerCommand()) {
            RequestSubmitBattletowerCommand cmd = request.getSubmitBattletowerCommand();
            if (result)//SubmitBattletowerCommand
                result = handleCommand(cmd, responseBuilder, user);//SubmitBattletowerCommand
        }//SubmitBattletowerCommand
        if (request.hasResetBattletowerCommand()) {
            RequestResetBattletowerCommand cmd = request.getResetBattletowerCommand();
            if (result)//ResetBattletowerCommand
                result = handleCommand(cmd, responseBuilder, user);//ResetBattletowerCommand
        }//ResetBattletowerCommand
        if (request.hasGetBattletowerCommand()) {
            RequestGetBattletowerCommand cmd = request.getGetBattletowerCommand();
            if (result)//GetBattletowerCommand
                result = handleCommand(cmd, responseBuilder, user);//GetBattletowerCommand
        }//GetBattletowerCommand
        if (request.hasBattletowerShopCommand()) {
            RequestBattletowerShopCommand cmd = request.getBattletowerShopCommand();
            if (result)//BattletowerShopCommand
                result = handleCommand(cmd, responseBuilder, user);//BattletowerShopCommand
        }//BattletowerShopCommand
        if (request.hasBattletowerShopPurchaseCommand()) {
            RequestBattletowerShopPurchaseCommand cmd = request.getBattletowerShopPurchaseCommand();
            if (result)//BattletowerShopPurchaseCommand
                result = handleCommand(cmd, responseBuilder, user);//BattletowerShopPurchaseCommand
        }//BattletowerShopPurchaseCommand
        if (request.hasBattletowerShopRefreshCommand()) {
            RequestBattletowerShopRefreshCommand cmd = request.getBattletowerShopRefreshCommand();
            if (result)//BattletowerShopRefreshCommand
                result = handleCommand(cmd, responseBuilder, user);//BattletowerShopRefreshCommand
        }//BattletowerShopRefreshCommand
        if (request.hasBloodEnterCommand()) {
            RequestBloodEnterCommand cmd = request.getBloodEnterCommand();
            if (result)//BloodEnterCommand
                result = handleCommand(cmd, responseBuilder, user);//BloodEnterCommand
        }//BloodEnterCommand
        if (request.hasBloodXiazhuCommand()) {
            RequestBloodXiazhuCommand cmd = request.getBloodXiazhuCommand();
            if (result)//BloodXiazhuCommand
                result = handleCommand(cmd, responseBuilder, user);//BloodXiazhuCommand
        }//BloodXiazhuCommand
        if (request.hasQueryNoticeBoardCommand()) {
            RequestQueryNoticeBoardCommand cmd = request.getQueryNoticeBoardCommand();
            if (result)//QueryNoticeBoardCommand
                result = handleCommand(cmd, responseBuilder, user);//QueryNoticeBoardCommand
        }//QueryNoticeBoardCommand
        if (request.hasHelpLevelCommand()) {
            RequestHelpLevelCommand cmd = request.getHelpLevelCommand();
            if (result)//HelpLevelCommand
                result = handleCommand(cmd, responseBuilder, user);//HelpLevelCommand
        }//HelpLevelCommand
        if (request.hasInviteFightBossCommand()) {
            RequestInviteFightBossCommand cmd = request.getInviteFightBossCommand();
            if (result)//InviteFightBossCommand
                result = handleCommand(cmd, responseBuilder, user);//InviteFightBossCommand
        }//InviteFightBossCommand
        if (request.hasQuitFightBossCommand()) {
            RequestQuitFightBossCommand cmd = request.getQuitFightBossCommand();
            if (result)//QuitFightBossCommand
                result = handleCommand(cmd, responseBuilder, user);//QuitFightBossCommand
        }//QuitFightBossCommand
        if (request.hasSubmitBossScoreCommand()) {
            RequestSubmitBossRoomScoreCommand cmd = request.getSubmitBossScoreCommand();
            if (result)//SubmitBossScoreCommand
                result = handleCommand(cmd, responseBuilder, user);//SubmitBossScoreCommand
        }//SubmitBossScoreCommand
        if (request.hasUserInfoCommand()) {
            RequestUserInfoCommand cmd = request.getUserInfoCommand();
            if (result)//UserInfoCommand
                result = handleCommand(cmd, responseBuilder, user);//UserInfoCommand
        }//UserInfoCommand
        if (request.hasRechargeCommand()) {
            RequestRechargeCommand cmd = request.getRechargeCommand();
            if (result)//RechargeCommand
                result = handleCommand(cmd, responseBuilder, user);//RechargeCommand
        }//RechargeCommand
        if (request.hasTalentupgradeCommand()) {
            RequestTalentupgradeCommand cmd = request.getTalentupgradeCommand();
            if (result)//TalentupgradeCommand
                result = handleCommand(cmd, responseBuilder, user);//TalentupgradeCommand
        }//TalentupgradeCommand
        if (request.hasTalentChangeUseCommand()) {
            RequestTalentChangeUseCommand cmd = request.getTalentChangeUseCommand();
            if (result)//TalentChangeUseCommand
                result = handleCommand(cmd, responseBuilder, user);//TalentChangeUseCommand
        }//TalentChangeUseCommand
        if (request.hasTalentChangeSkillCommand()) {
            RequestTalentChangeSkillCommand cmd = request.getTalentChangeSkillCommand();
            if (result)//TalentChangeSkillCommand
                result = handleCommand(cmd, responseBuilder, user);//TalentChangeSkillCommand
        }//TalentChangeSkillCommand
        if (request.hasStartBossRoomCommand()) {
            RequestStartBossRoomCommand cmd = request.getStartBossRoomCommand();
            if (result)//StartBossRoomCommand
                result = handleCommand(cmd, responseBuilder, user);//StartBossRoomCommand
        }//StartBossRoomCommand
        if (request.hasCreateBossRoomCommand()) {
            RequestCreateBossRoomCommand cmd = request.getCreateBossRoomCommand();
            if (result)//CreateBossRoomCommand
                result = handleCommand(cmd, responseBuilder, user);//CreateBossRoomCommand
        }//CreateBossRoomCommand
        if (request.hasBossRoomInfoCommand()) {
            RequestBossRoomInfoCommand cmd = request.getBossRoomInfoCommand();
            if (result)//BossRoomInfoCommand
                result = handleCommand(cmd, responseBuilder, user);//BossRoomInfoCommand
        }//BossRoomInfoCommand
        if (request.hasEquipStrenthenCommand()) {
            RequestEquipStrenthenCommand cmd = request.getEquipStrenthenCommand();
            if (result)//EquipStrenthenCommand
                result = handleCommand(cmd, responseBuilder, user);//EquipStrenthenCommand
        }//EquipStrenthenCommand
        if (request.hasEquipPokedeCommand()) {
            RequestEquipPokedeCommand cmd = request.getEquipPokedeCommand();
            if (result)//EquipPokedeCommand
                result = handleCommand(cmd, responseBuilder, user);//EquipPokedeCommand
        }//EquipPokedeCommand
        if (request.hasTalentChangeEquipCommand()) {
            RequestTalentChangeEquipCommand cmd = request.getTalentChangeEquipCommand();
            if (result)//TalentChangeEquipCommand
                result = handleCommand(cmd, responseBuilder, user);//TalentChangeEquipCommand
        }//TalentChangeEquipCommand
        if (request.hasZanHeroMessageBoardCommand()) {
            RequestZanHeroMessageBoardCommand cmd = request.getZanHeroMessageBoardCommand();
            if (result)//ZanHeroMessageBoardCommand
                result = handleCommand(cmd, responseBuilder, user);//ZanHeroMessageBoardCommand
        }//ZanHeroMessageBoardCommand
        if (request.hasOpenRaidCommand()) {
            RequestOpenRaidCommand cmd = request.getOpenRaidCommand();
            if (result)//OpenRaidCommand
                result = handleCommand(cmd, responseBuilder, user);//OpenRaidCommand
        }//OpenRaidCommand
        if (request.hasRaidCommand()) {
            RequestStartRaidCommand cmd = request.getRaidCommand();
            if (result)//RaidCommand
                result = handleCommand(cmd, responseBuilder, user);//RaidCommand
        }//RaidCommand
        if (request.hasCreateRewardTaskRoomCommand()) {
            RequestCreateRewardTaskRoomCommand cmd = request.getCreateRewardTaskRoomCommand();
            if (result)//CreateRewardTaskRoomCommand
                result = handleCommand(cmd, responseBuilder, user);//CreateRewardTaskRoomCommand
        }//CreateRewardTaskRoomCommand
        if (request.hasQuitRewardTaskRoomCommand()) {
            RequestQuitRewardTaskRoomCommand cmd = request.getQuitRewardTaskRoomCommand();
            if (result)//QuitRewardTaskRoomCommand
                result = handleCommand(cmd, responseBuilder, user);//QuitRewardTaskRoomCommand
        }//QuitRewardTaskRoomCommand
        if (request.hasInviteToRewardTaskRoomCommand()) {
            RequestInviteToRewardTaskRoomCommand cmd = request.getInviteToRewardTaskRoomCommand();
            if (result)//InviteToRewardTaskRoomCommand
                result = handleCommand(cmd, responseBuilder, user);//InviteToRewardTaskRoomCommand
        }//InviteToRewardTaskRoomCommand
        if (request.hasSubmitRewardTaskScoreCommand()) {
            RequestSubmitRewardTaskScoreCommand cmd = request.getSubmitRewardTaskScoreCommand();
            if (result)//SubmitRewardTaskScoreCommand
                result = handleCommand(cmd, responseBuilder, user);//SubmitRewardTaskScoreCommand
        }//SubmitRewardTaskScoreCommand
        if (request.hasUserRewardTaskCommand()) {
            RequestUserRewardTaskCommand cmd = request.getUserRewardTaskCommand();
            if (result)//UserRewardTaskCommand
                result = handleCommand(cmd, responseBuilder, user);//UserRewardTaskCommand
        }//UserRewardTaskCommand
        if (request.hasRewardTaskRewardCommand()) {
            RequestRewardTaskRewardCommand cmd = request.getRewardTaskRewardCommand();
            if (result)//RewardTaskRewardCommand
                result = handleCommand(cmd, responseBuilder, user);//RewardTaskRewardCommand
        }//RewardTaskRewardCommand
        //call handleCommand here
        if (request.hasGetFightInfoCommand()) {
            RequestGetFightInfoCommand cmd = request.getGetFightInfoCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasFightInfoCommand()) {
            RequestFightInfoCommand cmd = request.getFightInfoCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasRefreshAreaCommand()) {
            RequestRefreshAreaCommand cmd = request.getRefreshAreaCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasPurchaseContractCommand()) {
            RequestPurchaseContractCommand cmd = request.getPurchaseContractCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasPurchaseLadderTimeCommand()) {
            RequestPurchaseLadderTimeCommand cmd = request.getPurchaseLadderTimeCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasHeroLevelUpToCommand()) {
            RequestHeroLevelUpToCommand cmd = request.getHeroLevelUpToCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        
        	if (responseBuilder.hasErrorCommand()) {
        		int errorCode = TypeTranslatedUtil.stringToInt(responseBuilder.getErrorCommand().getCode());
        		if (errorCode == ErrorConst.NOT_ENOUGH_COIN.getCode() || errorCode == ErrorConst.NOT_ENOUGH_EXP.getCode()
        				|| errorCode == ErrorConst.NOT_ENOUGH_JEWEL.getCode())
        			pushCommandService.pushUserInfoCommand(responseBuilder, user);
        	}
        
	        /**
	    	 * push notice
	    	 */
	    	if (result && user != null && !request.hasQueryRechargeCommand() && !request.hasLogCommand())
	    		pushNoticeCommand(responseBuilder, user);
        
        return result;
	}
}
