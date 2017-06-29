package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.RewardTaskProto.RequestChangePositionCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestCreateRewardTaskRoomCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestGiveupRewardTaskCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestInviteToRewardTaskRoomCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestQuitRewardTaskRoomCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestRewardTaskRewardCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestSubmitRewardTaskScoreCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestUserRewardTaskCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestUserRewardTaskRoomCommand;
import com.trans.pixel.protoc.RewardTaskProto.ResponseUserRewardTaskCommand;
import com.trans.pixel.protoc.RewardTaskProto.ResponseUserRewardTaskRoomCommand;
import com.trans.pixel.protoc.RewardTaskProto.UserRewardTask;
import com.trans.pixel.protoc.RewardTaskProto.UserRewardTask.REWARDTASK_STATUS;
import com.trans.pixel.protoc.RewardTaskProto.UserRewardTaskRoom;
import com.trans.pixel.service.LogService;
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
	private LogService logService;
	@Resource
	private UserRewardTaskService userRewardTaskService;
	
	public void submitScore(RequestSubmitRewardTaskScoreCommand cmd, Builder responseBuilder, UserBean user) {
		MultiReward.Builder rewards = MultiReward.newBuilder();
		List<UserInfo> errorUserList = new ArrayList<UserInfo>();
		List<UserEquipBean> userEquipList = new ArrayList<UserEquipBean>();
		ResultConst ret = rewardTaskService.submitRewardTaskScore(user, cmd.getIndex(), cmd.getRet(), rewards, errorUserList, userEquipList);
		if (ret instanceof ErrorConst) {
			if (ret.getCode() == ErrorConst.NOT_ENOUGH_PROP.getCode()) {
				pusher.pushOtherUserInfoCommand(responseBuilder, errorUserList);
				
				UserRewardTaskRoom.Builder room = rewardTaskService.getUserRoom(user, cmd.getIndex());
				if (room != null) {
					ResponseUserRewardTaskRoomCommand.Builder roombuilder = ResponseUserRewardTaskRoomCommand.newBuilder();
					roombuilder.addRoom(room);
					responseBuilder.setUserRewardTaskRoomCommand(roombuilder.build());
				}
			}
			
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ret);
			ErrorCommand errorCommand = buildErrorCommand(ret);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		handleRewards(responseBuilder, user, rewards.build());


		pusher.pushUserRewardTask(responseBuilder, user);
		
		pusher.pushUserEquipListCommand(responseBuilder, user, userEquipList);
	}
	
	public void createRoom(RequestCreateRewardTaskRoomCommand cmd, Builder responseBuilder, UserBean user) {
		UserRewardTaskRoom.Builder room = rewardTaskService.createRoom(user, cmd.getIndex());
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
		ResultConst ret = rewardTaskService.quitRoom(user, cmd.getUserId(), cmd.getIndex(), rewardTaskBuilder, roomBuilder);
		if (ret instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ret);
			ErrorCommand errorCommand = buildErrorCommand(ret);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		if (roomBuilder.getCreateUserId() == user.getId() && roomBuilder.getRoomInfoCount() > 0) {
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
		int index = cmd.getIndex();
		UserRewardTaskRoom.Builder room = rewardTaskService.getUserRoom(user, index);
		if(room == null && !userIds.isEmpty()) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.ROOM_NEED_CREATE_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.ROOM_NEED_CREATE_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
//		if(room != null && userIds.isEmpty() && room.getIndex() == index) {
//			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.BOSS_ROOM_HASIN);
//			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BOSS_ROOM_HASIN);
//            responseBuilder.setErrorCommand(errorCommand);
//			return;
//		}
//		UserRewardTask.Builder userRewardTaskBuilder = UserRewardTask.newBuilder();
		UserRewardTask.Builder userRewardTaskBuilder = rewardTaskService.inviteFightRewardTask(user, createUserId, userIds, id, index);
		
		if (userRewardTaskBuilder == null) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.BOSS_ROOM_HAS_START);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BOSS_ROOM_HAS_START);
            responseBuilder.setErrorCommand(errorCommand);
            
            return;
		} 
		
		if (userRewardTaskBuilder.hasStatus() && userRewardTaskBuilder.getStatus() == REWARDTASK_STATUS.LIMIT_VALUE) { // 悬赏任务已达上限
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.REWARDTASK_IS_LIMIT_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.REWARDTASK_IS_LIMIT_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            
            return;
		}
		
		if (userRewardTaskBuilder.hasStatus() && userRewardTaskBuilder.getStatus() == REWARDTASK_STATUS.FULL_VALUE) { // 房间人数已满
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.BOSS_ROOM_IS_FULL_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BOSS_ROOM_IS_FULL_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            
            return;
		}
		
		if (userRewardTaskBuilder.hasStatus() && userRewardTaskBuilder.getStatus() == REWARDTASK_STATUS.HAS_IN_VALUE) { // 已经加入该房间
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.BOSS_ROOM_HAS_BEIN_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BOSS_ROOM_HAS_BEIN_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            
            return;
		}
		
		ResponseUserRewardTaskRoomCommand.Builder builder = ResponseUserRewardTaskRoomCommand.newBuilder();
		builder.addRoom(rewardTaskService.getUserRoom(user, userRewardTaskBuilder.getIndex()));
		responseBuilder.setUserRewardTaskRoomCommand(builder.build());
		
		if (userIds.isEmpty()) {
			ResponseUserRewardTaskCommand.Builder commandBuilder = ResponseUserRewardTaskCommand.newBuilder();
			commandBuilder.addUserRewardTask(userRewardTaskBuilder);
			responseBuilder.setUserRewardTaskCommand(commandBuilder.build());
		}
	}
	
	public void getUserRewardTask(RequestUserRewardTaskCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseUserRewardTaskCommand.Builder builder = ResponseUserRewardTaskCommand.newBuilder();
		Map<Integer, UserRewardTask> list = userRewardTaskService.getUserRewardTaskList(user);
		builder.addAllUserRewardTask(list.values());
		responseBuilder.setUserRewardTaskCommand(builder.build());
	}
	
	public void getUserRewardTaskReward(RequestRewardTaskRewardCommand cmd, Builder responseBuilder, UserBean user) {
		UserRewardTask.Builder builder = UserRewardTask.newBuilder();
		MultiReward.Builder rewardList = rewardTaskService.getRewardList(user, cmd.getIndex(), builder);
		if (rewardList.getLootCount() > 0) {
			handleRewards(responseBuilder, user, rewardList);
			
			ResponseUserRewardTaskCommand.Builder userRewardTaskBuilder = ResponseUserRewardTaskCommand.newBuilder();
			userRewardTaskBuilder.addUserRewardTask(builder.build());
			responseBuilder.setUserRewardTaskCommand(userRewardTaskBuilder.build());
		} else {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.BOSS_IS_NOT_EXIST_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BOSS_IS_NOT_EXIST_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
		}
	}
	
	public void getUserRewardTaskRoom(RequestUserRewardTaskRoomCommand cmd, Builder responseBuilder, UserBean user) {
		int index = cmd.getIndex();
		UserRewardTaskRoom.Builder room = rewardTaskService.getUserRoom(user, index);
		
		if (room != null) {
			ResponseUserRewardTaskRoomCommand.Builder builder = ResponseUserRewardTaskRoomCommand.newBuilder();
			builder.addRoom(room);
			responseBuilder.setUserRewardTaskRoomCommand(builder.build());
			
			if (user.getId() == room.getCreateUserId()) {
				pusher.pushOtherUserInfoCommand(responseBuilder, rewardTaskService.getNotEnoughPropUser(room.build()));
			}
		}else {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.ROOM_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.ROOM_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
		}
	}
	
	public void giveupRewardTask(RequestGiveupRewardTaskCommand cmd, Builder responseBuilder, UserBean user) {
		UserRewardTask.Builder rewardTaskBuilder = UserRewardTask.newBuilder();
		UserRewardTaskRoom.Builder roomBuilder = UserRewardTaskRoom.newBuilder();
		ResultConst ret = rewardTaskService.giveupRewardtask(user, cmd.getIndex(), rewardTaskBuilder, roomBuilder);
		if (ret instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ret);
			ErrorCommand errorCommand = buildErrorCommand(ret);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		ResponseUserRewardTaskCommand.Builder builder = ResponseUserRewardTaskCommand.newBuilder();
		builder.addUserRewardTask(rewardTaskBuilder.build());
		responseBuilder.setUserRewardTaskCommand(builder.build());
	}
	
	public void changePosition(RequestChangePositionCommand cmd, Builder responseBuilder, UserBean user) {
		int position1 = cmd.getPosition1();
		int position2 = cmd.getPosition2();
		int index = cmd.getIndex();
		UserRewardTaskRoom.Builder roomBuilder = UserRewardTaskRoom.newBuilder();
		ResultConst ret = rewardTaskService.changePosition(user, index, position1, position2, roomBuilder);
		if (ret instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ret);
			ErrorCommand errorCommand = buildErrorCommand(ret);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		ResponseUserRewardTaskRoomCommand.Builder builder = ResponseUserRewardTaskRoomCommand.newBuilder();
		builder.addRoom(roomBuilder.build());
		responseBuilder.setUserRewardTaskRoomCommand(builder.build());
	}
}
