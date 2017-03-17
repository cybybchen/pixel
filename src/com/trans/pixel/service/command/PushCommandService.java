package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.MessageBoardBean;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.userinfo.UserAchieveBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.model.userinfo.UserEquipPokedeBean;
import com.trans.pixel.model.userinfo.UserFoodBean;
import com.trans.pixel.model.userinfo.UserHeadBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.model.userinfo.UserPokedeBean;
import com.trans.pixel.model.userinfo.UserPropBean;
import com.trans.pixel.model.userinfo.UserTeamBean;
import com.trans.pixel.protoc.Commands.BossGroupRecord;
import com.trans.pixel.protoc.Commands.MailList;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.PVPMapList;
import com.trans.pixel.protoc.Commands.RequestDailyShopCommand;
import com.trans.pixel.protoc.Commands.RequestExpeditionShopCommand;
import com.trans.pixel.protoc.Commands.RequestLadderShopCommand;
import com.trans.pixel.protoc.Commands.RequestPVPShopCommand;
import com.trans.pixel.protoc.Commands.RequestShopCommand;
import com.trans.pixel.protoc.Commands.RequestUnionShopCommand;
import com.trans.pixel.protoc.Commands.ResponseAchieveListCommand;
import com.trans.pixel.protoc.Commands.ResponseBosskillCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseEquipPokedeCommand;
import com.trans.pixel.protoc.Commands.ResponseFightInfoCommand;
import com.trans.pixel.protoc.Commands.ResponseGetUserEquipCommand;
import com.trans.pixel.protoc.Commands.ResponseGetUserFriendListCommand;
import com.trans.pixel.protoc.Commands.ResponseGetUserHeroCommand;
import com.trans.pixel.protoc.Commands.ResponseGetUserMailListCommand;
import com.trans.pixel.protoc.Commands.ResponseLevelLootCommand;
import com.trans.pixel.protoc.Commands.ResponseMessageBoardListCommand;
import com.trans.pixel.protoc.Commands.ResponsePVPMapListCommand;
import com.trans.pixel.protoc.Commands.ResponseUserFoodCommand;
import com.trans.pixel.protoc.Commands.ResponseUserHeadCommand;
import com.trans.pixel.protoc.Commands.ResponseUserInfoCommand;
import com.trans.pixel.protoc.Commands.ResponseUserPokedeCommand;
import com.trans.pixel.protoc.Commands.ResponseUserPropCommand;
import com.trans.pixel.protoc.Commands.ResponseUserTalentCommand;
import com.trans.pixel.protoc.Commands.ResponseUserTeamListCommand;
import com.trans.pixel.protoc.Commands.RewardCommand;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.protoc.Commands.UserFriend;
import com.trans.pixel.service.BossService;
import com.trans.pixel.service.LadderService;
import com.trans.pixel.service.MailService;
import com.trans.pixel.service.MessageService;
import com.trans.pixel.service.PvpMapService;
import com.trans.pixel.service.UserAchieveService;
import com.trans.pixel.service.UserEquipPokedeService;
import com.trans.pixel.service.UserEquipService;
import com.trans.pixel.service.UserFoodService;
import com.trans.pixel.service.UserFriendService;
import com.trans.pixel.service.UserHeadService;
import com.trans.pixel.service.UserHeroService;
import com.trans.pixel.service.UserPokedeService;
import com.trans.pixel.service.UserPropService;
import com.trans.pixel.service.UserTalentService;
import com.trans.pixel.service.UserTeamService;
import com.trans.pixel.service.redis.LevelRedisService;

@Service
public class PushCommandService extends BaseCommandService {

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
	private LevelRedisService userLevelService;
	@Resource
	private MessageService messageService;
	@Resource
	private UserTeamService userTeamService;
	@Resource
	private ShopCommandService shopCommandService;
	@Resource
	private UserPropService userPropService;
	@Resource
	private UserAchieveService userAchieveService;
	@Resource
	private UserHeadService userHeadService;
	@Resource
	private UserPokedeService userPokedeService;
	@Resource
	private UserFoodService userFoodService;
	@Resource
	private BossService bossService;
	@Resource
	private UserTalentService userTalentService;
	@Resource
	private UserEquipPokedeService userEquipPokedeService;
	
//	public void pushLootResultCommand(Builder responseBuilder, UserBean user) {
//		user = lootService.updateLootResult(user);
//		responseBuilder.setLootResultCommand(super.builderLootResultCommand(user));
//	}
	
	public void pushPvpMapListCommand(Builder responseBuilder, UserBean user) {
		PVPMapList maplist = pvpMapService.getMapList(responseBuilder, user);
		ResponsePVPMapListCommand.Builder builder = ResponsePVPMapListCommand.newBuilder();
		builder.addAllField(maplist.getFieldList());
		builder.setBuff(maplist.getBuff());
		builder.setEndTime(user.getRefreshPvpMapTime());
		responseBuilder.setPvpMapListCommand(builder);
	}
	
	public void pushUserHeroListCommand(Builder responseBuilder, UserBean user) {
		List<HeroInfoBean> userHeroList = userHeroService.selectUserHeroList(user);
		ResponseGetUserHeroCommand.Builder builder = ResponseGetUserHeroCommand.newBuilder();
		builder.addAllUserHero(super.buildUserHeroList(userHeroList));
		responseBuilder.setGetUserHeroCommand(builder.build());
	}
	
	public void pushUserHeroListCommand(Builder responseBuilder, UserBean user, HeroInfoBean userHero) {
		List<HeroInfoBean> userHeroList = new ArrayList<HeroInfoBean>();
		userHeroList.add(userHero);
		pushUserHeroListCommand(responseBuilder, user, userHeroList);
	}
	
	public void pushUserHeroListCommand(Builder responseBuilder, UserBean user, List<HeroInfoBean> userHeroList) {
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
	
	public void pushUserEquipListCommand(Builder responseBuilder, UserBean user, List<UserEquipBean> userEquipList) {
		ResponseGetUserEquipCommand.Builder builder = ResponseGetUserEquipCommand.newBuilder();
		builder.addAllUserEquip(buildUserEquipList(userEquipList));
		responseBuilder.setUserEquipCommand(builder.build());
	}
	
	public void pushUserPropListCommand(Builder responseBuilder, UserBean user) {
		List<UserPropBean> userPropList = userPropService.selectUserPropList(user.getId());
		ResponseUserPropCommand.Builder builder = ResponseUserPropCommand.newBuilder();
		builder.addAllUserProp(super.buildUserPropList(userPropList));
		responseBuilder.setUserPropCommand(builder.build());
	}
	
	public void pushUserPropListCommand(Builder responseBuilder, UserBean user, List<UserPropBean> userPropList) {
		ResponseUserPropCommand.Builder builder = null;
		if(responseBuilder.hasUserPropCommand())
			builder = responseBuilder.getUserPropCommandBuilder();
		else
			builder = ResponseUserPropCommand.newBuilder();
		builder.addAllUserProp(super.buildUserPropList(userPropList));
		responseBuilder.setUserPropCommand(builder.build());
	}
	
	public void pushUserFoodListCommand(Builder responseBuilder, UserBean user) {
		List<UserFoodBean> userFoodList = userFoodService.selectUserFoodList(user.getId());
		ResponseUserFoodCommand.Builder builder = ResponseUserFoodCommand.newBuilder();
		builder.addAllUserFood(buildUserFoodList(userFoodList));
		responseBuilder.setUserFoodCommand(builder.build());
	}
	
	public void pushUserFoodListCommand(Builder responseBuilder, UserBean user, List<UserFoodBean> userFoodList) {
		ResponseUserFoodCommand.Builder builder = ResponseUserFoodCommand.newBuilder();
		builder.addAllUserFood(buildUserFoodList(userFoodList));
		responseBuilder.setUserFoodCommand(builder.build());
	}
	
	public void pushGetUserLadderRankListCommand(Builder responseBuilder, UserBean user) {
		super.handleGetUserLadderRankListCommand(responseBuilder, user);
	}
	
	public void pushUserInfoCommand(Builder responseBuilder, UserBean user) {
//		lootService.updateLootResult(user);
		ResponseUserInfoCommand.Builder builder = ResponseUserInfoCommand.newBuilder();
		buildUserInfo(builder, user);
		responseBuilder.setUserInfoCommand(builder.build());
	}
	
	public void pushUserMailListCommand(Builder responseBuilder, UserBean user, int type) {
		ResponseGetUserMailListCommand.Builder builder = ResponseGetUserMailListCommand.newBuilder();
		List<MailList> mailBuilderList = new ArrayList<MailList>();
		if (type == MailConst.TYPE_SYSTEM_MAIL || type == MailConst.TYPE_MINE_ATTACKED_MAIL) {
			for (Integer systemType : MailConst.SYSTEM_MAIL_TYPES) {
				List<MailBean> mailList = mailService.getMailList(user.getId(), systemType);
				mailBuilderList.add(buildMailList(systemType, mailList));
			}
			
			mailService.isDeleteNotice(user.getId(), MailConst.TYPE_SYSTEM_MAIL);
		} else {
			for (Integer friendMailType : MailConst.FRIEND_MAIL_TYPES) {
				List<MailBean> mailList = mailService.getMailList(user.getId(), friendMailType);
				mailBuilderList.add(buildMailList(friendMailType, mailList));
			}
			
			mailService.isDeleteNotice(user.getId(), MailConst.TYPE_ADDFRIEND_MAIL);
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
	
	public void pushLevelLootCommand(Builder responseBuilder, UserBean user) {
		// UserLevelBean userLevel = userLevelService.getUserLevel(user);
//		if (userLevel.getLastLevelResultTime() > 0) {
//			userLevel.setLevelPrepareTime(userLevel.getLevelPrepareTime() + 
//					(int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND) - userLevel.getLastLevelResultTime());
//			userLevel.setLastLevelResultTime((int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND));
//			userLevelService.updateUserLevelRecord(userLevel);
//		}
		// responseBuilder.setLevelLootCommand(userLevel.build());
	}
	
//	public void pushUserLootLevelCommand(Builder responseBuilder, UserBean user) {
//		ResponseUserLootLevelCommand.Builder builder = ResponseUserLootLevelCommand.newBuilder();
//		UserLevelLootBean userLevelLoot = userLevelLootService.selectUserLevelLootRecord(user.getId());
//		builder.setUserLootLevel(userLevelLoot.buildUserLootLevel());
//		responseBuilder.setUserLootLevelCommand(builder.build());
//	}
	
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
		List<UserTeamBean> userTeamList = userTeamService.selectUserTeamList(user);
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
		shopCommandService.BlackShop(responseBuilder, user);
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

	public void pushUserHeadCommand(Builder responseBuilder, UserBean user) {
		ResponseUserHeadCommand.Builder cmd = ResponseUserHeadCommand.newBuilder();
		List<UserHeadBean> userHeadList = userHeadService.selectUserHeadList(user.getId());
		cmd.addAllUserHead(this.buildUserHeadList(userHeadList));
		responseBuilder.setUserHeadCommand(cmd.build());
	}
	
	public void pushUserHeadCommand(Builder responseBuilder, UserBean user, List<UserHeadBean> userHeadList) {
		ResponseUserHeadCommand.Builder cmd = ResponseUserHeadCommand.newBuilder();
		cmd.addAllUserHead(this.buildUserHeadList(userHeadList));
		responseBuilder.setUserHeadCommand(cmd.build());
	}
	
	
	/**
	 * Include RewardCommand
	 */
	public void pushRewardCommand(Builder responseBuilder, UserBean user, int rewardId, String rewardName, long rewardCount) {
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
		List<HeroInfoBean> heroList = new ArrayList<HeroInfoBean>();
		List<UserEquipBean> equipList = new ArrayList<UserEquipBean>();
		List<UserPropBean> propList = new ArrayList<UserPropBean>(); 
		List<UserHeadBean> headList = new ArrayList<UserHeadBean>();
		List<UserFoodBean> foodList = new ArrayList<UserFoodBean>();
		long userId = user.getId();
		if (rewardId > RewardConst.FOOD) {
			foodList.add(userFoodService.selectUserFood(user, rewardId));
			this.pushUserFoodListCommand(responseBuilder, user, foodList);
		} else if (rewardId > RewardConst.HEAD) {
			headList.add(userHeadService.selectUserHead(userId, rewardId));
			this.pushUserHeadCommand(responseBuilder, user, headList);
		} else if (rewardId > RewardConst.HERO) {
			heroList.addAll(userHeroService.selectUserNewHero(userId));
			this.pushUserHeroListCommand(responseBuilder, user, heroList);
		} else if (rewardId > RewardConst.PACKAGE) {
			propList.add(userPropService.selectUserProp(userId, rewardId));
			this.pushUserPropListCommand(responseBuilder, user, propList);
		} else if (rewardId > RewardConst.EQUIPMENT) {
			equipList.add(userEquipService.selectUserEquip(userId, rewardId));
			this.pushUserEquipListCommand(responseBuilder, user, equipList);
		} else {
			this.pushUserInfoCommand(responseBuilder, user);
		}
	}

	public void pushRewardCommand(Builder responseBuilder, UserBean user, MultiReward rewards) {
		pushRewardCommand(responseBuilder, user, rewards, true);
	}
	
	public void pushRewardCommand(Builder responseBuilder, UserBean user, MultiReward rewards, boolean isreward) {
		List<HeroInfoBean> heroList = new ArrayList<HeroInfoBean>();
		List<UserEquipBean> equipList = new ArrayList<UserEquipBean>();
		List<UserPropBean> propList = new ArrayList<UserPropBean>(); 
		List<UserHeadBean> headList = new ArrayList<UserHeadBean>();
		List<Integer> pushRewardIdList = new ArrayList<Integer>();
		List<UserFoodBean> foodList = new ArrayList<UserFoodBean>();
		boolean isUserUpdated = false;
		long userId = user.getId();
		for(RewardInfo reward : rewards.getLootList()){
			int rewardId = reward.getItemid();
			if (pushRewardIdList.contains(rewardId))
				continue;
			
			if (rewardId > RewardConst.FOOD) {
				foodList.add(userFoodService.selectUserFood(user, rewardId));
			} else if (rewardId > RewardConst.HEAD) {
				headList.add(userHeadService.selectUserHead(userId, rewardId));
			} if (rewardId > RewardConst.HERO) {
				heroList.addAll(userHeroService.selectUserNewHero(userId));
			} else if (rewardId > RewardConst.PACKAGE) {
				if(rewardId / 1000 == 44)
					isUserUpdated = true;
				else
					propList.add(userPropService.selectUserProp(userId, rewardId));
			} else if (rewardId > RewardConst.EQUIPMENT) {
				equipList.add(userEquipService.selectUserEquip(userId, rewardId));
			} else {
				isUserUpdated = true;
			}
			
			pushRewardIdList.add(rewardId);
		}
		if (isreward) {
			RewardCommand.Builder reward = RewardCommand.newBuilder();
			reward.setTitle(rewards.getName());
			reward.addAllLoot(rewards.getLootList());
			responseBuilder.setRewardCommand(reward);
		}
		if (headList.size() > 0)
			this.pushUserHeadCommand(responseBuilder, user, headList);
		if(heroList.size() > 0)
			this.pushUserHeroListCommand(responseBuilder, user, heroList);
		if(propList.size() > 0)
			this.pushUserPropListCommand(responseBuilder, user, propList);
		if(equipList.size() > 0)
			this.pushUserEquipListCommand(responseBuilder, user, equipList);
		if (foodList.size() > 0)
			this.pushUserFoodListCommand(responseBuilder, user, foodList);
		if(isUserUpdated)
			this.pushUserInfoCommand(responseBuilder, user);
	}
	
	public void pushRewardCommand(Builder responseBuilder, UserBean user, List<RewardBean> rewards) {
		pushRewardCommand(responseBuilder, user, rewards, "恭喜获得");
	}
	
	public void pushRewardCommand(Builder responseBuilder, UserBean user, List<RewardBean> rewards, String title) {
		MultiReward.Builder builder = MultiReward.newBuilder();
		builder.setName(title);
		builder.addAllLoot(RewardBean.buildRewardInfoList(rewards));
		pushRewardCommand(responseBuilder, user, builder.build());
//		List<HeroInfoBean> heroList = new ArrayList<HeroInfoBean>();
//		List<UserEquipBean> equipList = new ArrayList<UserEquipBean>();
//		List<UserPropBean> propList = new ArrayList<UserPropBean>(); 
//		List<UserHeadBean> headList = new ArrayList<UserHeadBean>();
//		List<Integer> pushRewardIdList = new ArrayList<Integer>();
//		boolean isUserUpdated = false;
//		long userId = user.getId();
//		RewardCommand.Builder rewardbuilder = RewardCommand.newBuilder();
//		for(RewardBean reward : rewards){
//			rewardbuilder.addLoot(reward.buildRewardInfo());
//			int rewardId = reward.getItemid();
//			if (pushRewardIdList.contains(rewardId))
//				continue;
//			if (rewardId > RewardConst.HEAD) {
//				headList.add(userHeadService.selectUserHead(userId, rewardId));
//			} else if (rewardId > RewardConst.HERO) {
//				heroList.addAll(userHeroService.selectUserNewHero(userId));
//			} else if (rewardId > RewardConst.PACKAGE) {
//				propList.add(userPropService.selectUserProp(userId, rewardId));
//			} else if (rewardId > RewardConst.EQUIPMENT) {
//				equipList.add(userEquipService.selectUserEquip(userId, rewardId));
//			} else {
//				isUserUpdated = true;
//			}
//			pushRewardIdList.add(rewardId);
//		}
//		rewardbuilder.setTitle(title);
//		responseBuilder.setRewardCommand(rewardbuilder);
//		if (headList.size() > 0)
//			this.pushUserHeadCommand(responseBuilder, user, headList);
//		if(heroList.size() > 0)
//			this.pushUserHeroListCommand(responseBuilder, user, heroList);
//		if(propList.size() > 0)
//			this.pushUserPropListCommand(responseBuilder, user, propList);
//		if(equipList.size() > 0)
//			this.pushUserEquipListCommand(responseBuilder, user, equipList);
//		if(isUserUpdated)
//			this.pushUserInfoCommand(responseBuilder, user);
	}
	
	public void pushPurchaseCoinCommand(Builder responseBuilder, UserBean user) {
		shopCommandService.getPurchaseCoinTime(responseBuilder, user);
	}
	
	public void pushUserAchieveCommand(Builder responseBuilder, UserBean user) {
		ResponseAchieveListCommand.Builder builder = ResponseAchieveListCommand.newBuilder();
		
		List<UserAchieveBean> uaList = userAchieveService.selectUserAchieveList(user.getId());
		builder.addAllUserAchieve(buildUserAchieveList(uaList));
		
		responseBuilder.setAchieveListCommand(builder.build());
	}
	
	public void pushUserPokedeList(Builder responseBuilder, UserBean user) {
		ResponseUserPokedeCommand.Builder builder = ResponseUserPokedeCommand.newBuilder();
		List<UserPokedeBean> userPokedeList = userPokedeService.selectUserPokedeList(user.getId());
		builder.addAllPokede(buildHeroInfo(userPokedeList, user));
		responseBuilder.setUserPokedeCommand(builder.build());
	}
	
	public void pushUserBosskillRecord(Builder responseBuilder, UserBean user) {
		ResponseBosskillCommand.Builder builder = ResponseBosskillCommand.newBuilder();
		List<BossGroupRecord> list = bossService.getBossGroupRecord(user);
		builder.addAllRecord(list);
		responseBuilder.setBosskillCommand(builder.build());
		
	}
	
	public void pushUserTalentList(Builder responseBuilder, UserBean user) {
		ResponseUserTalentCommand.Builder builder = ResponseUserTalentCommand.newBuilder();
		builder.addAllUserTalent(userTalentService.getUserTalentList(user));
		builder.addAllUserTalentSkill(userTalentService.getUserTalentSkillList(user));
		responseBuilder.setUserTalentCommand(builder.build());
	}
	
	public void pushFightInfoList(Builder responseBuilder, UserBean user) {
		ResponseFightInfoCommand.Builder builder = ResponseFightInfoCommand.newBuilder();
		builder.addAllInfo(userTeamService.getFightInfoList(user));
		responseBuilder.setFightInfoCommand(builder.build());
	}
	
	public void pushUserEquipPokedeList(Builder responseBuilder, UserBean user) {
		ResponseEquipPokedeCommand.Builder builder = ResponseEquipPokedeCommand.newBuilder();
		List<UserEquipPokedeBean> userPokedeList = userEquipPokedeService.selectUserEquipPokedeList(user.getId());
		builder.addAllUserEquipPokede(buildEquipPokede(userPokedeList));
		responseBuilder.setEquipPokedeCommand(builder.build());
	}
}
