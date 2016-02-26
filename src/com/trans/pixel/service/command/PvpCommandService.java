package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserMineBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.RequestAttackRelativeCommand;
import com.trans.pixel.protoc.Commands.RequestGetUserMineCommand;
import com.trans.pixel.protoc.Commands.RequestRefreshRelatedUserCommand;
import com.trans.pixel.protoc.Commands.ResponseAttackRelativeCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseRefreshRelatedUserCommand;
import com.trans.pixel.service.PvpMapService;

@Service
public class PvpCommandService extends BaseCommandService {

	@Resource
	private PvpMapService pvpMapService;
	
	public void attackRelatedUser(RequestAttackRelativeCommand cmd, Builder responseBuilder, UserBean user) {	
		ResponseAttackRelativeCommand.Builder builder = ResponseAttackRelativeCommand.newBuilder();
		long userId = user.getId();
		int mapId = cmd.getMapId();
		int mineId = cmd.getMineId();
		UserMineBean userMine = pvpMapService.attackRelativeUser(userId, mapId, mineId);
		if (userMine == null) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.PVP_MAP_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		
		builder.setMapId(userMine.getMapId());
		builder.setMineId(userMine.getMineId());
		builder.setPreventTime(userMine.getPreventTime());
		builder.setLevel(userMine.getLevel());
		
		responseBuilder.setAttackRelativeCommand(builder);
	}
	
	public void refreshRelatedUser(RequestRefreshRelatedUserCommand cmd, Builder responseBuilder, UserBean user) {	
		ResponseRefreshRelatedUserCommand.Builder builder = ResponseRefreshRelatedUserCommand.newBuilder();
		long refreshUserId = pvpMapService.refreshRelatedUser(user, cmd.getMapId(), cmd.getMineId());
		builder.setLeftTimes(user.getRefreshLeftTimes());
		builder.setRelatedUserId(refreshUserId);
		
		responseBuilder.setRefreshRelatedUserCommand(builder.build());
	}
	
	public void getUserMine(RequestGetUserMineCommand cmd, Builder responseBuilder, UserBean user) {
		List<UserMineBean> userMineList = pvpMapService.relateUser(user);
		responseBuilder.setGetUserMineCommand(super.buildGetUserMineCommand(userMineList));
	}
}
