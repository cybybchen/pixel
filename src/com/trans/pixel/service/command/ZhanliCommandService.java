package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.RequestSubmitZhanliCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseUserInfoCommand;
import com.trans.pixel.protoc.Commands.Team;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.UserTeamService;
import com.trans.pixel.service.redis.RankRedisService;

@Service
public class ZhanliCommandService extends BaseCommandService {
	@Resource
	private UserService userService;
	@Resource
	private RankRedisService rankRedisService;
	@Resource
	private ActivityService activityService;
	@Resource
	private UserTeamService userTeamService;
	
	public void submitZhanli(RequestSubmitZhanliCommand cmd, Builder responseBuilder, UserBean user) {
		// int zhanli = cmd.getZhanli();
		// if(user.getZhanliMax() < zhanli){
		// 	Team team = userTeamService.getTeamCache(user);
		// 	if(team.getUser().getZhanli() < zhanli){
			// 	zhanli = team.getUser().getZhanli();
		// 		ResponseUserInfoCommand.Builder builder = ResponseUserInfoCommand.newBuilder();
		// 		builder.setUser(user.build());
		// 		responseBuilder.setUserInfoCommand(builder.build());
		// 		return;
		// 	}

		// user.setZhanli(zhanli);
		// 	user.setZhanliMax(zhanli);
		// 	rankRedisService.updateZhanliRank(user);
			
		// 	/**
		// 	 * zhanli activity
		// 	 */
		// 	activityService.zhanliActivity(user, zhanli);
		// }
		// user.setZhanli(zhanli);
		// userService.cache(user.getServerId(), user.buildShort());
		// userService.updateUserDailyData(user);
		
//		responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.SUBMIT_ZHANLI_SUCCESS));
	}
}
