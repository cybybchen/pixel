package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.RequestAddTeamCommand;
import com.trans.pixel.protoc.Commands.RequestGetTeamCommand;
import com.trans.pixel.protoc.Commands.RequestUpdateTeamCommand;
import com.trans.pixel.protoc.Commands.RequestUserTeamListCommand;
import com.trans.pixel.protoc.Commands.ResponseGetTeamCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.UserTeamService;

@Service
public class TeamCommandService extends BaseCommandService {
	@Resource
	private UserTeamService userTeamService;
	@Resource
	private PushCommandService pushCommandService;
	public void updateUserTeam(RequestUpdateTeamCommand cmd, Builder responseBuilder, UserBean user) {
		long userId = user.getId();
		int id = cmd.getId();
		String teamInfo = cmd.getTeamInfo();
		userTeamService.updateUserTeam(userId, id, teamInfo);
		pushCommandService.pushUserTeamListCommand(responseBuilder, user);
	}
	
	public void addUserTeam(RequestAddTeamCommand cmd, Builder responseBuilder, UserBean user) {
		long userId = user.getId();
		String teamInfo = cmd.getTeamInfo();
		userTeamService.addUserTeam(userId, teamInfo);
		pushCommandService.pushUserTeamListCommand(responseBuilder, user);
	}
	
	public void getUserTeamList(RequestUserTeamListCommand cmd, Builder responseBuilder, UserBean user) {
		pushCommandService.pushUserTeamListCommand(responseBuilder, user);
	}
	
	public void getTeamCache(RequestGetTeamCommand cmd, Builder responseBuilder, UserBean user) {
		List<HeroInfoBean> heroList = userTeamService.getTeamCache(cmd.getUserId());
		ResponseGetTeamCommand.Builder builder= ResponseGetTeamCommand.newBuilder();
		for (HeroInfoBean heroInfo : heroList) {
			builder.addHeroInfo(heroInfo.buildRankHeroInfo());
		}
		responseBuilder.setTeamCommand(builder);
	}
}
