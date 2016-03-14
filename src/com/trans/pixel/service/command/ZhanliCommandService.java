package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.RequestSubmitZhanliCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.redis.RankRedisService;

@Service
public class ZhanliCommandService extends BaseCommandService {
	@Resource
	private UserService userService;
	@Resource
	private RankRedisService rankRedisService;
	@Resource
	private ActivityService activityService;
	
	public void submitZhanli(RequestSubmitZhanliCommand cmd, Builder responseBuilder, UserBean user) {
		int zhanli = cmd.getZhanli();
		user.setZhanli(zhanli);
		if(user.getZhanliMax() < zhanli){
			user.setZhanliMax(zhanli);
			rankRedisService.updateZhanliRank(user);
			
			/**
			 * zhanli activity
			 */
			activityService.zhanliActivity(user, zhanli);
		}
		userService.cache(user.getServerId(), user.buildShort());
		userService.updateUserDailyData(user);
		
//		responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.SUBMIT_ZHANLI_SUCCESS));
	}
}
