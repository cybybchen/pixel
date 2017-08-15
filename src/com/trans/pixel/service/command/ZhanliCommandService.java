package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.BlackListBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.EquipProto.RequestSubmitZhanliCommand;
import com.trans.pixel.protoc.UserInfoProto.Merlevel;
import com.trans.pixel.protoc.UserInfoProto.MerlevelList;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.AreaFightService;
import com.trans.pixel.service.BlackListService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.UserTeamService;
import com.trans.pixel.service.redis.RankRedisService;
import com.trans.pixel.service.redis.ZhanliRedisService;

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
	@Resource
	private ZhanliRedisService redis;
	@Resource
	private PushCommandService pusher;
	
	public void submitZhanli(RequestSubmitZhanliCommand cmd, Builder responseBuilder, UserBean user) {
		int zhanli = cmd.getZhanli();
		if(user.getZhanliMax() < zhanli/* || user.getFirstAddedtoZhanli() == 0*/){
//			user.setFirstAddedtoZhanli(1);
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
			if(zhanli - user.getZhanliMax() > 25000) {
				BlackListBean blacklist = blackService.getBlackList(user.getId());
				if(blacklist == null) {
					blacklist = new BlackListBean();
					blacklist.setUserId(user.getId());
					blacklist.setUserName(user.getUserName());
					blacklist.setServerId(user.getServerId());
					blacklist.setAccount(user.getAccount());
					blacklist.setIdfa(user.getIdfa());
					blacklist.setNotalk(true);
					blacklist.setNoranklist(true);
					blacklist.setNologin(false);
					blacklist.setNoaccount(false);
					blacklist.setNoidfa(false);
					blacklist.setNoip(false);
					blackService.updateBlackList(blacklist);
					userService.sendMail(user.getUserName()+"(id:"+user.getId()+"),由于提升战力过大"+user.getZhanliMax()+"->"+zhanli+"被封禁排行");
				}else if(!blacklist.isNotalk() || !blacklist.isNoranklist()) {
					blacklist.setNotalk(true);
					blacklist.setNoranklist(true);
					blackService.updateBlackList(blacklist);
					userService.sendMail(user.getUserName()+"(id:"+user.getId()+"),由于提升战力过大"+user.getZhanliMax()+"->"+zhanli+"被封禁排行");
				}
			}
			user.setZhanli(zhanli);
			user.setZhanliMax(zhanli);
			
			MerlevelList.Builder list = redis.getMerlevel();
			for(Merlevel level : list.getLevelList()){
				if(user.getZhanliMax() >= level.getScore() && user.getMerlevel() < level.getLevel()) {
					user.setMerlevel(level.getLevel());
					activityService.merLevel(user, user.getMerlevel());
				}
			}
			if(!blackService.isNoranklist(user.getId()) && zhanli > 1000) {
				rankRedisService.updateZhanliRank(user);
				/**
				 * zhanli activity
				 */
				activityService.zhanliActivity(user, zhanli);
			}
		}
//		areaService.unlockArea(zhanli, user);
		user.setZhanli(zhanli);
		userService.cache(user.getServerId(), user.buildShort());
		userService.updateUser(user);
//		responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.SUBMIT_ZHANLI_SUCCESS));
		pusher.pushUserInfoCommand(responseBuilder, user);
	}
}
