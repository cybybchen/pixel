package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.BossGroupRecord;
import com.trans.pixel.protoc.Commands.BossRoomRecord;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.RequestCreateBossRoomCommand;
import com.trans.pixel.protoc.Commands.RequestInviteFightBossCommand;
import com.trans.pixel.protoc.Commands.RequestQuitFightBossCommand;
import com.trans.pixel.protoc.Commands.RequestStartBossRoomCommand;
import com.trans.pixel.protoc.Commands.RequestSubmitBossRoomScoreCommand;
import com.trans.pixel.protoc.Commands.RequestSubmitBosskillCommand;
import com.trans.pixel.protoc.Commands.ResponseBossRoomRecordCommand;
import com.trans.pixel.protoc.Commands.ResponseBosskillCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.BossService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class BossCommandService extends BaseCommandService {

	@Resource
	private BossService bossService;
	@Resource
	private PushCommandService pusher;
	@Resource
	private RewardService rewardService;
	@Resource
	private LogService logService;
	
	public void bossKill(RequestSubmitBosskillCommand cmd, Builder responseBuilder, UserBean user) {
		List<RewardBean> rewardList = bossService.submitBosskill(user, cmd.getGroupId(), cmd.getBossId());
		if (rewardList != null && rewardList.size() > 0) {
			rewardService.doRewards(user, rewardList);
			pusher.pushRewardCommand(responseBuilder, user, rewardList);
		}
		pusher.pushUserBosskillRecord(responseBuilder, user);
	}
	
	public void getBosskillRecord(Builder responseBuilder, UserBean user) {
		ResponseBosskillCommand.Builder builder = ResponseBosskillCommand.newBuilder();
		List<BossGroupRecord> list = bossService.getBossGroupRecord(user);
		builder.addAllRecord(list);
		responseBuilder.setBosskillCommand(builder.build());
		
		BossRoomRecord record = bossService.getBossRoomRecord(user);
		if (record != null) {
			ResponseBossRoomRecordCommand.Builder roombuilder = ResponseBossRoomRecordCommand.newBuilder();
			roombuilder.setBossRoom(record);
			responseBuilder.setBossRoomRecordCommand(roombuilder.build());
		}
	}
	
	public void createFightBossRoom(RequestInviteFightBossCommand cmd, Builder responseBuilder, UserBean user) {
		List<Long> userIds = cmd.getUserIdList();
		int groupId = cmd.getGroupId();
		int bossId = cmd.getBossId();
		long createUserId = cmd.getCreateUserId();
		String startDate = cmd.hasStartDate() ? cmd.getStartDate() : "";
		BossRoomRecord bossRoomRecord = bossService.inviteFightBoss(user, createUserId, userIds, groupId, bossId, startDate);
		
		pusher.pushUserInfoCommand(responseBuilder, user);
		if (bossRoomRecord == null) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.BOSS_ROOM_IS_NOT);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BOSS_ROOM_IS_NOT);
            responseBuilder.setErrorCommand(errorCommand);
            
            return;
		}
		
		ResponseBossRoomRecordCommand.Builder builder = ResponseBossRoomRecordCommand.newBuilder();
		builder.setBossRoom(bossRoomRecord);
		responseBuilder.setBossRoomRecordCommand(builder.build());
	}
	
	public void quitFightBossRoom(RequestQuitFightBossCommand cmd, Builder responseBuilder, UserBean user) {
		BossRoomRecord bossRecord = bossService.getBossRoomRecord(user);
		if(bossRecord.getCreateUserId() == user.getId() && cmd.getUserId() == user.getId() && bossRecord.getUserCount() > 1){
			ResponseBossRoomRecordCommand.Builder roombuilder = ResponseBossRoomRecordCommand.newBuilder();
			roombuilder.setBossRoom(bossRecord);
			responseBuilder.setBossRoomRecordCommand(roombuilder.build());
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.BOSS_ROOM_FULL);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BOSS_ROOM_FULL);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		bossRecord = bossService.quitBossRoom(user, bossRecord, cmd.getUserId());
		if (bossRecord != null) {
			if(bossRecord.getUserCount() == 0)
				return;
			ResponseBossRoomRecordCommand.Builder roombuilder = ResponseBossRoomRecordCommand.newBuilder();
			roombuilder.setBossRoom(bossRecord);
			responseBuilder.setBossRoomRecordCommand(roombuilder.build());
		}else{
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.BOSS_ROOM_IS_NOT);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BOSS_ROOM_IS_NOT);
            responseBuilder.setErrorCommand(errorCommand);
		}
//		pusher.pushUserInfoCommand(responseBuilder, user);
	}
	
	public void submitBossRoomScore(RequestSubmitBossRoomScoreCommand cmd, Builder responseBuilder, UserBean user) {
		List<RewardBean> rewardList = new ArrayList<RewardBean>();
		BossRoomRecord bossRoomRecord = bossService.submitBossRoomScore(user, cmd.getPercent(), rewardList);
		if (bossRoomRecord == null) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.SUBMIT_BOSS_SCORE_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.SUBMIT_BOSS_SCORE_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		ResponseBossRoomRecordCommand.Builder roombuilder = ResponseBossRoomRecordCommand.newBuilder();
		roombuilder.setBossRoom(bossRoomRecord);
		responseBuilder.setBossRoomRecordCommand(roombuilder.build());
		if (!rewardList.isEmpty()) {
			rewardService.doRewards(user, rewardList);
			pusher.pushRewardCommand(responseBuilder, user, rewardList);
		}
	}
	
	public void startBossRoom(RequestStartBossRoomCommand cmd, Builder responseBuilder, UserBean user) {
		BossRoomRecord bossRoomRecord = bossService.startBossRoom(user);
		
		ResponseBossRoomRecordCommand.Builder roombuilder = ResponseBossRoomRecordCommand.newBuilder();
		roombuilder.setBossRoom(bossRoomRecord);
		responseBuilder.setBossRoomRecordCommand(roombuilder.build());
	}
	
	public void createBossRoom(RequestCreateBossRoomCommand cmd, Builder responseBuilder, UserBean user) {
		BossRoomRecord bossRoomRecord = bossService.createBossRoom(user, cmd.getGroupId(), cmd.getBossId());
		if (bossRoomRecord == null) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.BOSS_ROOM_START);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BOSS_ROOM_START);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		ResponseBossRoomRecordCommand.Builder roombuilder = ResponseBossRoomRecordCommand.newBuilder();
		roombuilder.setBossRoom(bossRoomRecord);
		responseBuilder.setBossRoomRecordCommand(roombuilder.build());
	}
}
