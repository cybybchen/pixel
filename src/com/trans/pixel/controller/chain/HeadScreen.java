package com.trans.pixel.controller.chain;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.HeadInfo;
import com.trans.pixel.protoc.Commands.RequestAchieveListCommand;
import com.trans.pixel.protoc.Commands.RequestAchieveRewardCommand;
import com.trans.pixel.protoc.Commands.RequestAddFriendCommand;
import com.trans.pixel.protoc.Commands.RequestAddHeroEquipCommand;
import com.trans.pixel.protoc.Commands.RequestAddTeamCommand;
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
import com.trans.pixel.protoc.Commands.RequestBlackShopCommand;
import com.trans.pixel.protoc.Commands.RequestBlackShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestBlackShopRefreshCommand;
import com.trans.pixel.protoc.Commands.RequestBrotherMineInfoCommand;
import com.trans.pixel.protoc.Commands.RequestBuyHeroPackageCommand;
import com.trans.pixel.protoc.Commands.RequestBuyLootPackageCommand;
import com.trans.pixel.protoc.Commands.RequestCdkeyCommand;
import com.trans.pixel.protoc.Commands.RequestCheatRechargeCommand;
import com.trans.pixel.protoc.Commands.RequestRefreshPVPMapCommand;
import com.trans.pixel.protoc.Commands.RequestReadyAttackLadderCommand;
import com.trans.pixel.protoc.Commands.RequestBindAccountCommand;
import com.trans.pixel.protoc.Commands.RequestPurchaseVipLibaoCommand;
//add import here
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
import com.trans.pixel.protoc.Commands.RequestFenjieEquipCommand;
import com.trans.pixel.protoc.Commands.RequestFenjieHeroCommand;
import com.trans.pixel.protoc.Commands.RequestFenjieHeroEquipCommand;
import com.trans.pixel.protoc.Commands.RequestGetLadderRankListCommand;
import com.trans.pixel.protoc.Commands.RequestGetLadderUserInfoCommand;
import com.trans.pixel.protoc.Commands.RequestGetTeamCommand;
import com.trans.pixel.protoc.Commands.RequestGetUserFriendListCommand;
import com.trans.pixel.protoc.Commands.RequestGetUserLadderRankListCommand;
import com.trans.pixel.protoc.Commands.RequestGetUserMailListCommand;
import com.trans.pixel.protoc.Commands.RequestHandleUnionMemberCommand;
import com.trans.pixel.protoc.Commands.RequestHelpAttackPVPMineCommand;
import com.trans.pixel.protoc.Commands.RequestHeroLevelUpCommand;
import com.trans.pixel.protoc.Commands.RequestKaifu2ActivityCommand;
import com.trans.pixel.protoc.Commands.RequestKaifu2RewardCommand;
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
import com.trans.pixel.protoc.Commands.RequestLockHeroCommand;
import com.trans.pixel.protoc.Commands.RequestLoginCommand;
import com.trans.pixel.protoc.Commands.RequestLootResultCommand;
import com.trans.pixel.protoc.Commands.RequestLotteryCommand;
import com.trans.pixel.protoc.Commands.RequestMessageBoardListCommand;
import com.trans.pixel.protoc.Commands.RequestMohuaHpRewardCommand;
import com.trans.pixel.protoc.Commands.RequestMohuaStageRewardCommand;
import com.trans.pixel.protoc.Commands.RequestMohuaSubmitStageCommand;
import com.trans.pixel.protoc.Commands.RequestPVPMapListCommand;
import com.trans.pixel.protoc.Commands.RequestPVPMineInfoCommand;
import com.trans.pixel.protoc.Commands.RequestPVPShopCommand;
import com.trans.pixel.protoc.Commands.RequestPVPShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestPVPShopRefreshCommand;
import com.trans.pixel.protoc.Commands.RequestPurchaseCoinCommand;
import com.trans.pixel.protoc.Commands.RequestQuitUnionCommand;
import com.trans.pixel.protoc.Commands.RequestRankCommand;
import com.trans.pixel.protoc.Commands.RequestReadMailCommand;
import com.trans.pixel.protoc.Commands.RequestReceiveFriendCommand;
import com.trans.pixel.protoc.Commands.RequestRefreshPVPMineCommand;
import com.trans.pixel.protoc.Commands.RequestRegisterCommand;
import com.trans.pixel.protoc.Commands.RequestReplyMessageCommand;
import com.trans.pixel.protoc.Commands.RequestReplyUnionCommand;
import com.trans.pixel.protoc.Commands.RequestResetHeroSkillCommand;
import com.trans.pixel.protoc.Commands.RequestRichangListCommand;
import com.trans.pixel.protoc.Commands.RequestRichangRewardCommand;
import com.trans.pixel.protoc.Commands.RequestSaleEquipCommand;
import com.trans.pixel.protoc.Commands.RequestSendMailCommand;
import com.trans.pixel.protoc.Commands.RequestShopCommand;
import com.trans.pixel.protoc.Commands.RequestShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestSignCommand;
import com.trans.pixel.protoc.Commands.RequestStartMohuaMapCommand;
import com.trans.pixel.protoc.Commands.RequestSubmitComposeSkillCommand;
import com.trans.pixel.protoc.Commands.RequestCdkeyCommand;
import com.trans.pixel.protoc.Commands.RequestCheatRechargeCommand;
import com.trans.pixel.protoc.Commands.RequestRefreshPVPMapCommand;
import com.trans.pixel.protoc.Commands.RequestReadyAttackLadderCommand;
import com.trans.pixel.protoc.Commands.RequestBindAccountCommand;
import com.trans.pixel.protoc.Commands.RequestPurchaseVipLibaoCommand;
//add import here
import com.trans.pixel.protoc.Commands.RequestSubmitZhanliCommand;
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
import com.trans.pixel.protoc.Commands.RequestUserTeamListCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.UserService;
import com.trans.pixel.protoc.Commands.RequestBuyHeroPackageCommand;
import com.trans.pixel.protoc.Commands.RequestSubmitComposeSkillCommand;
import com.trans.pixel.protoc.Commands.RequestBuyLootPackageCommand;
import com.trans.pixel.protoc.Commands.RequestSubmitIconCommand;
import com.trans.pixel.protoc.Commands.RequestCheatRechargeCommand;
import com.trans.pixel.protoc.Commands.RequestRefreshPVPMapCommand;
import com.trans.pixel.protoc.Commands.RequestReadyAttackLadderCommand;
import com.trans.pixel.protoc.Commands.RequestBindAccountCommand;
import com.trans.pixel.protoc.Commands.RequestPurchaseVipLibaoCommand;
//add import here

public class HeadScreen extends RequestScreen {
	
	private static final Logger log = LoggerFactory.getLogger(HeadScreen.class);
	
	@Resource
    private UserService userService;
	
	@Override
    public boolean handleRequest(PixelRequest req, PixelResponse rep) {
        RequestCommand request = req.command;
        HeadInfo.Builder head = HeadInfo.newBuilder(request.getHead());
        head.setDatetime(System.currentTimeMillis() / 1000);
        rep.command.setHead(head.build());
        if (request.hasRegisterCommand() || request.hasLoginCommand()) {
            return true;
        }
        long userId = head.getUserId();
        req.user = userService.getUser(userId);
        
        if (req.user == null/* || !req.user.getSession().equals(head.getSession())*/) {
        	ErrorCommand.Builder erBuilder = ErrorCommand.newBuilder();
            erBuilder.setCode(String.valueOf(ErrorConst.USER_NEED_LOGIN.getCode()));
            erBuilder.setMessage(ErrorConst.USER_NEED_LOGIN.getMesssage());
        	rep.command.setErrorCommand(erBuilder.build());
            log.info("cmd user need login:" + req);
            return false;
        }
        
        return true;
    }

	@Override
	protected boolean handleRegisterCommand(RequestCommand cmd,
			Builder responseBuilder) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleLoginCommand(RequestCommand cmd,
			Builder responseBuilder) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestLevelResultCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestLevelStartCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestLevelLootStartCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestLevelLootResultCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestLootResultCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestHeroLevelUpCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestAddHeroEquipCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestUpdateTeamCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestLotteryCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestGetLadderRankListCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestAttackLadderModeCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestGetUserMailListCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestReadMailCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestDeleteMailCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestAddFriendCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestReceiveFriendCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestEquipLevelUpCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestAreaCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestAttackBossCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestAttackMonsterCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestAttackResourceCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestAttackResourceMineCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
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
	protected boolean handleCommand(RequestHandleUnionMemberCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	protected boolean handleCommand(RequestUnionInfoCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	protected boolean handleCommand(RequestUpgradeUnionCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	protected boolean handleCommand(RequestUnionListCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	protected boolean handleCommand(RequestQuitUnionCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	protected boolean handleCommand(RequestAttackUnionCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	protected boolean handleCommand(RequestDefendUnionCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}
	@Override//DailyShopCommand
	protected boolean handleCommand(RequestDailyShopCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO DailyShopCommand method
		return true;//DailyShopCommand
	}//DailyShopCommand
	@Override//DailyShopPurchaseCommand
	protected boolean handleCommand(RequestDailyShopPurchaseCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO DailyShopPurchaseCommand method
		return true;//DailyShopPurchaseCommand
	}//DailyShopPurchaseCommand
	@Override//DailyShopRefreshCommand
	protected boolean handleCommand(RequestDailyShopRefreshCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO DailyShopRefreshCommand method
		return true;//DailyShopRefreshCommand
	}//DailyShopRefreshCommand
	@Override//ShopCommand
	protected boolean handleCommand(RequestShopCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO ShopCommand method
		return true;//ShopCommand
	}//ShopCommand
	@Override//ShopPurchaseCommand
	protected boolean handleCommand(RequestShopPurchaseCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO ShopPurchaseCommand method
		return true;//ShopPurchaseCommand
	}//ShopPurchaseCommand
	@Override//BlackShopCommand
	protected boolean handleCommand(RequestBlackShopCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO BlackShopCommand method
		return true;//BlackShopCommand
	}//BlackShopCommand
	@Override//BlackShopPurchaseCommand
	protected boolean handleCommand(RequestBlackShopPurchaseCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO BlackShopPurchaseCommand method
		return true;//BlackShopPurchaseCommand
	}//BlackShopPurchaseCommand
	@Override//BlackShopRefreshCommand
	protected boolean handleCommand(RequestBlackShopRefreshCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO BlackShopRefreshCommand method
		return true;//BlackShopRefreshCommand
	}//BlackShopRefreshCommand
	@Override//UnionShopCommand
	protected boolean handleCommand(RequestUnionShopCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO UnionShopCommand method
		return true;//UnionShopCommand
	}//UnionShopCommand
	@Override//UnionShopPurchaseCommand
	protected boolean handleCommand(RequestUnionShopPurchaseCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO UnionShopPurchaseCommand method
		return true;//UnionShopPurchaseCommand
	}//UnionShopPurchaseCommand
	@Override//UnionShopRefreshCommand
	protected boolean handleCommand(RequestUnionShopRefreshCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO UnionShopRefreshCommand method
		return true;//UnionShopRefreshCommand
	}//UnionShopRefreshCommand
	@Override//LadderShopCommand
	protected boolean handleCommand(RequestLadderShopCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO LadderShopCommand method
		return true;//LadderShopCommand
	}//LadderShopCommand
	@Override//LadderShopPurchaseCommand
	protected boolean handleCommand(RequestLadderShopPurchaseCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO LadderShopPurchaseCommand method
		return true;//LadderShopPurchaseCommand
	}//LadderShopPurchaseCommand
	@Override//LadderShopRefreshCommand
	protected boolean handleCommand(RequestLadderShopRefreshCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO LadderShopRefreshCommand method
		return true;//LadderShopRefreshCommand
	}//LadderShopRefreshCommand
	@Override//PVPShopCommand
	protected boolean handleCommand(RequestPVPShopCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO PVPShopCommand method
		return true;//PVPShopCommand
	}//PVPShopCommand
	@Override//PVPShopPurchaseCommand
	protected boolean handleCommand(RequestPVPShopPurchaseCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO PVPShopPurchaseCommand method
		return true;//PVPShopPurchaseCommand
	}//PVPShopPurchaseCommand
	@Override//PVPShopRefreshCommand
	protected boolean handleCommand(RequestPVPShopRefreshCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO PVPShopRefreshCommand method
		return true;//PVPShopRefreshCommand
	}//PVPShopRefreshCommand
	@Override//ExpeditionShopCommand
	protected boolean handleCommand(RequestExpeditionShopCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO ExpeditionShopCommand method
		return true;//ExpeditionShopCommand
	}//ExpeditionShopCommand
	@Override//ExpeditionShopPurchaseCommand
	protected boolean handleCommand(RequestExpeditionShopPurchaseCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO ExpeditionShopPurchaseCommand method
		return true;//ExpeditionShopPurchaseCommand
	}//ExpeditionShopPurchaseCommand
	@Override//ExpeditionShopRefreshCommand
	protected boolean handleCommand(RequestExpeditionShopRefreshCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO ExpeditionShopRefreshCommand method
		return true;//ExpeditionShopRefreshCommand
	}//ExpeditionShopRefreshCommand
	@Override//PurchaseCoinCommand
	protected boolean handleCommand(RequestPurchaseCoinCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO PurchaseCoinCommand method
		return true;//PurchaseCoinCommand
	}//PurchaseCoinCommand
	@Override//AttackResourceMineInfoCommand
	protected boolean handleCommand(RequestAttackResourceMineInfoCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO AttackResourceMineInfoCommand method
		return true;//AttackResourceMineInfoCommand
	}//AttackResourceMineInfoCommand
	@Override//UserMineTeamCommand
	protected boolean handleCommand(RequestGetTeamCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO UserMineTeamCommand method
		return true;//UserMineTeamCommand
	}//UserMineTeamCommand
	@Override//AttackPVPMonstersCommand
	protected boolean handleCommand(RequestAttackPVPMonsterCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO AttackPVPMonstersCommand method
		return true;//AttackPVPMonstersCommand
	}//AttackPVPMonstersCommand
	@Override//PvpMapListCommand
	protected boolean handleCommand(RequestPVPMapListCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO PvpMapListCommand method
		return true;//PvpMapListCommand
	}//PvpMapListCommand
	@Override//PvpMineInfoCommand
	protected boolean handleCommand(RequestPVPMineInfoCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO PvpMineInfoCommand method
		return true;//PvpMineInfoCommand
	}//PvpMineInfoCommand
	@Override//AttackPVPMineCommand
	protected boolean handleCommand(RequestAttackPVPMineCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO AttackPVPMineCommand method
		return true;//AttackPVPMineCommand
	}//AttackPVPMineCommand
	@Override//RefreshPVPMineCommand
	protected boolean handleCommand(RequestRefreshPVPMineCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO RefreshPVPMineCommand method
		return true;//RefreshPVPMineCommand
	}//RefreshPVPMineCommand
	@Override//LockHeroCommand
	protected boolean handleCommand(RequestLockHeroCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO LockHeroCommand method
		return true;//LockHeroCommand
	}//LockHeroCommand
	@Override//MohuaSubmitStageCommand
	protected boolean handleCommand(RequestMohuaSubmitStageCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO MohuaSubmitStageCommand method
		return true;//MohuaSubmitStageCommand
	}//MohuaSubmitStageCommand
	@Override//UnlockAreaCommand
	protected boolean handleCommand(RequestUnlockAreaCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO UnlockAreaCommand method
		return true;//UnlockAreaCommand
	}//UnlockAreaCommand
	@Override//UnlockPvpMapCommand
	protected boolean handleCommand(RequestUnlockPVPMapCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO UnlockPvpMapCommand method
		return true;//UnlockPvpMapCommand
	}//UnlockPvpMapCommand
	@Override//CollectResourceMineCommand
	protected boolean handleCommand(RequestCollectResourceMineCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO CollectResourceMineCommand method
		return true;//CollectResourceMineCommand
	}//CollectResourceMineCommand
	@Override//AreaResourceCommand
	protected boolean handleCommand(RequestAreaResourceCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO AreaResourceCommand method
		return true;//AreaResourceCommand
	}//AreaResourceCommand
	@Override//UnlockLevelCommand
	protected boolean handleCommand(RequestUnlockLevelCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO UnlockLevelCommand method
		return true;//UnlockLevelCommand
	}//UnlockLevelCommand
	@Override//UseAreaEquipCommand
	protected boolean handleCommand(RequestUseAreaEquipCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO UseAreaEquipCommand method
		return true;//UseAreaEquipCommand
	}//UseAreaEquipCommand
	@Override//BuyHeroPackageCommand
	protected boolean handleCommand(RequestBuyHeroPackageCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO BuyHeroPackageCommand method
		return true;//BuyHeroPackageCommand
	}//BuyHeroPackageCommand
	@Override//SubmitComposeSkillCommand
	protected boolean handleCommand(RequestSubmitComposeSkillCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO SubmitComposeSkillCommand method
		return true;//SubmitComposeSkillCommand
	}//SubmitComposeSkillCommand
	@Override//BuyLootPackageCommand
	protected boolean handleCommand(RequestBuyLootPackageCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO BuyLootPackageCommand method
		return true;//BuyLootPackageCommand
	}//BuyLootPackageCommand
	@Override//CdkeyCommand
	protected boolean handleCommand(RequestCdkeyCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO CdkeyCommand method
		return true;//CdkeyCommand
	}//CdkeyCommand
	@Override//SubmitIconCommand
	protected boolean handleCommand(RequestSubmitIconCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO SubmitIconCommand method
		return true;//SubmitIconCommand
	}//SubmitIconCommand
	@Override//RechargeCommand
	protected boolean handleCommand(RequestCheatRechargeCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO RechargeCommand method
		return true;//RechargeCommand
	}//RechargeCommand
	@Override//RefreshPvpMapCommand
	protected boolean handleCommand(RequestRefreshPVPMapCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO RefreshPvpMapCommand method
		return true;//RefreshPvpMapCommand
	}//RefreshPvpMapCommand
	@Override//ReadyAttackLadderCommand
	protected boolean handleCommand(RequestReadyAttackLadderCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO ReadyAttackLadderCommand method
		return true;//ReadyAttackLadderCommand
	}//ReadyAttackLadderCommand
	@Override//BindAccountCommand
	protected boolean handleCommand(RequestBindAccountCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO BindAccountCommand method
		return true;//BindAccountCommand
	}//BindAccountCommand
	@Override//PurchaseVipLibaoCommand
	protected boolean handleCommand(RequestPurchaseVipLibaoCommand cmd, Builder responseBuilder, UserBean user) {
		// TODO PurchaseVipLibaoCommand method
		return true;//PurchaseVipLibaoCommand
	}//PurchaseVipLibaoCommand
	//add handleCommand here

	@Override
	protected boolean handleCommand(RequestLevelPrepareCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestLevelPauseCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestApplyUnionCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestCreateUnionCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestReplyUnionCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestMessageBoardListCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestCreateMessageBoardCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestReplyMessageCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestAddTeamCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestUserTeamListCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestGetLadderUserInfoCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestGetUserFriendListCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestEquipComposeCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestUsePropCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestFenjieEquipCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestFenjieHeroEquipCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestSignCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestHelpAttackPVPMineCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestFenjieHeroCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestResetHeroSkillCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestSendMailCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestBrotherMineInfoCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestEnterMohuaMapCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestUseMohuaCardCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestMohuaStageRewardCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestMohuaHpRewardCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestStartMohuaMapCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestEndMohuaMapCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestSaleEquipCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestDelFriendCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestSubmitZhanliCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestAchieveRewardCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestAchieveListCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestRichangRewardCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestRichangListCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestKaifu2ActivityCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestKaifu2RewardCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestKaifuRewardCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestKaifuListCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestUserPokedeCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean handleCommand(RequestRankCommand cmd,
			Builder responseBuilder, UserBean user) {
		// TODO Auto-generated method stub
		return true;
	}
}
