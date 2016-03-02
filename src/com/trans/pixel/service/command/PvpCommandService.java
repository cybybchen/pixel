package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.PVPMapList;
import com.trans.pixel.protoc.Commands.PVPMine;
import com.trans.pixel.protoc.Commands.RequestAttackPVPBossCommand;
import com.trans.pixel.protoc.Commands.RequestAttackPVPMineCommand;
import com.trans.pixel.protoc.Commands.RequestAttackPVPMonsterCommand;
import com.trans.pixel.protoc.Commands.RequestPVPMapListCommand;
import com.trans.pixel.protoc.Commands.RequestPVPMineInfoCommand;
import com.trans.pixel.protoc.Commands.RequestRefreshPVPMineCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseGetTeamCommand;
import com.trans.pixel.protoc.Commands.ResponsePVPMapListCommand;
import com.trans.pixel.service.PvpMapService;
import com.trans.pixel.service.UserTeamService;

@Service
public class PvpCommandService extends BaseCommandService {
	@Resource
	private UserTeamService userTeamService;
	@Resource
	private PvpMapService pvpMapService;
	
//	public void attackRelatedUser(RequestAttackRelativeCommand cmd, Builder responseBuilder, UserBean user) {	
//		ResponseAttackRelativeCommand.Builder builder = ResponseAttackRelativeCommand.newBuilder();
//		long userId = user.getId();
//		int mapId = cmd.getMapId();
//		int mineId = cmd.getMineId();
//		UserMineBean userMine = pvpMapService.attackRelativeUser(userId, mapId, mineId);
//		if (userMine == null) {
//			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.PVP_MAP_ERROR);
//            responseBuilder.setErrorCommand(errorCommand);
//			return;
//		}
//		
//		builder.setMapId(userMine.getMapId());
//		builder.setMineId(userMine.getMineId());
//		builder.setPreventTime(userMine.getPreventTime());
//		builder.setLevel(userMine.getLevel());
//		
//		responseBuilder.setAttackRelativeCommand(builder);
//	}
//	
//	public void refreshRelatedUser(RequestRefreshRelatedUserCommand cmd, Builder responseBuilder, UserBean user) {	
//		ResponseRefreshRelatedUserCommand.Builder builder = ResponseRefreshRelatedUserCommand.newBuilder();
//		long refreshUserId = pvpMapService.refreshRelatedUser(user, cmd.getMapId(), cmd.getMineId());
//		builder.setLeftTimes(user.getRefreshLeftTimes());
//		builder.setRelatedUserId(refreshUserId);
//		
//		responseBuilder.setRefreshRelatedUserCommand(builder.build());
//	}
	
	public void getMapList(RequestPVPMapListCommand cmd, Builder responseBuilder, UserBean user) {
		PVPMapList maplist = pvpMapService.getMapList(user);
		ResponsePVPMapListCommand.Builder builder = ResponsePVPMapListCommand.newBuilder();
		builder.addAllField(maplist.getFieldList());
		responseBuilder.setPvpMapListCommand(builder);
	}
	
	public void attackMonster(RequestAttackPVPMonsterCommand cmd, Builder responseBuilder, UserBean user) {
		if(!pvpMapService.attackMonster(user, cmd.getPositionid(), cmd.getRet()))
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_MONSTER));
		getMapList(RequestPVPMapListCommand.newBuilder().build(), responseBuilder, user);
	}
	
	public void attackBoss(RequestAttackPVPBossCommand cmd, Builder responseBuilder, UserBean user) {
		if(!pvpMapService.attackBoss(user, cmd.getPositionid(), cmd.getRet()))
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_MONSTER));
		getMapList(RequestPVPMapListCommand.newBuilder().build(), responseBuilder, user);
	}
	
	public void attackMine(RequestAttackPVPMineCommand cmd, Builder responseBuilder, UserBean user) {
		if(!pvpMapService.attackMine(user, cmd.getId(), cmd.getRet()))
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_MONSTER));
		getMapList(RequestPVPMapListCommand.newBuilder().build(), responseBuilder, user);
	}
	
	public void getMineInfo(RequestPVPMineInfoCommand cmd, Builder responseBuilder, UserBean user) {
		PVPMine mine = pvpMapService.getUserMine(user, cmd.getId());
		if(mine == null || !mine.hasOwner()){
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.MAPINFO_ERROR));
			getMapList(RequestPVPMapListCommand.newBuilder().build(), responseBuilder, user);
		}else{
			List<HeroInfoBean> heroList = userTeamService.getTeamCache(mine.getOwner().getId());
			ResponseGetTeamCommand.Builder builder= ResponseGetTeamCommand.newBuilder();
			for (HeroInfoBean heroInfo : heroList) {
				builder.addHeroInfo(heroInfo.buildRankHeroInfo());
			}
			responseBuilder.setTeamCommand(builder);
		}
	}

	public void refreshMine(RequestRefreshPVPMineCommand cmd, Builder responseBuilder, UserBean user) {
		if(!pvpMapService.refreshMine(user, cmd.getId()))
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.NOT_ENOUGH_JEWEL));
		getMapList(RequestPVPMapListCommand.newBuilder().build(), responseBuilder, user);		
	}
}
