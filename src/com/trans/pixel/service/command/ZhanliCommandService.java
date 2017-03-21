package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.EquipProto.RequestSubmitZhanliCommand;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.AreaFightService;
import com.trans.pixel.service.BlackListService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.UserTeamService;
import com.trans.pixel.service.redis.RankRedisService;

@Service
public class ZhanliCommandService extends BaseCommandService {
	private static Logger log = Logger.getLogger(ZhanliCommandService.class);	
	@Resource
	private UserService userService;
	@Resource
	private RankRedisService rankRedisService;
	@Resource
	private ActivityService activityService;
	@Resource
	private UserTeamService userTeamService;
	@Resource
	private BlackListService blackService;
	@Resource
	private AreaFightService areaService;
	
	public void submitZhanli(RequestSubmitZhanliCommand cmd, Builder responseBuilder, UserBean user) {
		log.debug("00 ||" + System.currentTimeMillis());
		int zhanli = cmd.getZhanli();
		if(user.getZhanliMax() < zhanli || user.getFirstAddedtoZhanli() == 0){
			user.setFirstAddedtoZhanli(1);
//			Team team = userTeamService.getTeamCache(user);
//			log.debug("33|| " + System.currentTimeMillis());
//			if(team.getUser().getZhanli() < zhanli){
//				log.debug("submit zhanli "+zhanli +" < real "+team.getUser().getZhanli());
//				zhanli = team.getUser().getZhanli();
//				ResponseUserInfoCommand.Builder builder = ResponseUserInfoCommand.newBuilder();
//				builder.setUser(user.build());
//				responseBuilder.setUserInfoCommand(builder.build());
//				return;
//			}else
//				log.warn("zhanli update "+user.getZhanliMax() +" to "+team.getUser().getZhanli());
//
//			zhanli = team.getUser().getZhanli();
			user.setZhanli(zhanli);
			user.setZhanliMax(zhanli);
			if(!blackService.isNoranklist(user.getId())) {
				rankRedisService.updateZhanliRank(user);
				/**
				 * zhanli activity
				 */
				activityService.zhanliActivity(user, zhanli);
			}
		}
		log.debug("11||" + System.currentTimeMillis());
		areaService.unlockArea(zhanli, user);
		user.setZhanli(zhanli);
		userService.cache(user.getServerId(), user.buildShort());
		userService.updateUser(user);
		log.debug("22||" + System.currentTimeMillis());
//		responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.SUBMIT_ZHANLI_SUCCESS));
	}
}
