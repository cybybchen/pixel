package com.trans.pixel.service.command;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.AchieveConst;
import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.HeadInfo;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestLoginCommand;
import com.trans.pixel.protoc.Commands.RequestRegisterCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseUserInfoCommand;
import com.trans.pixel.protoc.Commands.VipInfo;
import com.trans.pixel.service.AchieveService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.utils.DateUtil;

@Service
public class UserCommandService extends BaseCommandService {
	private static final Logger logger = LoggerFactory.getLogger(UserCommandService.class);
	
//	@Resource
//    private AccountService accountService;
	@Resource
    private UserService userService;
	@Resource
	private PushCommandService pushCommandService;
	@Resource
	private AchieveService achieveService;
	
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
		ResponseUserInfoCommand.Builder userInfoBuilder = ResponseUserInfoCommand.newBuilder();
		userInfoBuilder.setUser(user.build());
		responseBuilder.setUserInfoCommand(userInfoBuilder.build());

		pushCommand(responseBuilder, user);
	}

	public void register(RequestCommand request, Builder responseBuilder) {
		RequestRegisterCommand registerCommand = request.getRegisterCommand();
		ResponseUserInfoCommand.Builder userInfoBuilder = ResponseUserInfoCommand.newBuilder();
		HeadInfo head = request.getHead();
		UserBean user = new UserBean();
		user.init(head.getServerId(), head.getAccount(), registerCommand.getUserName());
		try{
			userService.addNewUser(user);
		}catch(Exception e){
			logger.error(e.getMessage());
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.ACCOUNT_HAS_REGISTER);
			responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		userInfoBuilder.setUser(user.build());
		responseBuilder.setUserInfoCommand(userInfoBuilder.build());

		pushCommand(responseBuilder, user);
	}
	
	private void pushCommand(Builder responseBuilder, UserBean user) {
		pushCommandService.pushLootResultCommand(responseBuilder, user);
//		pushCommandService.pushUserMineListCommand(responseBuilder, user);
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
	}
	
	private void refreshUserLogin(UserBean user) {
		if (isNextDay(user.getLastLoginTime())) {
			//每日首次登陆
			VipInfo vip = userService.getVip(user.getVip());
			if(vip != null){
//				user.setPurchaseCoinLeft(user.getPurchaseCoinLeft() + vip.getDianjin());
			}
			user.setLoginDays(user.getLoginDays() + 1);
			user.setHasSign(false);
			
			/**achieve
			 * 
			 */
			achieveService.sendAchieveScore(user.getId(), AchieveConst.TYPE_LOGIN);
		}
		
		user.setLastLoginTime(DateUtil.getCurrentDate(TimeConst.DEFAULT_DATETIME_FORMAT));
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
}
