package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ActivityConst;
import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserHeadBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.Base.TeamEngine;
import com.trans.pixel.protoc.Base.UserTalent;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.MessageBoardProto.RequestGreenhandCommand;
import com.trans.pixel.protoc.RechargeProto.RequestBindAccountCommand;
import com.trans.pixel.protoc.RechargeProto.RequestSubmitIconCommand;
import com.trans.pixel.protoc.Request.RequestCommand;
import com.trans.pixel.protoc.ServerProto.HeadInfo;
import com.trans.pixel.protoc.UserInfoProto.RequestBindRecommandCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestChangeUserNameCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestExtraRewardCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestLoginCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestRecommandCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestRegisterCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestRemoveRecommandCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestSignNameCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestSubmitRiteCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestUserInfoCommand;
import com.trans.pixel.protoc.UserInfoProto.ResponseRecommandCommand;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.BlackListService;
import com.trans.pixel.service.CostService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.LootService;
import com.trans.pixel.service.NoticeMessageService;
import com.trans.pixel.service.RechargeService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.ServerService;
import com.trans.pixel.service.ShopService;
import com.trans.pixel.service.UserHeadService;
import com.trans.pixel.service.UserHeroService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.UserTalentService;
import com.trans.pixel.service.UserTeamService;
import com.trans.pixel.service.redis.LevelRedisService;
import com.trans.pixel.service.redis.RechargeRedisService;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.utils.DateUtil;

@Service
public class UserCommandService extends BaseCommandService {
	private static final Logger logger = LoggerFactory
			.getLogger(UserCommandService.class);

	@Resource
	private UserService userService;
	@Resource
	private PushCommandService pushCommandService;
	@Resource
	private NoticeCommandService noticeCommandService;
	@Resource
	private ActivityService activityService;
	@Resource
	private UserHeroService userHeroService;
	@Resource
	private UserTeamService userTeamService;
	@Resource
	private UserHeadService userHeadService;
	@Resource
	private BlackListService blackService;
	@Resource
	private ShopService shopService;
	@Resource
	private LogService logService;
	@Resource
	private ServerService serverService;
	@Resource
	private UserTalentService userTalentService;
	@Resource
	private LevelRedisService userLevelService;
	@Resource
	private NoticeMessageService noticeMessageService;
	@Resource
	private LootService lootService;
	@Resource
	private CostService costService;
	@Resource
	private RechargeService rechargeService;
	@Resource
	private RechargeRedisService rechargeRedisService;
	@Resource
	private RewardService rewardService;
	
	public void login(RequestCommand request, Builder responseBuilder) {
		HeadInfo head = request.getHead();
		if (blackService.isNoaccount(head.getAccount())) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BLACK_ACCOUNT_ERROR);
			logService.sendErrorLog(/* head.getAccount() */0,
					head.getServerId(), RequestLoginCommand.class,
					RedisService.formatJson(head),
					ErrorConst.BLACK_ACCOUNT_ERROR);
			responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		UserBean user = userService.getUserByAccount(head.getServerId(),
				head.getAccount());
		if (user == null) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.USER_NOT_EXIST);
			logService.sendErrorLog(/* head.getAccount() */0,
					head.getServerId(), RequestLoginCommand.class,
					RedisService.formatJson(head), ErrorConst.USER_NOT_EXIST);
			responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		if (blackService.isNologin(user.getId())
				|| blackService.isNoDevice(user.getIdfa())) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BLACK_USER_ERROR);
			logService.sendErrorLog(user.getId(), user.getServerId(),
					RequestLoginCommand.class, RedisService.formatJson(head),
					ErrorConst.BLACK_USER_ERROR);
			responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		// RequestLoginCommand cmd = request.getLoginCommand();
		// if(cmd.getZhanli() > user.getZhanli()){
		// user.setZhanli(cmd.getZhanli());
		// userService.updateUserDailyData(user);
		// }
		user.setVersion(head.getVersion() + "");
		userHeroService.selectUserNewHero(user.getId());
		refreshUserLogin(user);
		
		MultiReward rewards = rechargeService.handlerRecharge(user);
		
		if (rewards != null) {
			handleRewards(responseBuilder, user, rewards, false);
		}
		
		MultiReward rewards1 = rechargeRedisService.getUserLoginReward(user.getId());
		if (rewards1 != null && rewards1.getLootCount() > 0) {
			for (RewardInfo reward : rewards1.getLootList()) {
				if (reward.getItemid() == RewardConst.VIPEXP)
					rewardService.doReward(user, reward.getItemid(), reward.getCount(), 0, false);
				else
					rewardService.doReward(user, reward.getItemid(), reward.getCount(), 0);
//				handleRewards(responseBuilder, user, rewards1, false);
			}
		}
		
		/**
		 * login activity
		 */
		activityService.handleActivity(user, ActivityConst.ACTIVITY_TYPE_LOGIN);

		userService.cache(user.getServerId(), user.buildShort());
		lootService.calLoot(user, responseBuilder, true);
		pushCommand(responseBuilder, user);
		pushCommandService.pushUserInfoCommand(responseBuilder, user);

		noticeMessageService.composeLogin(user);
	}

	public void register(RequestCommand request, Builder responseBuilder) {
		RequestRegisterCommand registerCommand = request.getRegisterCommand();
		HeadInfo head = request.getHead();
		if (blackService.isNoaccount(head.getAccount())) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BLACK_ACCOUNT_ERROR);
			logService.sendErrorLog(/* head.getAccount() */0,
					head.getServerId(), RequestRegisterCommand.class,
					RedisService.formatJson(head),
					ErrorConst.BLACK_ACCOUNT_ERROR);
			responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		UserBean user = new UserBean();
		user = userService.getUserByAccount(head.getServerId(),
				head.getAccount());
		if (user == null) {
			user = new UserBean();
			user.init(head.getServerId(), head.getAccount(), userService
					.handleUserName(head.getServerId(),
							registerCommand.getUserName()), registerCommand
					.getIcon());
			user.setRegisterTime(DateUtil
					.getCurrentDate(TimeConst.DEFAULT_DATETIME_FORMAT));
			// }
			boolean hasRegistered = false;
			try {
				userService.addNewUser(user);
			} catch (Exception e) {
				logger.error(e.getMessage());
				hasRegistered = true;
			}
			if (hasRegistered) {
				user = userService.getUserByAccount(head.getServerId(),
						head.getAccount());
				if (user == null) {
					ErrorCommand errorCommand = buildErrorCommand(ErrorConst.ACCOUNT_REGISTER_FAIL);
					logService.sendErrorLog(/* head.getAccount() */0,
							head.getServerId(), RequestRegisterCommand.class,
							RedisService.formatJson(head),
							ErrorConst.ACCOUNT_REGISTER_FAIL);
					responseBuilder.setErrorCommand(errorCommand);
					return;
				}
			} else {
				// user.setUserType(userService.randomUserType());
				int addHeroId = registerCommand.getHeroId();
				UserTalent userTalent = addRegisterTalent(user, addHeroId);

				if (userTalent != null)
					user.setUseTalentId(addHeroId);

				user.setFirstGetHeroId(addHeroId);
				addRegisterTeam(user, user.getUseTalentId());
				/**
				 * register activity
				 */
				activityService.handleActivity(user,
						ActivityConst.ACTIVITY_TYPE_REGISTER);

				userService.refreshUserDailyData(user);
			}
		}

		user.setVersion(head.getVersion() + "");
		if (serverService.getOnlineStatus(user.getVersion()) != 0) {
			user.setGreenhand("14");
			user.setSkill(1);
			user.setAdvance(1);
			user.setFailed(1);
		}
		refreshUserLogin(user);
		userService.cache(user.getServerId(), user.buildShort());
		pushCommandService.pushUserInfoCommand(responseBuilder, user);

		pushCommand(responseBuilder, user);
	}

	private UserTalent addRegisterTalent(UserBean user, int id) {
		return userTalentService.initTalent(user, id);
	}

	private void addRegisterTeam(UserBean user, int roleId) {
		String teamRecord = "0,0|";

		// userTeamService.updateUserTeam(user.getId(), 2, teamRecord, user, 1,
		// new ArrayList<TeamEngine>(), roleId);
		// userTeamService.updateUserTeam(user.getId(), 3, teamRecord, user, 1,
		// new ArrayList<TeamEngine>(), roleId);
		// userTeamService.updateUserTeam(user.getId(), 4, teamRecord, user, 1,
		// new ArrayList<TeamEngine>(), roleId);
		// userTeamService.updateUserTeam(user.getId(), 5, teamRecord, user, 1,
		// new ArrayList<TeamEngine>(), roleId);
		userTeamService.updateUserTeam(user.getId(), 1, teamRecord, user, 1,
				new ArrayList<TeamEngine>(), roleId);
		// userTeamService.updateUserTeam(user.getId(), 2, "", "");
		// userTeamService.updateUserTeam(user.getId(), 3, "", "");
		// userTeamService.updateUserTeam(user.getId(), 4, "", "");
		// userTeamService.updateUserTeam(user.getId(), 5, "", "");
	}

	public void submitIcon(RequestSubmitIconCommand cmd,
			Builder responseBuilder, UserBean user) {
		int icon = 0;
		int frame = 0;
		if (cmd.hasIcon())
			icon = cmd.getIcon();
		if (cmd.hasFrame())
			frame = cmd.getFrame();
		if (icon > 62000) {
			UserHeadBean userHead = userHeadService.selectUserHead(
					user.getId(), icon);
			if (userHead == null) {
				logService.sendErrorLog(user.getId(), user.getServerId(),
						cmd.getClass(), RedisService.formatJson(cmd),
						ErrorConst.HEAD_NOT_EXIST);

				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.HEAD_NOT_EXIST);
				responseBuilder.setErrorCommand(errorCommand);
				pushCommandService.pushUserInfoCommand(responseBuilder, user);
				return;
			}
		}
		if (frame > 0 && frame != 65001) {
			UserHeadBean userHead = userHeadService.selectUserHead(
					user.getId(), frame);
			if (userHead == null) {
				logService.sendErrorLog(user.getId(), user.getServerId(),
						cmd.getClass(), RedisService.formatJson(cmd),
						ErrorConst.HEAD_NOT_EXIST);

				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.HEAD_NOT_EXIST);
				responseBuilder.setErrorCommand(errorCommand);
				pushCommandService.pushUserInfoCommand(responseBuilder, user);
				return;
			}
		}
		if (icon > 0)
			user.setIcon(icon);
		if (frame > 0)
			user.setFrame(frame);

		userService.cache(user.getServerId(), user.buildShort());
		userService.updateUser(user);

		pushCommandService.pushUserInfoCommand(responseBuilder, user);
		// responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.SUBMIT_SUCCESS));
	}

	public void bindAccount(RequestBindAccountCommand cmd,
			Builder responseBuilder, UserBean user) {
		if (!user.getAccount().equals(cmd.getOldAccount())) {
			logService.sendErrorLog(user.getId(), user.getServerId(),
					cmd.getClass(), RedisService.formatJson(cmd),
					ErrorConst.USER_NOT_EXIST);

			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.USER_NOT_EXIST);
			responseBuilder.setErrorCommand(errorCommand);
			// pushCommandService.pushUserInfoCommand(responseBuilder, user);
			return;
		}
		UserBean olduser = userService.getUserByAccount(user.getServerId(),
				cmd.getNewAccount());
		if (olduser != null) {
			logService.sendErrorLog(user.getId(), user.getServerId(),
					cmd.getClass(), RedisService.formatJson(cmd),
					ErrorConst.USER_HAS_EXIST);

			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.USER_HAS_EXIST);
			responseBuilder.setErrorCommand(errorCommand);
			pushCommandService.pushUserInfoCommand(responseBuilder, user);
			return;
		}
		user.setAccount(cmd.getNewAccount());
		userService.updateUserToMysql(user);
		userService.updateUser(user);
		userService.delUserIdByAccount(user.getServerId(), cmd.getOldAccount());
		userService.setUserIdByAccount(user.getServerId(), user.getAccount(),
				user.getId());

		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}

	public void submitGreenhand(RequestGreenhandCommand cmd,
			Builder responseBuilder, UserBean user) {
		if (cmd.hasGreenhand()) {
			user.setGreenhand(cmd.getGreenhand());

			UserLevelBean userLevel = userLevelService.getUserLevel(user);
			/**
			 * send greenhand log
			 */
			logService.sendGreenhandLog(user.getServerId(), user.getId(),
					userLevel.getUnlockDaguan(), cmd.getGreenhand(),
					cmd.hasResult() ? cmd.getResult() : true);
		}
		if (cmd.hasAdvance())
			user.setAdvance(cmd.getAdvance());
		if (cmd.hasSkill())
			user.setSkill(cmd.getSkill());
		if (cmd.hasFailed())
			user.setFailed(cmd.getFailed());

		userService.updateUser(user);

		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}

	public void userInfo(RequestUserInfoCommand cmd, Builder responseBuilder,
			UserBean user) {
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}

	public void extra(RequestExtraRewardCommand cmd, Builder responseBuilder,
			UserBean user) {
		int status = cmd.getStatus();
		int type = 0;
		if (cmd.hasExtraType())
			type = cmd.getExtraType();
		List<RewardBean> rewardList = new ArrayList<RewardBean>();
		ResultConst ret = userService.handleExtra(user, status, type,
				rewardList);
		if (ret instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(),
					cmd.getClass(), RedisService.formatJson(cmd), ret);

			ErrorCommand errorCommand = buildErrorCommand(ret);
			responseBuilder.setErrorCommand(errorCommand);
		}
		if (!rewardList.isEmpty()) {
			handleRewards(responseBuilder, user, rewardList);
		}
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}

	public void bindRecommand(RequestBindRecommandCommand cmd,
			Builder responseBuilder, UserBean user) {
		if (user.getRecommandUserId() != 0) {
			logService.sendErrorLog(user.getId(), user.getServerId(),
					cmd.getClass(), RedisService.formatJson(cmd),
					ErrorConst.RECOMMAND_IS_EXIST_ERROR);

			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.RECOMMAND_IS_EXIST_ERROR);
			responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		if (user.getId() == cmd.getUserId()) {
			logService.sendErrorLog(user.getId(), user.getServerId(),
					cmd.getClass(), RedisService.formatJson(cmd),
					ErrorConst.RECOMMAND_CAN_NOT_SELF_ERROR);

			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.RECOMMAND_CAN_NOT_SELF_ERROR);
			responseBuilder.setErrorCommand(errorCommand);
			return;
		}

		UserBean other = userService.getUserOther(cmd.getUserId());
		if (other == null) {
			logService.sendErrorLog(user.getId(), user.getServerId(),
					cmd.getClass(), RedisService.formatJson(cmd),
					ErrorConst.FRIEND_NOT_EXIST);

			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.FRIEND_NOT_EXIST);
			responseBuilder.setErrorCommand(errorCommand);
			return;
		}

		if (cmd.hasIsFirst() && cmd.getIsFirst() && other.getFriendVip() == 0) {
			ResponseRecommandCommand.Builder builder = ResponseRecommandCommand
					.newBuilder();
			builder.setUser(other.buildShort());
			return;
		}

		userService.handlerRecommand(user, cmd.getUserId());

		ResponseRecommandCommand.Builder builder = ResponseRecommandCommand
				.newBuilder();
		if (user.getRecommandUserId() != 0) {
			builder.setUser(other.buildShort());
		}
		builder.setCount(userService.getRecommands(user));
		responseBuilder.setRecommandCommand(builder.build());
		if (other.getFriendVip() == 1) {
			MultiReward.Builder rewards = MultiReward.newBuilder();
			rewards.addLoot(RewardBean.init(RewardConst.JEWEL, 300)
					.buildRewardInfo());
			rewards.addLoot(RewardBean.init(RewardConst.ZHAOHUANSHI, 3)
					.buildRewardInfo());
			handleRewards(responseBuilder, user, rewards);
		}
	}
	
	public void removeRecommand(RequestRemoveRecommandCommand cmd,
			Builder responseBuilder, UserBean user) {
		if (user.getRecommandUserId() == 0) {
			logService.sendErrorLog(user.getId(), user.getServerId(),
					cmd.getClass(), RedisService.formatJson(cmd),
					ErrorConst.RECOMMAND_IS_NOT_EXIST_ERROR);

			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.RECOMMAND_IS_NOT_EXIST_ERROR);
			responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		
		if (!costService.cost(user, RewardConst.JEWEL, 1500)) {
			logService.sendErrorLog(user.getId(), user.getServerId(),
					cmd.getClass(), RedisService.formatJson(cmd),
					ErrorConst.NOT_ENOUGH_JEWEL);

			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_ENOUGH_JEWEL);
			responseBuilder.setErrorCommand(errorCommand);
			return;
		}

		userService.handlerRemoveRecommand(user);

		ResponseRecommandCommand.Builder builder = ResponseRecommandCommand
				.newBuilder();
		builder.setCount(userService.getRecommands(user));
		responseBuilder.setRecommandCommand(builder.build());
	}


	public void recommand(RequestRecommandCommand cmd, Builder responseBuilder,
			UserBean user) {
		ResponseRecommandCommand.Builder builder = ResponseRecommandCommand
				.newBuilder();
		if (user.getRecommandUserId() != 0) {
			UserBean userBean = userService.getUserOther(user
					.getRecommandUserId());
			if (userBean != null)
				builder.setUser(userBean.buildShort());
		}
		builder.setCount(userService.getRecommands(user));
		responseBuilder.setRecommandCommand(builder.build());
	}

	public void changeName(RequestChangeUserNameCommand cmd,
			Builder responseBuilder, UserBean user) {
		if (userService.queryUserIdByUserName(user.getServerId(),
				cmd.getNewName()) != 0) {
			logService.sendErrorLog(user.getId(), user.getServerId(),
					cmd.getClass(), RedisService.formatJson(cmd),
					ErrorConst.USER_NAME_IS_EXIST_ERROR);

			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.USER_NAME_IS_EXIST_ERROR);
			responseBuilder.setErrorCommand(errorCommand);
			return;
		}

		if (!costService.cost(user, RewardConst.JEWEL, 200)) {
			logService.sendErrorLog(user.getId(), user.getServerId(),
					cmd.getClass(), RedisService.formatJson(cmd),
					ErrorConst.NOT_ENOUGH_JEWEL);

			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_ENOUGH_JEWEL);
			responseBuilder.setErrorCommand(errorCommand);
			return;
		}

		userService.deleteUserIdByName(user.getServerId(), user.getUserName());
		user.setUserName(cmd.getNewName());
		userService.setUserIdByName(user.getServerId(), user.getUserName(),
				user.getId());

		userService.updateUser(user);

		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}

	public void signName(RequestSignNameCommand cmd, Builder responseBuilder,
			UserBean user) {
		user.setSignName(cmd.getSignName());
		userService.updateUser(user);
		userService.cache(user.getServerId(), user.buildShort());

		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}
	
	public void submitRite(RequestSubmitRiteCommand cmd, Builder responseBuilder, UserBean user) {
		if (user.getRite() == 1) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.ACTIVITY_REWARD_HAS_GET_ERROR);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.ACTIVITY_REWARD_HAS_GET_ERROR);
			responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		user.setRite(1);
		List<RewardInfo> rewardList = RewardBean.initRewardInfoList(34092, 25);
		rewardList.add(RewardBean.init(1002, 500).buildRewardInfo());
		MultiReward.Builder rewards = MultiReward.newBuilder();
		rewards.addAllLoot(rewardList);
		handleRewards(responseBuilder, user, rewards.build());
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}
	
	private void pushCommand(Builder responseBuilder, UserBean user) {
		pushCommandService.pushLevelLootCommand(responseBuilder, user);
		pushCommandService.pushUserFriendListCommand(responseBuilder, user);
		pushCommandService.pushUserHeroListCommand(responseBuilder, user);
		pushCommandService.pushUserEquipListCommand(responseBuilder, user);
		// pushCommandService.pushLadderRankListCommand(responseBuilder, user);
		pushCommandService.pushUserTeamListCommand(responseBuilder, user);
		// pushCommandService.pushShopCommand(responseBuilder, user);
		// pushCommandService.pushDailyShopCommand(responseBuilder, user);
		// pushCommandService.pushBlackShopCommand(responseBuilder, user);
		// pushCommandService.pushPVPShopCommand(responseBuilder, user);
		// pushCommandService.pushExpeditionShopCommand(responseBuilder, user);
		// pushCommandService.pushLadderShopCommand(responseBuilder, user);
		// pushCommandService.pushUnionShopCommand(responseBuilder, user);
		// pushCommandService.pushBattletowerShopCommand(responseBuilder, user);
		// pushCommandService.pushUserMailListCommand(responseBuilder, user);
		// pushCommandService.pushPurchaseCoinCommand(responseBuilder, user);
		pushCommandService.pushUserPropListCommand(responseBuilder, user);
		pushCommandService.pushPvpMapListCommand(responseBuilder, user);
		// pushCommandService.pushUserAchieveCommand(responseBuilder, user);
		pushCommandService.pushUserHeadCommand(responseBuilder, user);
		pushCommandService.pushUserPokedeList(responseBuilder, user);
		// pushCommandService.pushUserFoodListCommand(responseBuilder, user);
		shopService.getLibaoShop(responseBuilder, user);
		// pushCommandService.pushUserBosskillRecord(responseBuilder, user);
		pushCommandService.pushUserTalentList(responseBuilder, user);
		// noticeCommandService.pushNotices(responseBuilder, user);
		pushCommandService.pushUserEquipPokedeList(responseBuilder, user);
		pushCommandService.pushUserRaid(responseBuilder, user);
		pushCommandService.pushUserUnion(responseBuilder, user);
	}

	private void refreshUserLogin(UserBean user) {
		// if (DateUtil.isNextDay(user.getLastLoginTime())) {
		// //每日首次登陆
		// // VipInfo vip = userService.getVip(user.getVip());
		// // if(vip != null){
		// //// user.setPurchaseCoinLeft(user.getPurchaseCoinLeft() +
		// vip.getDianjin());
		// // }
		// user.setLoginDays(user.getLoginDays() + 1);

		// /**
		// * 累计登录的活动
		// */
		// activityService.loginActivity(user);

		// user.setSignCount(0);
		// }

		user.setSession(DigestUtils.md5Hex(user.getAccount()
				+ System.currentTimeMillis()));
		userService.updateUser(user);
	}
}
