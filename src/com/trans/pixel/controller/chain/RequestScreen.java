package com.trans.pixel.controller.chain;

import com.trans.pixel.model.userinfo.UserBean;
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
import com.trans.pixel.protoc.Commands.RequestCheatRechargeCommand;
//add import here
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
import com.trans.pixel.protoc.Commands.RequestSubmitIconCommand;
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
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;


public abstract class RequestScreen implements RequestHandle {
	private RequestHandle nullUserErrorHandle = new NullUserErrorHandle();
	
	protected abstract boolean handleRegisterCommand(RequestCommand cmd, Builder responseBuilder);
	
	protected abstract boolean handleLoginCommand(RequestCommand cmd, Builder responseBuilder);
	
	protected abstract boolean handleCommand(RequestLevelStartCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestLevelResultCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestLevelLootStartCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestLevelLootResultCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestLootResultCommand cmd, Builder responseBuilder, UserBean user);
	
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
	
	protected abstract boolean handleCommand(RequestEquipLevelUpCommand cmd, Builder responseBuilder, UserBean user);

	protected abstract boolean handleCommand(RequestAreaCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestAttackBossCommand cmd, Builder responseBuilder, UserBean user);

	protected abstract boolean handleCommand(RequestAttackMonsterCommand cmd, Builder responseBuilder, UserBean user);

	protected abstract boolean handleCommand(RequestAttackResourceCommand cmd, Builder responseBuilder, UserBean user);

	protected abstract boolean handleCommand(RequestAttackResourceMineCommand cmd, Builder responseBuilder, UserBean user);
    
	protected abstract boolean handleCommand(RequestRegisterCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestLoginCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestGetUserLadderRankListCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestLevelPrepareCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestLevelPauseCommand cmd, Builder responseBuilder, UserBean user);
	
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
	
	protected abstract boolean handleCommand(RequestAddTeamCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestUserTeamListCommand cmd, Builder responseBuilder, UserBean user);

	protected abstract boolean handleCommand(RequestAttackUnionCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestGetLadderUserInfoCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestGetUserFriendListCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestEquipComposeCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestUsePropCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestFenjieEquipCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestFenjieHeroEquipCommand cmd, Builder responseBuilder, UserBean user);
	
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
	
	protected abstract boolean handleCommand(RequestKaifu2RewardCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestKaifuRewardCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestKaifuListCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestUserPokedeCommand cmd, Builder responseBuilder, UserBean user);
	
	protected abstract boolean handleCommand(RequestRankCommand cmd, Builder responseBuilder, UserBean user);
	
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
	protected abstract boolean handleCommand(RequestUnlockLevelCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestUseAreaEquipCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestBuyHeroPackageCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestSubmitComposeSkillCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestBuyLootPackageCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestCdkeyCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestSubmitIconCommand cmd, Builder responseBuilder, UserBean user);
	protected abstract boolean handleCommand(RequestCheatRechargeCommand cmd, Builder responseBuilder, UserBean user);
	//add handleCommand here
	
	@Override
    public boolean handleRequest(PixelRequest req, PixelResponse rep) {
        boolean result = true;
        RequestCommand request = req.command;
        ResponseCommand.Builder responseBuilder = rep.command;
        UserBean user = req.user;
        
        if (request.hasRegisterCommand()) {
        	handleRegisterCommand(request, responseBuilder);
        } else if (request.hasLoginCommand()) {
        	handleLoginCommand(request, responseBuilder);
        } else {
        	if (user == null) {
                nullUserErrorHandle.handleRequest(req, rep);
                return false;
            }
        }
        
        if (request.hasLevelResultCommand()) {
        	RequestLevelResultCommand cmd = request.getLevelResultCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
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
        if (request.hasLevelLootStartCommand()) {
        	RequestLevelLootStartCommand cmd = request.getLevelLootStartCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasLevelStartCommand()) {
        	RequestLevelStartCommand cmd = request.getLevelStartCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasLootResultCommand()) {
        	RequestLootResultCommand cmd = request.getLootResultCommand();
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
        if (request.hasAttackLadderModeCommand()) {
        	RequestAttackLadderModeCommand cmd = request.getAttackLadderModeCommand();
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
        if (request.hasEquipLevelUpCommand()) {
        	RequestEquipLevelUpCommand cmd = request.getEquipLevelUpCommand();
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
        if (request.hasLevelPrepareCommand()) {
            RequestLevelPrepareCommand cmd = request.getLevelPrepareCommand();
            if (result)
                result = handleCommand(cmd, responseBuilder, user);
        }
        if (request.hasLevelPauseCommand()) {
            RequestLevelPauseCommand cmd = request.getLevelPauseCommand();
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
        if (request.hasAddTeamCommand()) {
            RequestAddTeamCommand cmd = request.getAddTeamCommand();
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
        if (request.hasFenjieHeroEquipCommand()) {
        	RequestFenjieHeroEquipCommand cmd = request.getFenjieHeroEquipCommand();
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
        if (request.hasKaifu2RewardCommand()) {
        	RequestKaifu2RewardCommand cmd = request.getKaifu2RewardCommand();
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
        if (request.hasUnlockLevelCommand()) {
            RequestUnlockLevelCommand cmd = request.getUnlockLevelCommand();
            if (result)//UnlockLevelCommand
                result = handleCommand(cmd, responseBuilder, user);//UnlockLevelCommand
        }//UnlockLevelCommand
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
        if (request.hasRechargeCommand()) {
            RequestCheatRechargeCommand cmd = request.getRechargeCommand();
            if (result)//RechargeCommand
                result = handleCommand(cmd, responseBuilder, user);//RechargeCommand
        }//RechargeCommand
        //call handleCommand here
        
        return result;
	}
}
