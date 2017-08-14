package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.HeroInfoBean;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.MessageBoardBean;
import com.trans.pixel.model.RewardBean;
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
import com.trans.pixel.protoc.ActivityProto.ResponseAchieveListCommand;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.Base.UserTalent;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.EquipProto.ResponseEquipPokedeCommand;
import com.trans.pixel.protoc.EquipProto.ResponseGetUserEquipCommand;
import com.trans.pixel.protoc.EquipProto.ResponseUserPropCommand;
import com.trans.pixel.protoc.HeroProto.ResponseGetUserHeroCommand;
import com.trans.pixel.protoc.HeroProto.ResponseUserFoodCommand;
import com.trans.pixel.protoc.HeroProto.ResponseUserPokedeCommand;
import com.trans.pixel.protoc.HeroProto.ResponseUserTalentCommand;
import com.trans.pixel.protoc.HeroProto.ResponseUserTeamListCommand;
import com.trans.pixel.protoc.MailProto.MailList;
import com.trans.pixel.protoc.MailProto.ResponseGetUserFriendListCommand;
import com.trans.pixel.protoc.MailProto.ResponseGetUserMailListCommand;
import com.trans.pixel.protoc.MailProto.UserFriend;
import com.trans.pixel.protoc.MessageBoardProto.ResponseMessageBoardListCommand;
import com.trans.pixel.protoc.PVPProto.PVPMapList;
import com.trans.pixel.protoc.PVPProto.ResponsePVPMapListCommand;
import com.trans.pixel.protoc.RewardTaskProto.ResponseUserRewardTaskCommand;
import com.trans.pixel.protoc.RewardTaskProto.UserRewardTask;
import com.trans.pixel.protoc.ShopProto.RequestBattletowerShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestDailyShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestExpeditionShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestLadderShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestPVPShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestUnionShopCommand;
import com.trans.pixel.protoc.UnionProto.BossGroupRecord;
import com.trans.pixel.protoc.UnionProto.ResponseBosskillCommand;
import com.trans.pixel.protoc.UnionProto.ResponseUnionInfoCommand;
import com.trans.pixel.protoc.UnionProto.Union;
import com.trans.pixel.protoc.UserInfoProto.ResponseOtherUserInfoCommand;
import com.trans.pixel.protoc.UserInfoProto.ResponseUserHeadCommand;
import com.trans.pixel.protoc.UserInfoProto.ResponseUserInfoCommand;
import com.trans.pixel.protoc.UserInfoProto.RewardCommand;
import com.trans.pixel.service.BossService;
import com.trans.pixel.service.LadderService;
import com.trans.pixel.service.MailService;
import com.trans.pixel.service.MessageService;
import com.trans.pixel.service.PvpMapService;
import com.trans.pixel.service.UnionService;
import com.trans.pixel.service.UserAchieveService;
import com.trans.pixel.service.UserEquipPokedeService;
import com.trans.pixel.service.UserEquipService;
import com.trans.pixel.service.UserFoodService;
import com.trans.pixel.service.UserFriendService;
import com.trans.pixel.service.UserHeadService;
import com.trans.pixel.service.UserHeroService;
import com.trans.pixel.service.UserPokedeService;
import com.trans.pixel.service.UserPropService;
import com.trans.pixel.service.UserRewardTaskService;
import com.trans.pixel.service.UserTalentService;
import com.trans.pixel.service.UserTeamService;

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
	private LevelCommandService levelCommandService;
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
	@Resource
	private RaidCommandService raidCommandService;
	@Resource
	private UserRewardTaskService userRewardTaskService;
	@Resource
	private UnionService unionService;
	
//	public void pushLootResultCommand(Builder responseBuilder, UserBean user) {
//		user = lootService.updateLootResult(user);
//		responseBuilder.setLootResultCommand(super.builderLootResultCommand(user));
//	}
	
	public void pushPvpMapListCommand(Builder responseBuilder, UserBean user) {
		PVPMapList maplist = pvpMapService.getMapList(responseBuilder, user);
		ResponsePVPMapListCommand.Builder builder = ResponsePVPMapListCommand.newBuilder();
		builder.addAllField(maplist.getDataList());
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
		if (responseBuilder.hasUserEquipCommand())
			builder = responseBuilder.getUserEquipCommandBuilder();
		builder.addAllUserEquip(buildUserEquipList(userEquipList));
		responseBuilder.setUserEquipCommand(builder.build());
	}
	
	public void pushUserPropListCommand(Builder responseBuilder, UserBean user) {
		List<UserPropBean> userPropList = userPropService.selectUserPropList(user.getId());
		ResponseUserPropCommand.Builder builder = ResponseUserPropCommand.newBuilder();
		builder.addAllUserProp(super.buildUserPropList(userPropList));
		responseBuilder.setUserPropCommand(builder.build());
	}
	
	public void pushUserPropCommand(Builder responseBuilder, UserBean user, UserPropBean userProp) {
		List<UserPropBean> userPropList = new ArrayList<UserPropBean>();
		userPropList.add(userProp);
		pushUserPropListCommand(responseBuilder, user, userPropList);
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
		pushUserInfoCommand(responseBuilder, user.build());
	}
	
	public void pushUserInfoCommand(Builder responseBuilder, UserInfo user) {
		ResponseUserInfoCommand.Builder builder = ResponseUserInfoCommand.newBuilder();
		UserInfo.Builder userInfo = UserInfo.newBuilder(user);
//		UserLevelBean userLevel = levelCommandService.getUserLevel(user.getId());
//		if(userLevel != null){//cal loot result
//			userInfo.setExp(userInfo.getExp() + (RedisService.now()-userLevel.getLootTime())*userLevel.getExp());
//			userInfo.setCoin(userInfo.getCoin() + (RedisService.now()-userLevel.getLootTime())*userLevel.getCoin());
//		}
		builder.setUser(userInfo);
		responseBuilder.setUserInfoCommand(builder.build());
	}
	
	public void pushOtherUserInfoCommand(Builder responseBuilder, List<UserInfo> userList) {
		ResponseOtherUserInfoCommand.Builder builder = ResponseOtherUserInfoCommand.newBuilder();
		builder.addAllUser(userList);
		responseBuilder.setOtherUserInfoCommand(builder.build());
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
		UserLevelBean userLevel = levelCommandService.getUserLevel(user.getId());
		levelCommandService.pushLevelLootCommand(responseBuilder, userLevel, user);
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
	
	public void pushMessageBoardListCommand(int type, Builder responseBuilder, UserBean user, int itemId) {
		List<MessageBoardBean> messageBoardList = messageService.getMessageBoardList(type, user, itemId);
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
	
	public void pushUserTeamListCommand(Builder responseBuilder, UserBean user, List<UserTeamBean> userTeamList) {
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
	
	public void pushRaidShopCommand(Builder responseBuilder, UserBean user) {
		shopCommandService.RaidShop(responseBuilder, user);
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
	
	public void pushBattletowerShopCommand(Builder responseBuilder, UserBean user) {
		RequestBattletowerShopCommand.Builder cmd = RequestBattletowerShopCommand.newBuilder();
		shopCommandService.battletowerShop(cmd.build(), responseBuilder, user);
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

	public void pushUserRaid(Builder responseBuilder, UserBean user){
		raidCommandService.getRaid(responseBuilder, user);
	}
	
	/**
	 * Include RewardCommand
	 */
//	public void pushRewardCommand(Builder responseBuilder, UserBean user, int rewardId, String rewardName, long rewardCount) {
//		RewardCommand.Builder reward = RewardCommand.newBuilder();
//		RewardInfo.Builder builder = RewardInfo.newBuilder();
//		builder.setItemid(rewardId);
//		builder.setName(rewardName);
//		builder.setCount(rewardCount);
//		reward.addLoot(builder);
//		reward.setTitle("恭喜获得");
//		responseBuilder.setRewardCommand(reward);
//		pushUserDataByRewardId(responseBuilder, user, rewardId);
//	}
	
	/**
	 * No RewardCommand include
	 */
	public void pushRewardCommand(Builder responseBuilder, UserBean user, int rewardId) {
		pushUserDataByRewardId(responseBuilder, user, rewardId);
	}
	
	public void pushUserDataByRewardId(Builder responseBuilder, UserBean user, int rewardId) {
		pushUserDataByRewardId(responseBuilder, user, rewardId, false);
	}
	
	public void pushUserDataByRewardId(Builder responseBuilder, UserBean user, int rewardId, boolean needPushReward) {
		MultiReward.Builder rewards = MultiReward.newBuilder();
		rewards.addLoot(RewardInfo.newBuilder());
		rewards.getLootBuilder(0).setItemid(rewardId);
		rewards.getLootBuilder(0).setCount(1);
		pushRewardCommand(responseBuilder, user, rewards.build(), needPushReward);
	}

	public void pushRewardCommand(Builder responseBuilder, UserBean user, MultiReward rewards) {
		pushRewardCommand(responseBuilder, user, rewards, true);
	}
	
	public void pushRewardCommand(Builder responseBuilder, UserBean user, MultiReward rewards, boolean needPushReward) {
		if (rewards == null || rewards.getLootCount() == 0)
			return;
		List<HeroInfoBean> heroList = new ArrayList<HeroInfoBean>();
		List<UserEquipBean> equipList = new ArrayList<UserEquipBean>();
		List<UserPropBean> propList = new ArrayList<UserPropBean>(); 
		List<UserHeadBean> headList = new ArrayList<UserHeadBean>();
//		List<Integer> pushRewardIdList = new ArrayList<Integer>();
		List<UserFoodBean> foodList = new ArrayList<UserFoodBean>();
		List<UserEquipPokedeBean> equipPokedeList = new ArrayList<UserEquipPokedeBean>();
		List<UserTalent> userTalentList = new ArrayList<UserTalent>();
		List<UserEquipBean> userEquipList = null;
		boolean needUpdateUser = false;
		long userId = user.getId();
		for(RewardInfo reward : rewards.getLootList()){
			int rewardId = reward.getItemid();
//			if(rewardId < 100)
//				continue;
//			if (pushRewardIdList.contains(rewardId))
//				continue;
			 
			if (rewardId < 100 && rewardId > 0) {
				UserTalent.Builder userTalent = userTalentService.getUserTalent(user, rewardId);
				if (userTalent != null)
					userTalentList.add(userTalent.build());
			} else if (rewardId > RewardConst.FOOD) {
				UserFoodBean food = userFoodService.selectUserFood(user, rewardId);
				if(food != null)
					foodList.add(food);
			} else if (rewardId > RewardConst.HEAD) {
				UserHeadBean head = userHeadService.selectUserHead(userId, rewardId);
				if(head != null)
					headList.add(head);
			} else if (rewardId > RewardConst.HERO) {
				heroList.addAll(userHeroService.selectUserNewHero(userId));
			} else if (rewardId > RewardConst.PACKAGE) {
				if(rewardId / 1000 == 44)
					needUpdateUser = true;
				else{
					UserPropBean prop = userPropService.selectUserProp(userId, rewardId);
					if(prop != null)
						propList.add(prop);
				}
			} else if (rewardId > RewardConst.CHIP) {
//				equipList.add(userEquipService.selectUserEquip(userId, rewardId));
				if(userEquipList == null)
					userEquipList = userEquipService.selectUserEquipList(user.getId());
				UserEquipBean equip = userEquipService.selectUserEquipByEquipList(userEquipList, rewardId);
				if(equip != null)
					equipList.add(equip);
			} else if (rewardId > RewardConst.EQUIPMENT) {
//				equipPokedeList.add(userEquipPokedeService.selectUserEquipPokede(user, rewardId));
			} else if (rewardId == RewardConst.ZHUJUEEXP) {
				UserTalent.Builder userTalent = userTalentService.getUsingTalent(user);
				if (userTalent != null)
					userTalentList.add(userTalent.build());
			} else {
				needUpdateUser = true;
			}
			
//			pushRewardIdList.add(rewardId);
		}
		if (needPushReward) {
			RewardCommand.Builder reward = RewardCommand.newBuilder();
			if(responseBuilder.hasRewardCommand())
				reward = responseBuilder.getRewardCommandBuilder();
			reward.setTitle(rewards.getName());
			reward.addAllLoot(rewards.getLootList());
			for(int i = reward.getLootCount()-1; i >= 0; i--) {
				if(reward.getLoot(i).getItemid() < 100)//主角不返回
					reward.removeLoot(i);
			}
			if(reward.getLootCount() > 0)
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
		if (!equipPokedeList.isEmpty())
			this.pushUserEquipPokedeList(responseBuilder, user, equipPokedeList);
		if (!userTalentList.isEmpty())
			this.pushUserTalentList(responseBuilder, user, userTalentList);
		if(needUpdateUser)
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
		for(UserTalent.Builder talent : userTalentService.getUserTalentList(user))
			builder.addUserTalent(talent);
		builder.addAllUserTalentSkill(userTalentService.getUserTalentSkillList(user));
		responseBuilder.setUserTalentCommand(builder.build());
	}
	
	public void pushUserTalentList(Builder responseBuilder, UserBean user, List<UserTalent> userTalentList) {
		ResponseUserTalentCommand.Builder builder = ResponseUserTalentCommand.newBuilder();
		builder.addAllUserTalent(userTalentList);
		for (UserTalent userTalent : userTalentList) {
			builder.addAllUserTalentSkill(userTalentService.getUserTalentSkillListByTalentId(user, userTalent.getId()));
		}
		responseBuilder.setUserTalentCommand(builder.build());
	}
	
	public void pushUserTalentListNotPushSkill(Builder responseBuilder, UserBean user, List<UserTalent> userTalentList) {
		ResponseUserTalentCommand.Builder builder = ResponseUserTalentCommand.newBuilder();
		builder.addAllUserTalent(userTalentList);
		responseBuilder.setUserTalentCommand(builder.build());
	}
	
	public void pushUserTalent(Builder responseBuilder, UserBean user, UserTalent userTalent) {
		ResponseUserTalentCommand.Builder builder = ResponseUserTalentCommand.newBuilder();
		builder.addUserTalent(userTalent);
		
		responseBuilder.setUserTalentCommand(builder.build());
	}
	
	public void pushUserEquipPokedeList(Builder responseBuilder, UserBean user) {
		ResponseEquipPokedeCommand.Builder builder = ResponseEquipPokedeCommand.newBuilder();
		List<UserEquipPokedeBean> userPokedeList = userEquipPokedeService.selectUserEquipPokedeList(user.getId());
		builder.addAllUserEquipPokede(buildEquipPokede(userPokedeList));
		responseBuilder.setEquipPokedeCommand(builder.build());
	}
	
	public void pushUserEquipPokedeList(Builder responseBuilder, UserBean user, List<UserEquipPokedeBean> userPokedeList) {
		ResponseEquipPokedeCommand.Builder builder = ResponseEquipPokedeCommand.newBuilder();
		builder.addAllUserEquipPokede(buildEquipPokede(userPokedeList));
		responseBuilder.setEquipPokedeCommand(builder.build());
	}
	
	public void pushUserRewardTask(Builder responseBuilder, UserBean user) {
		ResponseUserRewardTaskCommand.Builder builder = ResponseUserRewardTaskCommand.newBuilder();
		Map<Integer, UserRewardTask> list = userRewardTaskService.getUserRewardTaskList(user);
		builder.addAllUserRewardTask(list.values());
		responseBuilder.setUserRewardTaskCommand(builder.build());
	}
	
	public void pushUserUnion(Builder responseBuilder, UserBean user) {
		Union union = unionService.getUnion(user, false);
		if(union == null){
			return;
		}
		
		ResponseUnionInfoCommand.Builder builder = ResponseUnionInfoCommand.newBuilder();
		builder.setUnion(union);
		responseBuilder.setUnionInfoCommand(builder.build());
	}
}
