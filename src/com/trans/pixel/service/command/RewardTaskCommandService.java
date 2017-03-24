package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserPropBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.RewardTaskProto.RequestCreateRewardTaskRoomCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestInviteToRewardTaskRoomCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestQuitRewardTaskRoomCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestRewardTaskRewardCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestSubmitRewardTaskScoreCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestUserRewardTaskCommand;
import com.trans.pixel.protoc.RewardTaskProto.ResponseUserRewardTaskCommand;
import com.trans.pixel.protoc.RewardTaskProto.ResponseUserRewardTaskRoomCommand;
import com.trans.pixel.protoc.RewardTaskProto.UserRewardTask;
import com.trans.pixel.protoc.RewardTaskProto.UserRewardTask.REWARDTASK_STATUS;
import com.trans.pixel.protoc.RewardTaskProto.UserRewardTaskRoom;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.RewardTaskService;
import com.trans.pixel.service.UserRewardTaskService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class RewardTaskCommandService extends BaseCommandService {

	@Resource
	private RewardTaskService rewardTaskService;
	@Resource
	private PushCommandService pusher;
	@Resource
	private RewardService rewardService;
	@Resource
	private LogService logService;
	@Resource
	private UserRewardTaskService userRewardTaskService;
	
	public void submitScore(RequestSubmitRewardTaskScoreCommand cmd, Builder responseBuilder, UserBean user) {
		MultiReward.Builder rewards = MultiReward.newBuilder();
		UserInfo.Builder errorUser = UserInfo.newBuilder();
		List<UserPropBean> userPropList = new ArrayList<UserPropBean>();
		ResultConst ret = rewardTaskService.submitRewardTaskScore(user, cmd.getId(), cmd.getRet(), rewards, errorUser, userPropList);
		if (ret instanceof ErrorConst) {
			if (ret.getCode() == ErrorConst.NOT_ENOUGH_PROP.getCode()) {
				pusher.pushUserInfoCommand(responseBuilder, errorUser.build());
			}
			
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ret);
			ErrorCommand errorCommand = buildErrorCommand(ret);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		rewardService.doRewards(user, rewards.build());
		pusher.pushRewardCommand(responseBuilder, user, rewards.build());


		pusher.pushUserRewardTask(responseBuilder, user);
		
		pusher.pushUserPropListCommand(responseBuilder, user, userPropList);
	}
	
	public void createRoom(RequestCreateRewardTaskRoomCommand cmd, Builder responseBuilder, UserBean user) {
		UserRewardTaskRoom room = rewardTaskService.createRoom(user, cmd.getId());
		if (room == null) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.BOSS_ROOM_CREATE_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BOSS_ROOM_CREATE_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		ResponseUserRewardTaskRoomCommand.Builder roombuilder = ResponseUserRewardTaskRoomCommand.newBuilder();
		roombuilder.addRoom(room);
		responseBuilder.setUserRewardTaskRoomCommand(roombuilder.build());
	}
	
	public void quitRoom(RequestQuitRewardTaskRoomCommand cmd, Builder responseBuilder, UserBean user) {
		UserRewardTask.Builder rewardTaskBuilder = UserRewardTask.newBuilder();
		UserRewardTaskRoom.Builder roomBuilder = UserRewardTaskRoom.newBuilder();
		ResultConst ret = rewardTaskService.quitRoom(user, cmd.getUserId(), cmd.getId(), rewardTaskBuilder, roomBuilder);
		if (ret instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ret);
			ErrorCommand errorCommand = buildErrorCommand(ret);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		if (roomBuilder.getCreateUserId() == user.getId() && roomBuilder.getUserCount() > 0) {
			ResponseUserRewardTaskRoomCommand.Builder room = ResponseUserRewardTaskRoomCommand.newBuilder();
			room.addRoom(roomBuilder.build());
			responseBuilder.setUserRewardTaskRoomCommand(room.build());
		}
		
		ResponseUserRewardTaskCommand.Builder builder = ResponseUserRewardTaskCommand.newBuilder();
		builder.addUserRewardTask(rewardTaskBuilder.build());
		responseBuilder.setUserRewardTaskCommand(builder.build());
	}
	
	public void inviteRoom(RequestInviteToRewardTaskRoomCommand cmd, Builder responseBuilder, UserBean user) {
		List<Long> userIds = cmd.getUserIdList();
		int id = cmd.getId();
		long createUserId = cmd.getCreateUserId();
		UserRewardTaskRoom room = rewardTaskService.getUserRoom(user, id);
		if(room == null){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.ROOM_NEED_CREATE_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.ROOM_NEED_CREATE_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		room = rewardTaskService.inviteFightRewardTask(user, createUserId, userIds, id);
		
		if (room == null) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.BOSS_ROOM_IS_NOT);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BOSS_ROOM_IS_NOT);
            responseBuilder.setErrorCommand(errorCommand);
            
            return;
		} 
		
		if (room.hasStatus() && room.getStatus() == REWARDTASK_STATUS.FULL_VALUE) { // 房间人数已满
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.BOSS_ROOM_IS_FULL_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BOSS_ROOM_IS_FULL_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            
            return;
		}
		
		ResponseUserRewardTaskRoomCommand.Builder builder = ResponseUserRewardTaskRoomCommand.newBuilder();
		builder.addRoom(room);
		responseBuilder.setUserRewardTaskRoomCommand(builder.build());
		
		if (userIds.isEmpty()) {
			ResponseUserRewardTaskCommand.Builder userRewardTaskBuilder = ResponseUserRewardTaskCommand.newBuilder();
			userRewardTaskBuilder.addUserRewardTask(userRewardTaskService.getUserRewardTask(user, id));
			responseBuilder.setUserRewardTaskCommand(userRewardTaskBuilder.build());
		}
	}
	
	public void getUserRewardTask(RequestUserRewardTaskCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseUserRewardTaskCommand.Builder builder = ResponseUserRewardTaskCommand.newBuilder();
		List<UserRewardTask> list = userRewardTaskService.getUserRewardTaskList(user);
		builder.addAllUserRewardTask(list);
		responseBuilder.setUserRewardTaskCommand(builder.build());
	}
	
	public void getUserRewardTaskReward(RequestRewardTaskRewardCommand cmd, Builder responseBuilder, UserBean user) {
		List<RewardBean> rewardList = rewardTaskService.getRewardList(user, cmd.getId());
		if (rewardList != null && rewardList.size() > 0) {
			rewardService.doRewards(user, rewardList);
			pusher.pushRewardCommand(responseBuilder, user, rewardList);
		} else {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.BOSS_IS_NOT_EXIST_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BOSS_IS_NOT_EXIST_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
		}
	}
}
