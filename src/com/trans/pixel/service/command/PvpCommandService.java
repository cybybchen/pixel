package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.PVPMapList;
import com.trans.pixel.protoc.Commands.PVPMine;
import com.trans.pixel.protoc.Commands.RequestAttackPVPMineCommand;
import com.trans.pixel.protoc.Commands.RequestAttackPVPMonsterCommand;
import com.trans.pixel.protoc.Commands.RequestBrotherMineInfoCommand;
import com.trans.pixel.protoc.Commands.RequestHelpAttackPVPMineCommand;
import com.trans.pixel.protoc.Commands.RequestPVPMapListCommand;
import com.trans.pixel.protoc.Commands.RequestPVPMineInfoCommand;
import com.trans.pixel.protoc.Commands.RequestRefreshPVPMineCommand;
import com.trans.pixel.protoc.Commands.RequestUnlockPVPMapCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseGetTeamCommand;
import com.trans.pixel.protoc.Commands.ResponsePVPMapListCommand;
import com.trans.pixel.protoc.Commands.Team;
import com.trans.pixel.service.MailService;
import com.trans.pixel.service.PvpMapService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.UserTeamService;

@Service
public class PvpCommandService extends BaseCommandService {
	@Resource
	private UserTeamService userTeamService;
	@Resource
	private PvpMapService pvpMapService;
	@Resource
	private UserService userService;
	@Resource
	private MailService mailService;
	@Resource
	private PushCommandService pusher;
	
	public void getMapList(RequestPVPMapListCommand cmd, Builder responseBuilder, UserBean user) {
		PVPMapList maplist = pvpMapService.getMapList(responseBuilder, user);
		ResponsePVPMapListCommand.Builder builder = ResponsePVPMapListCommand.newBuilder();
		builder.addAllField(maplist.getFieldList());
		responseBuilder.setPvpMapListCommand(builder);
	}

	public void unlockMap(RequestUnlockPVPMapCommand cmd, Builder responseBuilder, UserBean user) {
		if(pvpMapService.unlockMap(cmd.getFieldid(), cmd.getZhanli(), user))
			responseBuilder.setMessageCommand(this.buildMessageCommand(SuccessConst.UNLOCK_AREA));
		getMapList(RequestPVPMapListCommand.newBuilder().build(), responseBuilder, user);
	}
	
	public void attackMonster(RequestAttackPVPMonsterCommand cmd, Builder responseBuilder, UserBean user) {
		MultiReward rewards = pvpMapService.attackMonster(user, cmd.getPositionid(), cmd.getRet());
		if(rewards == null)
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_MONSTER));
		if(rewards.getLootCount() > 0)
			pusher.pushRewardCommand(responseBuilder, user, rewards);
		getMapList(RequestPVPMapListCommand.newBuilder().build(), responseBuilder, user);
	}
	
	public void attackMine(RequestAttackPVPMineCommand cmd, Builder responseBuilder, UserBean user) {
		int teamid = cmd.getTeamid();
		List<HeroInfoBean> heroList = userTeamService.getTeam(user, teamid);
		userTeamService.saveTeamCache(user, heroList);
		if(!pvpMapService.attackMine(user, cmd.getId(), cmd.getRet()))
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_MONSTER));
		getMapList(RequestPVPMapListCommand.newBuilder().build(), responseBuilder, user);
	}
	
	public void attackMine(RequestHelpAttackPVPMineCommand cmd, Builder responseBuilder, UserBean user) {
		int teamid = cmd.getTeamid();
		long friendUserId = cmd.getUserId();
		UserBean friend = userService.getUser(friendUserId);
		List<HeroInfoBean> heroList = userTeamService.getTeam(user, teamid);
		userTeamService.saveTeamCache(user, heroList);
		if(!pvpMapService.attackMine(friend, cmd.getId(), cmd.getRet()))
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_MONSTER));
//		getMapList(RequestPVPMapListCommand.newBuilder().build(), responseBuilder, friend);
		sendHelpMail(friend, user);
		
		responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.HELP_ATTACK_SUCCESS));
	}
	
	public void getMineInfo(RequestPVPMineInfoCommand cmd, Builder responseBuilder, UserBean user) {
		PVPMine mine = pvpMapService.getUserMine(user, cmd.getId());
		if(mine == null){
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.MAPINFO_ERROR));
			getMapList(RequestPVPMapListCommand.newBuilder().build(), responseBuilder, user);
		}else{
			Team team = null;
			if(!mine.hasOwner())
				team = Team.newBuilder().build();
			else
				team = userTeamService.getTeamCache(mine.getOwner().getId());
			ResponseGetTeamCommand.Builder builder= ResponseGetTeamCommand.newBuilder();
			builder.addAllHeroInfo(team.getHeroInfoList());
			if(team.hasUser())
				builder.setUser(team.getUser());
			responseBuilder.setTeamCommand(builder);
		}
	}

	public void getBrotherMineInfo(RequestBrotherMineInfoCommand cmd, Builder responseBuilder, UserBean user) {
		UserBean brother = userService.getUser(cmd.getBrotherId());
		PVPMine mine = pvpMapService.getUserMine(brother, cmd.getId());
		if(mine == null || !mine.hasOwner()){
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.MAPINFO_ERROR));
		}else{
			Team team = userTeamService.getTeamCache(mine.getOwner().getId());
			ResponseGetTeamCommand.Builder builder= ResponseGetTeamCommand.newBuilder();
			builder.addAllHeroInfo(team.getHeroInfoList());
			builder.setUser(team.getUser());
			responseBuilder.setTeamCommand(builder);
		}
	}
	
	public void refreshMine(RequestRefreshPVPMineCommand cmd, Builder responseBuilder, UserBean user) {
		if(user.getPvpMineLeftTime() <= 0){
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENOUGH_TIMES));
		}else{
			PVPMine mine = pvpMapService.refreshMine(user, cmd.getId());
			if(mine == null || !mine.hasOwner())
				responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENEMY));
			else{
				user.setPvpMineLeftTime(user.getPvpMineLeftTime()-1);
				userService.updateUserDailyData(user);
				Team team = userTeamService.getTeamCache(mine.getOwner().getId());
				ResponseGetTeamCommand.Builder builder= ResponseGetTeamCommand.newBuilder();
				builder.addAllHeroInfo(team.getHeroInfoList());
				builder.setUser(team.getUser());
				responseBuilder.setTeamCommand(builder);
				pusher.pushUserInfoCommand(responseBuilder, user);
			}
		}
		getMapList(RequestPVPMapListCommand.newBuilder().build(), responseBuilder, user);		
	}
	
	private void sendHelpMail(UserBean friend, UserBean user) {
		String content = "玩家" + friend.getUserName() + "帮助你赶走了矿场的敌人"; 
		MailBean mail = buildMail(user.getId(), friend.getId(), friend.getUserName(), content, MailConst.TYPE_HELP_ATTACK_PVP_MAIL);
		mailService.addMail(mail);
	}
}
