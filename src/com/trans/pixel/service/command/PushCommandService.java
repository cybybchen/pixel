package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.MessageBoardBean;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.model.userinfo.UserHeroBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.model.userinfo.UserLevelLootBean;
import com.trans.pixel.model.userinfo.UserPropBean;
import com.trans.pixel.model.userinfo.UserTeamBean;
import com.trans.pixel.protoc.Commands.MailList;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RequestBlackShopCommand;
import com.trans.pixel.protoc.Commands.RequestDailyShopCommand;
import com.trans.pixel.protoc.Commands.RequestExpeditionShopCommand;
import com.trans.pixel.protoc.Commands.RequestLadderShopCommand;
import com.trans.pixel.protoc.Commands.RequestPVPShopCommand;
import com.trans.pixel.protoc.Commands.RequestShopCommand;
import com.trans.pixel.protoc.Commands.RequestUnionShopCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseGetUserEquipCommand;
import com.trans.pixel.protoc.Commands.ResponseGetUserFriendListCommand;
import com.trans.pixel.protoc.Commands.ResponseGetUserHeroCommand;
import com.trans.pixel.protoc.Commands.ResponseGetUserMailListCommand;
import com.trans.pixel.protoc.Commands.ResponseMessageBoardListCommand;
import com.trans.pixel.protoc.Commands.ResponseUserInfoCommand;
import com.trans.pixel.protoc.Commands.ResponseUserLevelCommand;
import com.trans.pixel.protoc.Commands.ResponseUserLootLevelCommand;
import com.trans.pixel.protoc.Commands.ResponseUserPropCommand;
import com.trans.pixel.protoc.Commands.ResponseUserTeamListCommand;
import com.trans.pixel.protoc.Commands.RewardCommand;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.protoc.Commands.UserFriend;
import com.trans.pixel.service.LadderService;
import com.trans.pixel.service.LootService;
import com.trans.pixel.service.MailService;
import com.trans.pixel.service.MessageService;
import com.trans.pixel.service.PvpMapService;
import com.trans.pixel.service.UserEquipService;
import com.trans.pixel.service.UserFriendService;
import com.trans.pixel.service.UserHeroService;
import com.trans.pixel.service.UserLevelLootService;
import com.trans.pixel.service.UserLevelService;
import com.trans.pixel.service.UserPropService;
import com.trans.pixel.service.UserTeamService;

@Service
public class PushCommandService extends BaseCommandService {

	@Resource
	private LootService lootService;
	@Resource
	private PvpMapService pvpMapService;
	@Resource
	private UserHeroService userHeroService;
	@Resource
	private UserEquipService userEquipService;
	@Resource
	private LadderService ladderService;
	@Resource
	private MailService mailService;
	@Resource
	private UserFriendService userFriendService;
	@Resource
	private UserLevelService userLevelService;
	@Resource
	private UserLevelLootService userLevelLootService;
	@Resource
	private MessageService messageService;
	@Resource
	private UserTeamService userTeamService;
	@Resource
	private ShopCommandService shopCommandService;
	@Resource
	private UserPropService userPropService;
	
	public void pushLootResultCommand(Builder responseBuilder, UserBean user) {
		user = lootService.updateLootResult(user);
		responseBuilder.setLootResultCommand(super.builderLootResultCommand(user));
	}
	
//	public void pushUserMineListCommand(Builder responseBuilder, UserBean user) {
//		List<UserMineBean> userMineList = pvpMapService.relateUser(user);
//		responseBuilder.setGetUserMineCommand(super.buildGetUserMineCommand(userMineList));
//	}
	
	public void pushUserHeroListCommand(Builder responseBuilder, UserBean user) {
		List<UserHeroBean> userHeroList = userHeroService.selectUserHeroList(user.getId());
		ResponseGetUserHeroCommand.Builder builder = ResponseGetUserHeroCommand.newBuilder();
		builder.addAllUserHero(super.buildUserHeroList(userHeroList));
		responseBuilder.setGetUserHeroCommand(builder.build());
	}
	
	public void pushUserEquipListCommand(Builder responseBuilder, UserBean user) {
		List<UserEquipBean> userEquipList = userEquipService.selectUserEquipList(user.getId());
		ResponseGetUserEquipCommand.Builder builder = ResponseGetUserEquipCommand.newBuilder();
		builder.addAllUserEquip(super.buildUserEquipList(userEquipList));
		responseBuilder.setUserEquipCommand(builder.build());
	}
	
	public void pushUserPropListCommand(Builder responseBuilder, UserBean user) {
		List<UserPropBean> userPropList = userPropService.selectUserPropList(user.getId());
		ResponseUserPropCommand.Builder builder = ResponseUserPropCommand.newBuilder();
		builder.addAllUserProp(super.buildUserPropList(userPropList));
		responseBuilder.setUserPropCommand(builder.build());
	}
	
	public void pushGetUserLadderRankListCommand(Builder responseBuilder, UserBean user) {
		super.handleGetUserLadderRankListCommand(responseBuilder, user);
	}
	
	public void pushUserInfoCommand(Builder responseBuilder, UserBean user) {
		ResponseUserInfoCommand.Builder builder = ResponseUserInfoCommand.newBuilder();
		super.buildUserInfo(builder, user);
		responseBuilder.setUserInfoCommand(builder.build());
	}
	
	public void pushUserMailListCommand(Builder responseBuilder, UserBean user) {
		ResponseGetUserMailListCommand.Builder builder = ResponseGetUserMailListCommand.newBuilder();
		List<MailList> mailBuilderList = new ArrayList<MailList>();
		for (Integer type : MailConst.MAIL_TYPES) {
			List<MailBean> mailList = mailService.getMailList(user.getId(), type);
			mailBuilderList.add(super.buildMailList(type, mailList));
		}
		builder.addAllMailList(mailBuilderList);
		responseBuilder.setGetUserMailListCommand(builder.build());
	}
	
	public void pushUserFriendListCommand(Builder responseBuilder, UserBean user) {
		ResponseGetUserFriendListCommand.Builder builder = ResponseGetUserFriendListCommand.newBuilder();
		List<UserFriend> userFriendList = userFriendService.getUserFriendList(user);
		builder.addAllFriend(userFriendList);
		responseBuilder.setGetUserFriendListCommand(builder.build());
	}
	
	public void pushUserLevelCommand(Builder responseBuilder, UserBean user) {
		ResponseUserLevelCommand.Builder builder = ResponseUserLevelCommand.newBuilder();
		UserLevelBean userLevel = userLevelService.selectUserLevelRecord(user.getId());
		if (userLevel.getLastLevelResultTime() > 0) {
			userLevel.setLevelPrepareTime(userLevel.getLevelPrepareTime() + 
					(int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND) - userLevel.getLastLevelResultTime());
			userLevel.setLastLevelResultTime((int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND));
			userLevelService.updateUserLevelRecord(userLevel);
		}
		builder.setUserLevel(userLevel.buildUserLevel());
		responseBuilder.setUserLevelCommand(builder.build());
	}
	
	public void pushUserLootLevelCommand(Builder responseBuilder, UserBean user) {
		ResponseUserLootLevelCommand.Builder builder = ResponseUserLootLevelCommand.newBuilder();
		UserLevelLootBean userLevelLoot = userLevelLootService.selectUserLevelLootRecord(user.getId());
		builder.setUserLootLevel(userLevelLoot.buildUserLootLevel());
		responseBuilder.setUserLootLevelCommand(builder.build());
	}
	
	public void pushLadderRankListCommand(Builder responseBuilder, UserBean user) {	
		super.handleGetUserLadderRankListCommand(responseBuilder, user);
	}
	
	public void pushMessageBoardListCommand(int type, Builder responseBuilder, UserBean user) {
		List<MessageBoardBean> messageBoardList = messageService.getMessageBoardList(type, user);
		ResponseMessageBoardListCommand.Builder builder = ResponseMessageBoardListCommand.newBuilder();
		builder.addAllMessageBoard(super.buildMessageBoardList(messageBoardList));
		builder.setType(type);
		responseBuilder.setMessageBoardListCommand(builder.build());
	}
	
	public void pushUserTeamListCommand(Builder responseBuilder, UserBean user) {
		List<UserTeamBean> userTeamList = userTeamService.selectUserTeamList(user.getId());
		ResponseUserTeamListCommand.Builder builder = ResponseUserTeamListCommand.newBuilder();
		builder.addAllUserTeam(buildUserTeamList(userTeamList));
		
		responseBuilder.setUserTeamListCommand(builder.build());
	}
	
	public void pushShopCommand(Builder responseBuilder, UserBean user) {
		RequestShopCommand.Builder cmd = RequestShopCommand.newBuilder();
		shopCommandService.Shop(cmd.build(), responseBuilder, user);
	}
	
	public void pushDailyShopCommand(Builder responseBuilder, UserBean user) {
		RequestDailyShopCommand.Builder cmd = RequestDailyShopCommand.newBuilder();
		shopCommandService.DailyShop(cmd.build(), responseBuilder, user);
	}
	
	public void pushBlackShopCommand(Builder responseBuilder, UserBean user) {
		RequestBlackShopCommand.Builder cmd = RequestBlackShopCommand.newBuilder();
		shopCommandService.BlackShop(cmd.build(), responseBuilder, user);
	}
	
	public void pushUnionShopCommand(Builder responseBuilder, UserBean user) {
		RequestUnionShopCommand.Builder cmd = RequestUnionShopCommand.newBuilder();
		shopCommandService.UnionShop(cmd.build(), responseBuilder, user);
	}
	
	public void pushPVPShopCommand(Builder responseBuilder, UserBean user) {
		RequestPVPShopCommand.Builder cmd = RequestPVPShopCommand.newBuilder();
		shopCommandService.PVPShop(cmd.build(), responseBuilder, user);
	}
	
	public void pushExpeditionShopCommand(Builder responseBuilder, UserBean user) {
		RequestExpeditionShopCommand.Builder cmd = RequestExpeditionShopCommand.newBuilder();
		shopCommandService.ExpeditionShop(cmd.build(), responseBuilder, user);
	}
	
	public void pushLadderShopCommand(Builder responseBuilder, UserBean user) {
		RequestLadderShopCommand.Builder cmd = RequestLadderShopCommand.newBuilder();
		shopCommandService.LadderShop(cmd.build(), responseBuilder, user);
	}

	
	/**
	 * Include RewardCommand
	 */
	public void pushRewardCommand(Builder responseBuilder, UserBean user, int rewardId, String rewardName, int rewardCount) {
		RewardCommand.Builder reward = RewardCommand.newBuilder();
		RewardInfo.Builder builder = RewardInfo.newBuilder();
		builder.setItemid(rewardId);
		builder.setItemname(rewardName);
		builder.setCount(rewardCount);
		reward.addLoot(builder);
		reward.setTitle("恭喜获得");
		responseBuilder.setRewardCommand(reward);
		pushUserDataByRewardId(responseBuilder, user, rewardId);
	}
	
	/**
	 * No RewardCommand include
	 */
	public void pushRewardCommand(Builder responseBuilder, UserBean user, int rewardId) {
		pushUserDataByRewardId(responseBuilder, user, rewardId);
	}
	
	public void pushUserDataByRewardId(Builder responseBuilder, UserBean user, int rewardId) {
		if (rewardId > RewardConst.HERO) {
			this.pushUserHeroListCommand(responseBuilder, user);
		} else if (rewardId > RewardConst.PACKAGE) {
			this.pushUserPropListCommand(responseBuilder, user);
		} else if (rewardId > RewardConst.CHIP) {
			this.pushUserEquipListCommand(responseBuilder, user);
		} else if (rewardId > RewardConst.EQUIPMENT) {
			this.pushUserEquipListCommand(responseBuilder, user);
		} else {
			this.pushUserInfoCommand(responseBuilder, user);
		}
	}

	public void pushRewardCommand(Builder responseBuilder, UserBean user, MultiReward rewards) {
		boolean isHeroUpdated = false;
		boolean isPropUpdated = false;
		boolean isPackageUpdated = false;
		boolean isEquipUpdated = false;
		boolean isUserUpdated = false;
		for(RewardInfo reward : rewards.getLootList()){
			int rewardId = reward.getItemid();
			if (rewardId > RewardConst.HERO) {
				isHeroUpdated = true;
			} else if (rewardId > RewardConst.PROP) {
				isPropUpdated = true;
			} else if (rewardId > RewardConst.PACKAGE) {
				isPackageUpdated = true;
			} else if (rewardId > RewardConst.EQUIPMENT || rewardId > RewardConst.CHIP) {
				isEquipUpdated = true;
			} else {
				isUserUpdated = true;
			}
		}
		RewardCommand.Builder reward = RewardCommand.newBuilder();
		reward.setTitle(rewards.getName());
		reward.addAllLoot(rewards.getLootList());
		responseBuilder.setRewardCommand(reward);
		if(isHeroUpdated)
			this.pushUserDataByRewardId(responseBuilder, user, RewardConst.HERO+1);
		if(isPropUpdated)
			this.pushUserDataByRewardId(responseBuilder, user, RewardConst.PROP+1);
		if(isPackageUpdated)
			this.pushUserDataByRewardId(responseBuilder, user, RewardConst.PACKAGE+1);
		if(isEquipUpdated)
			this.pushUserDataByRewardId(responseBuilder, user, RewardConst.EQUIPMENT+1);
		if(isUserUpdated)
			this.pushUserDataByRewardId(responseBuilder, user, 1001);
	}
	
	public void pushRewardCommand(Builder responseBuilder, UserBean user, List<RewardBean> rewards) {
		boolean isHeroUpdated = false;
		boolean isPropUpdated = false;
		boolean isPackageUpdated = false;
		boolean isEquipUpdated = false;
		boolean isUserUpdated = false;
		RewardCommand.Builder rewardbuilder = RewardCommand.newBuilder();
		for(RewardBean reward : rewards){
			rewardbuilder.addLoot(reward.buildRewardInfo());
			int rewardId = reward.getItemid();
			if (rewardId > RewardConst.HERO) {
				isHeroUpdated = true;
			} else if (rewardId > RewardConst.PROP) {
				isPropUpdated = true;
			} else if (rewardId > RewardConst.PACKAGE) {
				isPackageUpdated = true;
			} else if (rewardId > RewardConst.EQUIPMENT || rewardId > RewardConst.CHIP) {
				isEquipUpdated = true;
			} else {
				isUserUpdated = true;
			}
		}
		rewardbuilder.setTitle("恭喜获得");
		responseBuilder.setRewardCommand(rewardbuilder);
		if(isHeroUpdated)
			this.pushUserDataByRewardId(responseBuilder, user, RewardConst.HERO+1);
		if(isPropUpdated)
			this.pushUserDataByRewardId(responseBuilder, user, RewardConst.PROP+1);
		if(isPackageUpdated)
			this.pushUserDataByRewardId(responseBuilder, user, RewardConst.PACKAGE+1);
		if(isEquipUpdated)
			this.pushUserDataByRewardId(responseBuilder, user, RewardConst.EQUIPMENT+1);
		if(isUserUpdated)
			this.pushUserDataByRewardId(responseBuilder, user, 1001);
	}

	public void pushPurchaseCoinCommand(Builder responseBuilder, UserBean user) {
		shopCommandService.getPurchaseCoinTime(responseBuilder, user);
	}
}
