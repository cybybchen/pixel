package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ActivityConst;
import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserHeadBean;
import com.trans.pixel.protoc.Base.UserTalent;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.MessageBoardProto.RequestGreenhandCommand;
import com.trans.pixel.protoc.RechargeProto.RequestBindAccountCommand;
import com.trans.pixel.protoc.RechargeProto.RequestSubmitIconCommand;
import com.trans.pixel.protoc.Request.RequestCommand;
import com.trans.pixel.protoc.UserInfoProto.HeadInfo;
import com.trans.pixel.protoc.UserInfoProto.RequestExtraRewardCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestLoginCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestRegisterCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestUserInfoCommand;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.BlackListService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.ServerService;
import com.trans.pixel.service.ShopService;
import com.trans.pixel.service.UserHeadService;
import com.trans.pixel.service.UserHeroService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.UserTalentService;
import com.trans.pixel.service.UserTeamService;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.utils.DateUtil;

@Service
public class UserCommandService extends BaseCommandService {
	private static final Logger logger = LoggerFactory.getLogger(UserCommandService.class);
	
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
	private RewardService rewardService;
	
	
	public void login(RequestCommand request, Builder responseBuilder) {
		HeadInfo head = request.getHead();
		if (blackService.isNoaccount(head.getAccount())) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BLACK_ACCOUNT_ERROR);
			logService.sendErrorLog(/*head.getAccount()*/0, head.getServerId(), RequestLoginCommand.class, RedisService.formatJson(head), ErrorConst.BLACK_ACCOUNT_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		UserBean user = userService.getUserByAccount(head.getServerId(), head.getAccount());
		if (user == null) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.USER_NOT_EXIST);
			logService.sendErrorLog(/*head.getAccount()*/0, head.getServerId(), RequestLoginCommand.class, RedisService.formatJson(head), ErrorConst.USER_NOT_EXIST);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		if (blackService.isNologin(user.getId()) || blackService.isNoidfa(user.getIdfa())) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BLACK_USER_ERROR);
            logService.sendErrorLog(user.getId(), user.getServerId(), RequestLoginCommand.class, RedisService.formatJson(head), ErrorConst.BLACK_USER_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
//		RequestLoginCommand cmd = request.getLoginCommand();
//		if(cmd.getZhanli() > user.getZhanli()){
//			user.setZhanli(cmd.getZhanli());
//			userService.updateUserDailyData(user);
//		}
		user.setVersion(head.getVersion()+"");
		userHeroService.selectUserNewHero(user.getId());
		refreshUserLogin(user);
		
		/**
		 * login activity
		 */
		activityService.handleActivity(user, ActivityConst.ACTIVITY_TYPE_LOGIN);
		
		userService.cache(user.getServerId(), user.buildShort());
		pushCommand(responseBuilder, user);
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}

	public void register(RequestCommand request, Builder responseBuilder) {
		RequestRegisterCommand registerCommand = request.getRegisterCommand();
		HeadInfo head = request.getHead();
		if (blackService.isNoaccount(head.getAccount())) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BLACK_ACCOUNT_ERROR);
            logService.sendErrorLog(/*head.getAccount()*/0, head.getServerId(), RequestRegisterCommand.class, RedisService.formatJson(head), ErrorConst.BLACK_ACCOUNT_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		UserBean user = new UserBean();
		user.init(head.getServerId(), head.getAccount(), userService.handleUserName(head.getServerId(), registerCommand.getUserName()), registerCommand.getIcon());
		user.setRegisterTime(DateUtil.getCurrentDate(TimeConst.DEFAULT_DATETIME_FORMAT));
		boolean hasRegistered = false;
		try{
			userService.addNewUser(user);
		}catch(Exception e){
			logger.error(e.getMessage());
			hasRegistered = true;
		}
		if(hasRegistered){
			user = userService.getUserByAccount(head.getServerId(), head.getAccount());
			if (user == null) {
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.ACCOUNT_REGISTER_FAIL);
				logService.sendErrorLog(/*head.getAccount()*/0, head.getServerId(), RequestRegisterCommand.class, RedisService.formatJson(head), ErrorConst.ACCOUNT_REGISTER_FAIL);
            	responseBuilder.setErrorCommand(errorCommand);
				return;
			}
		}else{
			int addHeroId = registerCommand.getHeroId();
			UserTalent userTalent = addRegisterTalent(user, addHeroId);
//			addRegisterTeam(user);
			if (userTalent != null)
				user.setUseTalentId(addHeroId);
				
			user.setFirstGetHeroId(addHeroId);
			
			/**
			 * register activity
			 */
			activityService.handleActivity(user, ActivityConst.ACTIVITY_TYPE_REGISTER);
		}

		user.setVersion(head.getVersion()+"");
		if(serverService.getOnlineStatus(user.getVersion()) != 0){
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
		return userTalentService.getRegisterTalent(user, id);
	}
	
//	private void addRegisterTeam(UserBean user) {
//		List<HeroInfoBean> userHeroList = userHeroService.selectUserHeroList(user);
//		String teamRecord = "";
//		for (HeroInfoBean hero : userHeroList) {
//			teamRecord += hero.getHeroId() + "," + hero.getId() + "|";
////			int skillid = hero.getSkillInfoList().get(0).getSkillId();
////			composeSkill = hero.getHeroId()+","+hero.getId()+","+skillid+"_"+skillid+"_组合技";
////			switch(hero.getHeroId()){
////			case 82:
////				composeSkill = "82,"+hero.getId()+",8201_8201_组合技";
////				break;
////			case 72:
////				composeSkill = "72,"+hero.getId()+",7201_7201_组合技";
////				break;
////			case 55:
////				composeSkill = "55,"+hero.getId()+",5501_5501_组合技";
////				break;
////			case 42:
////				composeSkill = "42,"+hero.getId()+",4201_4201_组合技";
////				break;
////			}
//			break;
//		}
//		
//		userTeamService.updateUserTeam(user.getId(), 1, teamRecord, null, 0);
//		// userTeamService.updateUserTeam(user.getId(), 2, "", "");
//		// userTeamService.updateUserTeam(user.getId(), 3, "", "");
//		// userTeamService.updateUserTeam(user.getId(), 4, "", "");
//		// userTeamService.updateUserTeam(user.getId(), 5, "", "");
//	}
	
	public void submitIcon(RequestSubmitIconCommand cmd, Builder responseBuilder, UserBean user) {
		int icon = cmd.getIcon();
		if (icon > 62000) {
			UserHeadBean userHead = userHeadService.selectUserHead(user.getId(), icon);
			if (userHead == null) {
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.HEAD_NOT_EXIST);
				
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.HEAD_NOT_EXIST);
				responseBuilder.setErrorCommand(errorCommand);
				pushCommandService.pushUserInfoCommand(responseBuilder, user);
				return;
			}
		}
		user.setIcon(icon);

		userService.cache(user.getServerId(), user.buildShort());
		userService.updateUser(user);
		
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
//		responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.SUBMIT_SUCCESS));
	}
	
	public void bindAccount(RequestBindAccountCommand cmd, Builder responseBuilder, UserBean user) {
		if (!user.getAccount().equals(cmd.getOldAccount())) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.USER_NOT_EXIST);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.USER_NOT_EXIST);
			responseBuilder.setErrorCommand(errorCommand);
//			pushCommandService.pushUserInfoCommand(responseBuilder, user);
			return;
		}
		UserBean olduser = userService.getUserByAccount(user.getServerId(), cmd.getNewAccount());
		if (olduser != null) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.USER_HAS_EXIST);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.USER_HAS_EXIST);
			responseBuilder.setErrorCommand(errorCommand);
			pushCommandService.pushUserInfoCommand(responseBuilder, user);
			return;
		}
		user.setAccount(cmd.getNewAccount());
		userService.updateUserToMysql(user);
		userService.delUserIdByAccount(user.getServerId(), cmd.getOldAccount());
		
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}
	
	public void submitGreenhand(RequestGreenhandCommand cmd, Builder responseBuilder, UserBean user) {
		if (cmd.hasGreenhand()) {
			user.setGreenhand(cmd.getGreenhand());
			
			/**
			 * send greenhand log
			 */
			logService.sendGreenhandLog(user.getServerId(), user.getId(), cmd.getGreenhand());
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
	
	public void userInfo(RequestUserInfoCommand cmd, Builder responseBuilder, UserBean user) {
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}
	
	public void extra(RequestExtraRewardCommand cmd, Builder responseBuilder, UserBean user) {
		int status = cmd.getStatus();
		if (status == 2) {
			if (System.currentTimeMillis() - user.getExtraTimeStamp() >= (25 * TimeConst.MILLION_SECOND_PER_MINUTE - 1000)) {
				List<RewardBean> rewardList = RewardBean.initRewardList(35003, 1);
				rewardService.doRewards(user, rewardList);
				pushCommandService.pushRewardCommand(responseBuilder, user, rewardList);
			}
			
			user.setExtraTimeStamp(0);
		} else if (status == 1) {
			user.setExtraTimeStamp(System.currentTimeMillis());
		}
		
		userService.updateUser(user);
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}
	
	private void pushCommand(Builder responseBuilder, UserBean user) {
		pushCommandService.pushLevelLootCommand(responseBuilder, user);
		pushCommandService.pushUserFriendListCommand(responseBuilder, user);
		pushCommandService.pushUserHeroListCommand(responseBuilder, user);
		pushCommandService.pushUserEquipListCommand(responseBuilder, user);
		pushCommandService.pushLadderRankListCommand(responseBuilder, user);
		pushCommandService.pushUserTeamListCommand(responseBuilder, user);
		pushCommandService.pushShopCommand(responseBuilder, user);
		pushCommandService.pushDailyShopCommand(responseBuilder, user);
		pushCommandService.pushBlackShopCommand(responseBuilder, user);
		pushCommandService.pushPVPShopCommand(responseBuilder, user);
		// pushCommandService.pushExpeditionShopCommand(responseBuilder, user);
		pushCommandService.pushLadderShopCommand(responseBuilder, user);
		// pushCommandService.pushUnionShopCommand(responseBuilder, user);
		// pushCommandService.pushBattletowerShopCommand(responseBuilder, user);
//		pushCommandService.pushUserMailListCommand(responseBuilder, user);
		pushCommandService.pushPurchaseCoinCommand(responseBuilder, user);
		pushCommandService.pushUserPropListCommand(responseBuilder, user);
		pushCommandService.pushPvpMapListCommand(responseBuilder, user);
//		pushCommandService.pushUserAchieveCommand(responseBuilder, user);
		pushCommandService.pushUserHeadCommand(responseBuilder, user);
		pushCommandService.pushUserPokedeList(responseBuilder, user);
		pushCommandService.pushUserFoodListCommand(responseBuilder, user);
		shopService.getLibaoShop(responseBuilder, user);
		pushCommandService.pushUserBosskillRecord(responseBuilder, user);
		pushCommandService.pushUserTalentList(responseBuilder, user);
//		noticeCommandService.pushNotices(responseBuilder, user);
		pushCommandService.pushUserEquipPokedeList(responseBuilder, user);
		pushCommandService.pushUserRaid(responseBuilder, user);
	}
	
	private void refreshUserLogin(UserBean user) {
// 		if (DateUtil.isNextDay(user.getLastLoginTime())) {
// 			//每日首次登陆
// //			VipInfo vip = userService.getVip(user.getVip());
// //			if(vip != null){
// ////				user.setPurchaseCoinLeft(user.getPurchaseCoinLeft() + vip.getDianjin());
// //			}
// 			user.setLoginDays(user.getLoginDays() + 1);
			
// 			/**
// 			 * 累计登录的活动
// 			 */
// 			activityService.loginActivity(user);
			
// 			user.setSignCount(0);
// 		}
		
		user.setLastLoginTime(DateUtil.getCurrentDate(TimeConst.DEFAULT_DATETIME_FORMAT));
		user.setSession(DigestUtils.md5Hex(user.getAccount() + System.currentTimeMillis()));
		userService.updateUser(user);
	}
}
