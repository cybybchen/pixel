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
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserHeadBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.HeadInfo;
import com.trans.pixel.protoc.Commands.RequestBindAccountCommand;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestGreenhandCommand;
import com.trans.pixel.protoc.Commands.RequestLoginCommand;
import com.trans.pixel.protoc.Commands.RequestRegisterCommand;
import com.trans.pixel.protoc.Commands.RequestSubmitIconCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseUserInfoCommand;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.BlackService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.ServerService;
import com.trans.pixel.service.ShopService;
import com.trans.pixel.service.UserHeadService;
import com.trans.pixel.service.UserHeroService;
import com.trans.pixel.service.UserService;
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
	private BlackService blackService;
	@Resource
	private ShopService shopService;
	@Resource
	private LogService logService;
	@Resource
	private ServerService serverService;
	
	
	public void login(RequestCommand request, Builder responseBuilder) {
		HeadInfo head = request.getHead();
		if (blackService.isBlackAccount(head.getAccount())) {
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
		if (blackService.isBlackUser(user)) {
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
		ResponseUserInfoCommand.Builder userInfoBuilder = ResponseUserInfoCommand.newBuilder();
		userInfoBuilder.setUser(user.build());
		responseBuilder.setUserInfoCommand(userInfoBuilder.build());
	}

	public void register(RequestCommand request, Builder responseBuilder) {
		RequestRegisterCommand registerCommand = request.getRegisterCommand();
		ResponseUserInfoCommand.Builder userInfoBuilder = ResponseUserInfoCommand.newBuilder();
		HeadInfo head = request.getHead();
		if (blackService.isBlackAccount(head.getAccount())) {
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
			addRegisterHero(user, addHeroId);
			addRegisterTeam(user);
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
		userInfoBuilder.setUser(user.build());
		responseBuilder.setUserInfoCommand(userInfoBuilder.build());
		
		pushCommand(responseBuilder, user);
	}
	
	private void addRegisterHero(UserBean user, int heroId) {
		userHeroService.addUserHero(user, heroId, 1, 1);
		userHeroService.selectUserNewHero(user.getId());
	}
	
	private void addRegisterTeam(UserBean user) {
		List<HeroInfoBean> userHeroList = userHeroService.selectUserHeroList(user.getId());
		String teamRecord = "";
		String composeSkill = "";
		for (HeroInfoBean hero : userHeroList) {
			teamRecord += hero.getHeroId() + "," + hero.getId() + "|";
			int skillid = hero.getSkillInfoList().get(0).getSkillId();
			composeSkill = hero.getHeroId()+","+hero.getId()+","+skillid+"_"+skillid+"_组合技";
//			switch(hero.getHeroId()){
//			case 82:
//				composeSkill = "82,"+hero.getId()+",8201_8201_组合技";
//				break;
//			case 72:
//				composeSkill = "72,"+hero.getId()+",7201_7201_组合技";
//				break;
//			case 55:
//				composeSkill = "55,"+hero.getId()+",5501_5501_组合技";
//				break;
//			case 42:
//				composeSkill = "42,"+hero.getId()+",4201_4201_组合技";
//				break;
//			}
			break;
		}
		
		userTeamService.updateUserTeam(user.getId(), 1, teamRecord, composeSkill);
		// userTeamService.updateUserTeam(user.getId(), 2, "", "");
		// userTeamService.updateUserTeam(user.getId(), 3, "", "");
		// userTeamService.updateUserTeam(user.getId(), 4, "", "");
		// userTeamService.updateUserTeam(user.getId(), 5, "", "");
	}
	
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
			pushCommandService.pushUserInfoCommand(responseBuilder, user);
			return;
		}
		
		user.setAccount(cmd.getNewAccount());
		userService.updateUser(user);
		
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
	
	private void pushCommand(Builder responseBuilder, UserBean user) {
		pushCommandService.pushLootResultCommand(responseBuilder, user);
		pushCommandService.pushUserFriendListCommand(responseBuilder, user);
		pushCommandService.pushUserHeroListCommand(responseBuilder, user);
		pushCommandService.pushUserEquipListCommand(responseBuilder, user);
		pushCommandService.pushUserLevelCommand(responseBuilder, user);
		pushCommandService.pushUserLootLevelCommand(responseBuilder, user);
		pushCommandService.pushLadderRankListCommand(responseBuilder, user);
		pushCommandService.pushUserTeamListCommand(responseBuilder, user);
		pushCommandService.pushShopCommand(responseBuilder, user);
		pushCommandService.pushDailyShopCommand(responseBuilder, user);
		pushCommandService.pushBlackShopCommand(responseBuilder, user);
		pushCommandService.pushPVPShopCommand(responseBuilder, user);
		pushCommandService.pushExpeditionShopCommand(responseBuilder, user);
		pushCommandService.pushLadderShopCommand(responseBuilder, user);
		pushCommandService.pushUnionShopCommand(responseBuilder, user);
//		pushCommandService.pushUserMailListCommand(responseBuilder, user);
		pushCommandService.pushPurchaseCoinCommand(responseBuilder, user);
		pushCommandService.pushUserPropListCommand(responseBuilder, user);
		pushCommandService.pushPvpMapListCommand(responseBuilder, user);
//		pushCommandService.pushUserAchieveCommand(responseBuilder, user);
		pushCommandService.pushUserHeadCommand(responseBuilder, user);
		pushCommandService.pushUserPokedeList(responseBuilder, user);
		shopService.getLibaoShop(responseBuilder, user);
		pushCommandService.pushUserBosskillRecord(responseBuilder, user);
		
		noticeCommandService.pushNotices(responseBuilder, user);
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
