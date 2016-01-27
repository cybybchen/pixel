package com.trans.pixel.service.command;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.RefreshConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.HeadInfo;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseUserInfoCommand;
import com.trans.pixel.service.AccountService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.utils.DateUtil;

@Service
public class LoginCommandService extends BaseCommandService {
	private static final Logger log = LoggerFactory.getLogger(LoginCommandService.class);
	
	@Resource
    private AccountService accountService;
	@Resource
    private UserService userService;
	@Resource
	private PushCommandService pushCommandService;
	
	public void login(RequestCommand request, Builder responseBuilder) {
		ResponseUserInfoCommand.Builder userInfoBuilder = ResponseUserInfoCommand.newBuilder();
		HeadInfo head = request.getHead();
		
		long userId = accountService.getUserId(head.getServerId(), head.getAccount());
		if (userId == 0) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.USER_NOT_EXIST);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		UserBean user = userService.getUser(userId);
		refreshUserLogin(user);
		super.buildUserInfo(userInfoBuilder, user);
		
		pushCommand(responseBuilder, user);
		
		responseBuilder.setUserInfoCommand(userInfoBuilder.build());
	}
	
	private void pushCommand(Builder responseBuilder, UserBean user) {
		pushCommandService.pushLootResultCommand(responseBuilder, user);
		pushCommandService.pushUserMineListCommand(responseBuilder, user);
		pushCommandService.pushUserHeroListCommand(responseBuilder, user);
		pushCommandService.pushUserEquipListCommand(responseBuilder, user);
		pushCommandService.pushUserLevelCommand(responseBuilder, user);
		pushCommandService.pushUserLootLevelCommand(responseBuilder, user);
		pushCommandService.pushLadderRankListCommand(responseBuilder, user);
	}
	
	private void refreshUserLogin(UserBean user) {
		if (isNextDay(user.getLastLoginTime())) {
			user.setRefreshLeftTimes(RefreshConst.REFRESH_PVP_TIMES);	
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
