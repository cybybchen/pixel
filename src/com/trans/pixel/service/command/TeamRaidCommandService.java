package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.RewardTaskProto.EventProgress;
import com.trans.pixel.protoc.RewardTaskProto.RequestCreateTeamRaidRoomCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestGetTeamRaidRewardCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestInviteToTeamRaidRoomCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestOpenTeamRaidCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestQuitTeamRaidRoomCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestStartTeamRaidCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestTeamRaidCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestTeamRaidRoomChangePositionCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestTeamRaidRoomCommand;
import com.trans.pixel.protoc.RewardTaskProto.ResponseTeamRaidCommand;
import com.trans.pixel.protoc.RewardTaskProto.ResponseTeamRaidRoomCommand;
import com.trans.pixel.protoc.RewardTaskProto.ResponseUserRewardTaskCommand;
import com.trans.pixel.protoc.RewardTaskProto.RoomInfo;
import com.trans.pixel.protoc.RewardTaskProto.TeamRaid;
import com.trans.pixel.protoc.RewardTaskProto.UserRoom;
import com.trans.pixel.protoc.UserInfoProto.EventConfig;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.CostService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.RankService;
import com.trans.pixel.service.UserRewardTaskService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.redis.LevelRedisService;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.service.redis.TeamRaidRedisService;

@Service
public class TeamRaidCommandService extends BaseCommandService{
	Logger logger = Logger.getLogger(TeamRaidCommandService.class);
	@Resource
    private TeamRaidRedisService redis;
	@Resource
    private PushCommandService pusher;
	@Resource
    private CostService costService;
	@Resource
	private LogService logService;
	@Resource
	private ActivityService activityService;
	@Resource
	private LevelRedisService levelRedisService;
	@Resource
	private RankService rankService;
	@Resource
	private UserService userService;


	public void openTeamRaid(RequestOpenTeamRaidCommand cmd, Builder responseBuilder, UserBean user){
		TeamRaid raidconfig = redis.getTeamRaid(cmd.getId());
		ResponseTeamRaidCommand.Builder raidlist = redis.getTeamRaid(user);
		int index = 0;
		for(index = 0; index < raidlist.getRaidCount(); index++) {
			TeamRaid.Builder myraid = raidlist.getRaidBuilder(index);
			if(myraid.getId() == cmd.getId()) {
				if(myraid.getStatus() == 0) {
					logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.RAID_LOCKED_ERROR);
					ErrorCommand errorCommand = buildErrorCommand(ErrorConst.RAID_LOCKED_ERROR);
		            responseBuilder.setErrorCommand(errorCommand);
		            return;
				}if(myraid.getCount() > 0 && myraid.getLeftcount() <= 0) {//次数用完
					logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH_TIMES);
					ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_ENOUGH_TIMES);
		            responseBuilder.setErrorCommand(errorCommand);
					return;
				}
				break;
			}
		}
		if(index >= raidlist.getRaidCount()) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.RAID_NOT_OPEN);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.RAID_NOT_OPEN);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		//判断消耗
//		if(costService.cost(user, raidconfig.getCost().getItemid(), raidconfig.getCost().getCount(), true)){
		for(TeamRaid.Builder myraid : raidlist.getRaidBuilderList()) {
			if(myraid.getId() != raidconfig.getId())
				continue;
			myraid.clearRoomInfo();
			myraid.clearEvent();
			myraid.addAllEvent(raidconfig.getEventList());
			if(myraid.getLeftcount() > 0)
				myraid.setLeftcount(myraid.getLeftcount()-1);
			redis.saveTeamRaid(user, myraid);
			responseBuilder.setTeamRaidCommand(raidlist);
			pusher.pushUserDataByRewardId(responseBuilder, user, raidconfig.getCost().getItemid());
			break;
		}

//			Map<String, String> params = new HashMap<String, String>();
//			params.put(LogString.USERID, "" + user.getId());
//			params.put(LogString.SERVERID, "" + user.getServerId());
//			params.put(LogString.RESULT, "2");
//			params.put(LogString.INSTANCEID, "" + raidconfig.getId());
//			params.put(LogString.FLOOR, "" + raidconfig.getLevel());
//			params.put(LogString.BOSSID, "0");
//			logService.sendLog(params, LogString.LOGTYPE_RAID);
		
//		}else{
//			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH);
//			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_ENOUGH);
//            responseBuilder.setErrorCommand(errorCommand);
//		}
	}
	
	public void getTeamRaidRoom(RequestTeamRaidRoomCommand cmd, Builder responseBuilder, UserBean user) {
		UserRoom.Builder room = redis.getUserRoom(user.getId(), cmd.getIndex());
//		if (ut == null || ut.getRoomInfo() == null) {
//			redis.delUserRoom(user, cmd.getIndex());
//			return null;
//		}
		 
//		UserRoom.Builder room = userRoomRedisService.getUserRoomRoom(ut.getRoomInfo().getUser().getId(), ut.getRoomInfo().getIndex());
		
		if (room != null) {
			if(!UserRewardTaskService.hasMeInRoom(user, room)) {//非法：我不在房间中
				TeamRaid.Builder myraid = redis.getTeamRaid(user.getId(), cmd.getIndex()/redis.INDEX_SIZE);
				if(myraid.hasRoomInfo() && myraid.getRoomInfo().getIndex() == cmd.getIndex()) {
					myraid.clearRoomInfo();
					redis.saveTeamRaid(user, myraid);
					if(room.getCreateUserId() == user.getId()) {
						redis.delUserRoom(user, cmd.getIndex());
					}
				}
				
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.ROOM_OUT_ERROR);
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.ROOM_OUT_ERROR);
	            responseBuilder.setErrorCommand(errorCommand);
			}else {
				ResponseTeamRaidRoomCommand.Builder builder = ResponseTeamRaidRoomCommand.newBuilder();
				builder.addRoom(room);
				responseBuilder.setTeamRaidRoomCommand(builder.build());
				
//				if (user.getId() == room.getCreateUserId()) {
//					pusher.pushOtherUserInfoCommand(responseBuilder, rewardTaskService.getNotEnoughPropUser(room.build()));
//				}
			}
		}else {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.ROOM_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.ROOM_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
		}
	}
		
	public void createTeamRaidRoom(RequestCreateTeamRaidRoomCommand cmd, Builder responseBuilder, UserBean user) {
		TeamRaid.Builder myraid = redis.getTeamRaid(user.getId(), cmd.getId());
		if (myraid == null) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.BOSS_ROOM_CREATE_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BOSS_ROOM_CREATE_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}

		if(myraid.hasRoomInfo()) {
			UserRoom.Builder room = redis.getUserRoom(myraid.getRoomInfo().getUser().getId(), myraid.getRoomInfo().getIndex());
			if (room != null && isInRoom(room, user)) {
				ResponseTeamRaidRoomCommand.Builder roombuilder = ResponseTeamRaidRoomCommand.newBuilder();
				roombuilder.addRoom(room);
				responseBuilder.setTeamRaidRoomCommand(roombuilder.build());
				return;
			}else {
				myraid.clearRoomInfo();
			}
		}
		UserRoom.Builder room = UserRoom.newBuilder();
		UserInfo create = userService.getCache(user.getServerId(), user.getId());
		room.setCreateUserId(user.getId());
		myraid.setIndex((myraid.getIndex()+1)%redis.INDEX_SIZE+myraid.getId()*redis.INDEX_SIZE);
		room.setIndex(myraid.getIndex());
		RoomInfo.Builder roomInfoBuilder = RoomInfo.newBuilder();
		roomInfoBuilder.setIndex(myraid.getIndex());
		roomInfoBuilder.setUser(create);
		roomInfoBuilder.setPosition(1);//排序位置
		room.addRoomInfo(roomInfoBuilder);
		redis.saveUserRoom(room.build());
		
		myraid.setRoomInfo(roomInfoBuilder);
		redis.saveTeamRaid(user, myraid);
//		UserRoom.Builder room = userRoomService.createRoom(user, cmd.getIndex());
//		if (room == null) {
//			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.BOSS_ROOM_CREATE_ERROR);
//			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BOSS_ROOM_CREATE_ERROR);
//            responseBuilder.setErrorCommand(errorCommand);
//            return;
//		}
		
		ResponseTeamRaidRoomCommand.Builder roombuilder = ResponseTeamRaidRoomCommand.newBuilder();
		roombuilder.addRoom(room);
		responseBuilder.setTeamRaidRoomCommand(roombuilder.build());
	}
	
	public void quitTeamRaidRoom(RequestQuitTeamRaidRoomCommand cmd, Builder responseBuilder, UserBean user) {
//		UserRewardTask.Builder myraid = UserRewardTask.newBuilder();
//		ResultConst ret = rewardTaskService.quitRoom(user, cmd.getUserId(), cmd.getIndex(), rewardTaskBuilder, roomBuilder);
		TeamRaid.Builder myraid = redis.getTeamRaid(user.getId(), cmd.getIndex()/redis.INDEX_SIZE);
		
		if (myraid.getRoomInfo() == null) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.ROOM_IS_NOT_EXIST_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.ROOM_IS_NOT_EXIST_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		
		UserRoom.Builder room = redis.getUserRoom(myraid.getRoomInfo().getUser().getId(), myraid.getRoomInfo().getIndex());
		if (room == null) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.ROOM_IS_NOT_EXIST_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.ROOM_IS_NOT_EXIST_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		if (user.getId() != cmd.getUserId() && room.getCreateUserId() != user.getId()) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.BOSS_ROOM_CAN_NOT_QUIT_OTHER);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BOSS_ROOM_CAN_NOT_QUIT_OTHER);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		if(!redis.setLock("TeamRaidRoom_"+room.getCreateUserId()+"_"+room.getIndex())) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.ERROR_LOCKED);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.ERROR_LOCKED);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}

		if (room.getCreateUserId() == user.getId() && user.getId() == cmd.getUserId()) {//退出自己的房间
			redis.delUserRoom(user, room.getIndex());
			for (RoomInfo roomInfo :room.getRoomInfoList()) {
				if (roomInfo.getUser().getId() != user.getId()) {
					TeamRaid.Builder hisraid = redis.getTeamRaid(roomInfo.getUser().getId(), roomInfo.getIndex()/redis.INDEX_SIZE);
					hisraid.clearRoomInfo();
					redis.saveTeamRaid(roomInfo.getUser().getId(), hisraid);
				}
			}
			room.clearRoomInfo();
			myraid.clearRoomInfo();
		} else {
			for (int i = 0; i < room.getRoomInfoCount(); ++i) {
				if (room.getRoomInfo(i).getUser().getId() == cmd.getUserId()) {
					if (cmd.getUserId() != user.getId()) {
						TeamRaid.Builder hsiraid = redis.getTeamRaid(cmd.getUserId(), room.getRoomInfo(i).getIndex());
						if(hsiraid != null) {
							hsiraid.clearRoomInfo();
							redis.saveTeamRaid(cmd.getUserId(), hsiraid);	
						}
					}
					room.removeRoomInfo(i);
					break;
				}
			}
			redis.saveUserRoom(room.build());
			if (cmd.getUserId() == user.getId()) {
				myraid.clearRoomInfo();
			}
		}
		
		redis.saveTeamRaid(user, myraid);
		redis.clearLock("TeamRaidRoom_"+room.getCreateUserId()+"_"+room.getIndex());
		
		if (room.getCreateUserId() == user.getId() && room.getRoomInfoCount() > 0) {
			ResponseTeamRaidRoomCommand.Builder roombuilder = ResponseTeamRaidRoomCommand.newBuilder();
			roombuilder.addRoom(room.build());
			responseBuilder.setTeamRaidRoomCommand(roombuilder.build());
		}
		
		getTeamRaid(responseBuilder, user);
	}
	public void inviteToTeamRaidRoom(RequestInviteToTeamRaidRoomCommand cmd, Builder responseBuilder, UserBean user) {
		List<Long> userIds = cmd.getUserIdList();
//		int id = cmd.getId();
		long createUserId = cmd.getCreateUserId();
		int index = cmd.getIndex();
		TeamRaid.Builder myraid = redis.getTeamRaid(user.getId(), index/redis.INDEX_SIZE);
		
//		UserRewardTask.Builder userRewardTaskBuilder = rewardTaskService.inviteFightRewardTask(user, createUserId, userIds, index);
		if (userIds.isEmpty()) {//接收邀请
			if(userIds.isEmpty() && myraid != null) {
				if(myraid.hasRoomInfo()) {
					logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.BOSS_ROOM_HASIN);
					ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BOSS_ROOM_HASIN);
		            responseBuilder.setErrorCommand(errorCommand);
					return;
				}else if(myraid.getStatus() == 0) {
					logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.RAID_LOCKED_ERROR);
					ErrorCommand errorCommand = buildErrorCommand(ErrorConst.RAID_LOCKED_ERROR);
		            responseBuilder.setErrorCommand(errorCommand);
		            return;
				}else if(myraid.getEventCount() == 0) {
					logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.RAID_NOTOPEN_ERROR);
					ErrorCommand errorCommand = buildErrorCommand(ErrorConst.RAID_NOTOPEN_ERROR);
		            responseBuilder.setErrorCommand(errorCommand);
		            return;
				}
			}
			UserRoom.Builder room = redis.getUserRoom(createUserId, index);
			TeamRaid.Builder hisraid = redis.getTeamRaid(createUserId, index/redis.INDEX_SIZE);
			if (room == null || hisraid == null) {
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.BOSS_ROOM_HAS_START);
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BOSS_ROOM_HAS_START);
	            responseBuilder.setErrorCommand(errorCommand);
	            return;
			}
			int eventi = 0;
			for(eventi = 0; eventi < myraid.getEventCount(); eventi++) {
				if(myraid.getEvent(eventi).getStatus() == 0)
					break;
			}
			if(eventi >= myraid.getEventCount()) {
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.RAID_PASS_ERROR);
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.RAID_PASS_ERROR);
	            responseBuilder.setErrorCommand(errorCommand);
	            return;
			}
//			Map<Integer, UserRewardTask> map = userRewardTaskService.getUserRewardTaskList(user);
//			for (UserRewardTask urt : map.values()) {
//				if(urt.getTask().getId() == hisraid.getTaskBuilder().getId() && urt.getTask().getRandcount() == hisraid.getTaskBuilder().getRandcount()) {
//					hisraid.setLeftcount(urt.getLeftcount());
//					hisraid.setIndex(urt.getIndex());
//					hisraid.setIsOver(urt.getIsOver());
//					if(hisraid.hasEndtime())
//						hisraid.setEndtime(urt.getEndtime());
//					break;
//				}
//			}
//			int rewardTaskCount = 0;
//			for (UserRewardTask urt : map.values()) {
//				if (urt.getStatus() != REWARDTASK_STATUS.END_VALUE)
//					rewardTaskCount++;
//			}
//			if (hisraid.getLeftcount() == 0) {
//				hisraid.setStatus(REWARDTASK_STATUS.LIMIT_VALUE);
//				return hisraid;
//			}
			 
//			for (RoomInfo roomInfo : room.getRoomInfoList()) {
//				if (roomInfo.getUser().getId() == user.getId()) {
//					hisraid.setStatus(REWARDTASK_STATUS.HAS_IN_VALUE);
////					rewardTask.setRoomInfo(roomInfo);
//					return hisraid;
//				}
//			}
			if (room.getRoomInfoCount() >= hisraid.getMembers()) {
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.BOSS_ROOM_IS_FULL_ERROR);
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BOSS_ROOM_IS_FULL_ERROR);
	            responseBuilder.setErrorCommand(errorCommand);
	            return;
			}
			if(!redis.setLock("TeamRaidRoom_"+room.getCreateUserId()+"_"+room.getIndex())) {
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.ERROR_LOCKED);
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.ERROR_LOCKED);
	            responseBuilder.setErrorCommand(errorCommand);
	            return;
			}
			for (int i = 0; i < room.getRoomInfoCount(); ++i) {
				if (room.getRoomInfo(i).getUser().getId() == createUserId) {
					myraid.setRoomInfo(room.getRoomInfo(i));
					break;
				}
			}
			redis.saveTeamRaid(user, myraid);
			
			RoomInfo.Builder roomInfoBuilder = RoomInfo.newBuilder();
			roomInfoBuilder.setIndex(hisraid.getIndex());
			roomInfoBuilder.setUser(userService.getCache(user.getServerId(), user.getId()));
			for (int position = 1; position <= hisraid.getMembers(); ++position) {
				if (!redis.hasPosition(position, room.getRoomInfoList())) {
					roomInfoBuilder.setPosition(position);
					break;
				}
			}
			room.addRoomInfo(roomInfoBuilder.build());
			redis.saveUserRoom(room.build());
			redis.clearLock("TeamRaidRoom_"+room.getCreateUserId()+"_"+room.getIndex());

			ResponseTeamRaidRoomCommand.Builder builder = ResponseTeamRaidRoomCommand.newBuilder();
			builder.addRoom(room);
			responseBuilder.setTeamRaidRoomCommand(builder.build());
			ResponseTeamRaidCommand.Builder commandBuilder = ResponseTeamRaidCommand.newBuilder();
			commandBuilder.addRaid(myraid);
			responseBuilder.setTeamRaidCommand(commandBuilder.build());
		} else { //邀请别人
			UserRoom.Builder room = redis.getUserRoom(user.getId(), index);
			if(room == null) {
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.ROOM_NEED_CREATE_ERROR);
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.ROOM_NEED_CREATE_ERROR);
	            responseBuilder.setErrorCommand(errorCommand);
				return;
			}
			for (long userId : userIds) {
				String content = "邀请你一起攻打团本:"+myraid.getName()+"！";
				redis.sendInviteMail(user, userId, index, content);
			}
		}
	}

	private boolean isAllClear(TeamRaid.Builder myraid) {
		for(int j = 0; j < myraid.getEventCount(); j++) {
			if(myraid.getEvent(j).getStatus() != 1)
				return false;
		}
		return true;
	}

	private boolean isInRoom(UserRoom.Builder room, UserBean user) {
		for(RoomInfo roominfo : room.getRoomInfoList()) {
			if(roominfo.getUser().getId() == user.getId())
				return true;
		}
		return false;
	}
	public void startTeamRaid(RequestStartTeamRaidCommand cmd, Builder responseBuilder, UserBean user){
		ResponseTeamRaidCommand.Builder raidlist = redis.getTeamRaid(user);
		TeamRaid.Builder myraid = getMyTeamRaid(raidlist, cmd.getId());
		if(myraid == null) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_MONSTER);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_MONSTER);
	        responseBuilder.setErrorCommand(errorCommand);
			responseBuilder.setTeamRaidCommand(raidlist);
			return;
		}
		if(!cmd.getRet() && cmd.getTurn() == 0){//give up
			myraid.clearEvent();
			if(myraid.hasRoomInfo()) {
				if(myraid.getRoomInfo().getUser().getId() == user.getId()) {//解散房间
					UserRoom.Builder room = redis.getUserRoom(user.getId(), myraid.getRoomInfo().getIndex());
					if(room != null)
					for(RoomInfo roominfo : room.getRoomInfoList()) {
						if(roominfo.getUser().getId() == user.getId())
							continue;
						TeamRaid.Builder hisraid = redis.getTeamRaid(roominfo.getUser().getId(), cmd.getId());
						if(hisraid != null && hisraid.hasRoomInfo() && hisraid.getRoomInfo().getUser().getId() == user.getId()) {
							hisraid.clearRoomInfo();
							redis.saveTeamRaid(roominfo.getUser().getId(), hisraid);
						}
					}
					redis.delUserRoom(user, myraid.getRoomInfo().getIndex());
				}else {//退出房间
					UserRoom.Builder room = redis.getUserRoom(user.getId(), myraid.getRoomInfo().getIndex());
					if(!redis.setLock("TeamRaidRoom_"+room.getCreateUserId()+"_"+room.getIndex())) {
						logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.ERROR_LOCKED);
						ErrorCommand errorCommand = buildErrorCommand(ErrorConst.ERROR_LOCKED);
				        responseBuilder.setErrorCommand(errorCommand);
						responseBuilder.setTeamRaidCommand(raidlist);
						return;
					}
					if(room != null)
					for(int j = 0; j < room.getRoomInfoCount(); j++) {
						if(room.getRoomInfo(j).getUser().getId() == user.getId()){
							room.removeRoomInfo(j);
							redis.saveUserRoom(room.build());
							break;
						}
					}
				}
				myraid.clearRoomInfo();
			}
			redis.saveTeamRaid(user, myraid);
			responseBuilder.setTeamRaidCommand(raidlist);
			return;
		}
		for(int i = 0; i < myraid.getEventCount(); i++) {
			if(cmd.getEventid() != myraid.getEvent(i).getEventid())
				continue;
			EventConfig event = levelRedisService.getEvent(cmd.getEventid());
			if(myraid.hasRoomInfo() && myraid.getRoomInfo().getUser().getId() != user.getId()) {
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.BOSS_ROOM_CAN_NOT_QUIT_OTHER);
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BOSS_ROOM_CAN_NOT_QUIT_OTHER);
		        responseBuilder.setErrorCommand(errorCommand);
			}else if(event == null || myraid.getEventBuilder(i).getStatus() != 0){
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_MONSTER);
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_MONSTER);
	            responseBuilder.setErrorCommand(errorCommand);
			}else if(cmd.getRet()){
				myraid.getEventBuilder(i).setStatus(1);
				MultiReward.Builder rewards = levelRedisService.eventReward(user, event, myraid.getLevel());
				handleRewards(responseBuilder, user, rewards.build());
				boolean isAllClear = isAllClear(myraid);
				if(myraid.hasRoomInfo()) {
					UserRoom.Builder room = redis.getUserRoom(user.getId(), myraid.getRoomInfo().getIndex());
					if(room != null)
					for(RoomInfo roominfo : room.getRoomInfoList()) {
						if(roominfo.getUser().getId() == user.getId())
							continue;
						TeamRaid.Builder hisraid = redis.getTeamRaid(roominfo.getUser().getId(), cmd.getId());
						if(hisraid != null && hisraid.hasRoomInfo() && hisraid.getRoomInfo().getUser().getId() == user.getId()) {
							for(EventProgress.Builder progress : hisraid.getEventBuilderList()) {
								if(progress.getEventid() == cmd.getEventid() && progress.getStatus() == 0) {
									progress.setStatus(2);
								}
							}
							if(isAllClear) {
								hisraid.clearRoomInfo();
							}
							redis.saveTeamRaid(roominfo.getUser().getId(), hisraid);
						}
					}
				}
				if(isAllClear) {
					myraid.clearEvent();
					if(myraid.hasRoomInfo()) {
						redis.delUserRoom(user, myraid.getRoomInfo().getIndex());
						myraid.clearRoomInfo();
					}
				}
				redis.saveTeamRaid(user, myraid);
			}
//				if(!responseBuilder.hasErrorCommand()) {
//				Map<String, String> params = new HashMap<String, String>();
//				params.put(LogString.USERID, "" + user.getId());
//				params.put(LogString.SERVERID, "" + user.getServerId());
//				params.put(LogString.RESULT, cmd.getRet() ? "1":"0");
//				params.put(LogString.INSTANCEID, "" + myraid.getId());
//				params.put(LogString.FLOOR, "" + myraid.getLevel());
//				params.put(LogString.BOSSID, "" + (event.hasEnemygroup()?event.getEnemygroup().getEnemy(0).getEnemyid() : 0));
//				params.put(LogString.PREINSTANCEID, "" + (!cmd.getRet() && cmd.getTurn() == 0 ? myraid.getEventid() : 0));
//				logService.sendLog(params, LogString.LOGTYPE_RAID);
//				}
			responseBuilder.setTeamRaidCommand(raidlist);
		}
	}
	
	public TeamRaid.Builder getMyTeamRaid(ResponseTeamRaidCommand.Builder raidlist, int id){
		for(TeamRaid.Builder myraid : raidlist.getRaidBuilderList()) {
			if(myraid.getId() == id)
				return myraid;
		}
		return null;
	}
	
	public void getTeamRaid(RequestTeamRaidCommand cmd, Builder responseBuilder, UserBean user){
		getTeamRaid(responseBuilder, user);
	}
	
	public void getTeamRaid(Builder responseBuilder, UserBean user){
		ResponseTeamRaidCommand.Builder raidlist = redis.getTeamRaid(user);
		responseBuilder.setTeamRaidCommand(raidlist);
	}
	
	public void changePosition(RequestTeamRaidRoomChangePositionCommand cmd, Builder responseBuilder, UserBean user) {
		int position1 = cmd.getPosition1();
		int position2 = cmd.getPosition2();
		int index = cmd.getIndex();
//		UserRoom.Builder roomBuilder = UserRoom.newBuilder();
//		ResultConst ret = rewardTaskService.changePosition(user, index, position1, position2, roomBuilder);
		UserRoom.Builder room = redis.getUserRoom(user.getId(), index);
		if (room == null) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.ROOM_IS_NOT_EXIST_ERROR);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.ROOM_IS_NOT_EXIST_ERROR);
	        responseBuilder.setErrorCommand(errorCommand);
		}else if (room.getCreateUserId() != user.getId()) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.BOSS_ROOM_CAN_NOT_QUIT_OTHER);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BOSS_ROOM_CAN_NOT_QUIT_OTHER);
	        responseBuilder.setErrorCommand(errorCommand);
		}else {
			if(redis.setLock("TeamRaidRoom_"+room.getCreateUserId()+"_"+room.getIndex())) {
				for (int i = 0; i < room.getRoomInfoCount(); ++i) {
					RoomInfo.Builder builder = room.getRoomInfoBuilder(i);
					if (builder.getPosition() == position1)
						builder.setPosition(position2);
					else if (builder.getPosition() == position2)
						builder.setPosition(position1);
					
	//				room.setRoomInfo(i, builder.build());
				}
				redis.saveUserRoom(room.build());
				redis.clearLock("TeamRaidRoom_"+room.getCreateUserId()+"_"+room.getIndex());
			}else {
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.ERROR_LOCKED);
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.ERROR_LOCKED);
		        responseBuilder.setErrorCommand(errorCommand);
			}
		}
		
		ResponseTeamRaidRoomCommand.Builder builder = ResponseTeamRaidRoomCommand.newBuilder();
		builder.addRoom(room);
		responseBuilder.setTeamRaidRoomCommand(builder.build());
	}
	
	public void getTeamRaidReward(RequestGetTeamRaidRewardCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseTeamRaidCommand.Builder raidlist = redis.getTeamRaid(user);
		TeamRaid.Builder myraid = getMyTeamRaid(raidlist, cmd.getId());
		if(myraid == null) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_MONSTER);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_MONSTER);
	        responseBuilder.setErrorCommand(errorCommand);
			responseBuilder.setTeamRaidCommand(raidlist);
			return;
		}
		for(int i = 0; i < myraid.getEventCount(); i++) {
			if(cmd.getEventid() != myraid.getEvent(i).getEventid())
				continue;
			EventConfig event = levelRedisService.getEvent(cmd.getEventid());
			if(event == null || myraid.getEventBuilder(i).getStatus() != 2){
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.GET_REWARD_AGAIN);
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.GET_REWARD_AGAIN);
	            responseBuilder.setErrorCommand(errorCommand);
			}else if(costService.cost(user, event.getCost().getItemid(), event.getCost().getCount(), true)){
				myraid.getEventBuilder(i).setStatus(1);
				MultiReward.Builder rewards = levelRedisService.eventReward(user, event, myraid.getLevel());
				handleRewards(responseBuilder, user, rewards.build());
				if(isAllClear(myraid)) {
					myraid.clearEvent();
					myraid.clearRoomInfo();
				}
				redis.saveTeamRaid(user, myraid);
			}else{
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH);
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_ENOUGH);
	            responseBuilder.setErrorCommand(errorCommand);
			}
//				if(!responseBuilder.hasErrorCommand()) {
//				Map<String, String> params = new HashMap<String, String>();
//				params.put(LogString.USERID, "" + user.getId());
//				params.put(LogString.SERVERID, "" + user.getServerId());
//				params.put(LogString.RESULT, cmd.getRet() ? "1":"0");
//				params.put(LogString.INSTANCEID, "" + myraid.getId());
//				params.put(LogString.FLOOR, "" + myraid.getLevel());
//				params.put(LogString.BOSSID, "" + (event.hasEnemygroup()?event.getEnemygroup().getEnemy(0).getEnemyid() : 0));
//				params.put(LogString.PREINSTANCEID, "" + (!cmd.getRet() && cmd.getTurn() == 0 ? myraid.getEventid() : 0));
//				logService.sendLog(params, LogString.LOGTYPE_RAID);
//				}
			responseBuilder.setTeamRaidCommand(raidlist);
		}
	}
}
