package com.trans.pixel.service.command;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserHeadBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.HeadInfo;
import com.trans.pixel.protoc.Commands.RequestBindAccountCommand;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestRegisterCommand;
import com.trans.pixel.protoc.Commands.RequestSubmitIconCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseUserInfoCommand;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.UserHeadService;
import com.trans.pixel.service.UserHeroService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.UserTeamService;
import com.trans.pixel.utils.DateUtil;

@Service
public class UserCommandService extends BaseCommandService {
	private static final Logger logger = LoggerFactory.getLogger(UserCommandService.class);
	
	@Resource
	private UserService userService;
	@Resource
	private PushCommandService pushCommandService;
	@Resource
	private ActivityService activityService;
	@Resource
	private UserHeroService userHeroService;
	@Resource
	private UserTeamService userTeamService;
	@Resource
	private UserHeadService userHeadService;
	
	public void login(RequestCommand request, Builder responseBuilder) {
		HeadInfo head = request.getHead();
		UserBean user = userService.getUserByAccount(head.getServerId(), head.getAccount());
		if (user == null) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.USER_NOT_EXIST);
			responseBuilder.setErrorCommand(errorCommand);
			return;
		}
//		RequestLoginCommand cmd = request.getLoginCommand();
//		if(cmd.getZhanli() > user.getZhanli()){
//			user.setZhanli(cmd.getZhanli());
//			userService.updateUserDailyData(user);
//		}
		refreshUserLogin(user);
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
		UserBean user = new UserBean();
		user.init(head.getServerId(), head.getAccount(), userService.handleUserName(head.getServerId(), registerCommand.getUserName()), registerCommand.getIcon());
		user.setRegisterTime(DateUtil.getCurrentDate(TimeConst.DEFAULT_DATETIME_FORMAT));
		try{
			userService.addNewUser(user);
		}catch(Exception e){
			logger.error(e.getMessage());
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.ACCOUNT_HAS_REGISTER);
			responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		refreshUserLogin(user);
		userService.cache(user.getServerId(), user.buildShort());
		userInfoBuilder.setUser(user.build());
		responseBuilder.setUserInfoCommand(userInfoBuilder.build());

		addRegisterHero(user);
		addRegisterTeam(user);
		
		pushCommand(responseBuilder, user);
	}
	
	public void submitIcon(RequestSubmitIconCommand cmd, Builder responseBuilder, UserBean user) {
		int icon = cmd.getIcon();
		if (icon > 62000) {
			UserHeadBean userHead = userHeadService.selectUserHead(user.getId(), icon);
			if (userHead == null) {
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.HEAD_NOT_EXIST);
				responseBuilder.setErrorCommand(errorCommand);
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
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.USER_NOT_EXIST);
			responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		
		user.setAccount(cmd.getNewAccount());
		userService.updateUser(user);
		
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}
	
	private void addRegisterHero(UserBean user) {
		userHeroService.addUserHero(user, 42, 1, 1);
		userHeroService.selectUserNewHero(user.getId());
	}
	
	private void addRegisterTeam(UserBean user) {
		List<HeroInfoBean> userHeroList = userHeroService.selectUserHeroList(user.getId());
		String teamRecord = "";
		for (HeroInfoBean hero : userHeroList) {
			teamRecord += hero.getHeroId() + "," + hero.getId() + "|";
			break;
		}
		
		if (!teamRecord.isEmpty())
			userTeamService.addUserTeam(user.getId(), teamRecord, "");
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
		pushCommandService.pushUserMailListCommand(responseBuilder, user);
		pushCommandService.pushPurchaseCoinCommand(responseBuilder, user);
		pushCommandService.pushUserPropListCommand(responseBuilder, user);
		pushCommandService.pushPvpMapListCommand(responseBuilder, user);
		pushCommandService.pushUserAchieveCommand(responseBuilder, user);
		pushCommandService.pushUserHeadCommand(responseBuilder, user);
	}
	
	private void refreshUserLogin(UserBean user) {
		if (isNextDay(user.getLastLoginTime())) {
			//每日首次登陆
//			VipInfo vip = userService.getVip(user.getVip());
//			if(vip != null){
////				user.setPurchaseCoinLeft(user.getPurchaseCoinLeft() + vip.getDianjin());
//			}
			user.setLoginDays(user.getLoginDays() + 1);
			
			/**
			 * 累计登录的活动
			 */
			activityService.loginActivity(user);
			
			if (isNextWeek(user.getLastLoginTime())) {
				user.setSignCount(0);
			}
		}
		
		user.setLastLoginTime(DateUtil.getCurrentDate(TimeConst.DEFAULT_DATETIME_FORMAT));
		user.setSession(DigestUtils.md5Hex(user.getAccount() + System.currentTimeMillis()));
		userService.updateUser(user);
	}
	
	private boolean isNextDay(String lastLoginTime) {
		SimpleDateFormat df = new SimpleDateFormat(TimeConst.DEFAULT_DATE_FORMAT);
		boolean nextDay = false;
		if (lastLoginTime == null || lastLoginTime.trim().equals("")) {
			return true;
		}
		try {
			String now = df.format(new Date());
			String last = df.format(df.parse(lastLoginTime));
			if (now.equals(last)) {
				nextDay = false;
			} else {
				nextDay = true;
			}
		} catch (ParseException e) {
			nextDay = false;
		}
		
		return nextDay;
	}
	
	private boolean isNextWeek(String lastLoginTime) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		int now = c.get(Calendar.WEEK_OF_YEAR);
		logger.debug("now is:" + now);
		
		SimpleDateFormat df = new SimpleDateFormat(TimeConst.DEFAULT_DATE_FORMAT);
		try {
			c.setTime(df.parse(lastLoginTime));
		} catch (ParseException e) {
			return false;
		}
		int last = c.get(Calendar.WEEK_OF_YEAR);
		
		if (now != last)
			return true;
		
		return false;
	}
}
