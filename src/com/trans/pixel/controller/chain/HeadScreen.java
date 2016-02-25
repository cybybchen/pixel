package com.trans.pixel.controller.chain;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.HeadInfo;
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
import com.trans.pixel.protoc.Commands.RequestAttackResourceMineInfoCommand;
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
import com.trans.pixel.protoc.Commands.RequestExpeditionShopCommand;
import com.trans.pixel.protoc.Commands.RequestExpeditionShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestExpeditionShopRefreshCommand;
import com.trans.pixel.protoc.Commands.RequestGetLadderRankListCommand;
import com.trans.pixel.protoc.Commands.RequestGetLadderUserInfoCommand;
import com.trans.pixel.protoc.Commands.RequestGetUserFriendListCommand;
import com.trans.pixel.protoc.Commands.RequestGetUserLadderRankListCommand;
import com.trans.pixel.protoc.Commands.RequestGetUserMailListCommand;
import com.trans.pixel.protoc.Commands.RequestHandleUnionMemberCommand;
import com.trans.pixel.protoc.Commands.RequestHeroLevelUpCommand;
import com.trans.pixel.protoc.Commands.RequestLadderShopCommand;
import com.trans.pixel.protoc.Commands.RequestLadderShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestLadderShopRefreshCommand;
//add import here
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
import com.trans.pixel.protoc.Commands.RequestPVPShopCommand;
import com.trans.pixel.protoc.Commands.RequestPVPShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestPVPShopRefreshCommand;
import com.trans.pixel.protoc.Commands.RequestPurchaseCoinCommand;
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
import com.trans.pixel.service.UserService;

public class HeadScreen extends RequestScreen {
	
	private static final Logger log = LoggerFactory.getLogger(HeadScreen.class);
	
//	@Resource
//    private AccountService accountService;
	@Resource
    private UserService userService;
	
	@Override
    public boolean handleRequest(PixelRequest req, PixelResponse rep) {
        RequestCommand request = req.command;
        HeadInfo head = request.getHead();
        HeadInfo.Builder headBuilder = buildHeadInfo(head);
        HeadInfo responseHead = headBuilder.build();
        rep.command.setHead(responseHead);
        if (request.hasRegisterCommand() || request.hasLoginCommand()) {
            return true;
        }
        long userId = head.getUserId();
        req.user = userService.getUser(userId);
        
        if (req.user != null) {
           
        } else {
            log.error("cmd user err : " + req);
        }
        
        return true;
    }

    private HeadInfo.Builder buildHeadInfo(HeadInfo head) {
        HeadInfo.Builder headBuilder = HeadInfo.newBuilder();
        headBuilder.setGameVersion(head.getGameVersion());
        headBuilder.setVersion(head.getVersion());
        headBuilder.setAccount(head.getAccount());
        headBuilder.setServerId(head.getServerId());
        headBuilder.setUserId(head.getUserId());
        headBuilder.setDatetime(System.currentTimeMillis() / 1000);
        return headBuilder;
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
	protected boolean handleCommand(RequestAttackRelativeCommand cmd,
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
	protected boolean handleCommand(RequestRefreshRelatedUserCommand cmd,
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
	protected boolean handleCommand(RequestDeleteUnionCommand cmd, Builder responseBuilder, UserBean user) {
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
}
