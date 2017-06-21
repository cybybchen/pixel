package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.LogString;
import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.model.userinfo.UserEquipPokedeBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.RewardTaskProto.RewardTask;
import com.trans.pixel.protoc.RewardTaskProto.RoomInfo;
import com.trans.pixel.protoc.RewardTaskProto.UserRewardTask;
import com.trans.pixel.protoc.RewardTaskProto.UserRewardTask.REWARDTASK_STATUS;
import com.trans.pixel.protoc.RewardTaskProto.UserRewardTaskRoom;
import com.trans.pixel.protoc.UserInfoProto.EventConfig;
import com.trans.pixel.service.redis.LevelRedisService;
import com.trans.pixel.service.redis.RewardTaskRedisService;

@Service
public class RewardTaskService {

	private static final Logger log = LoggerFactory.getLogger(RewardTaskService.class);
	
	@Resource
	private RewardTaskRedisService rewardTaskRedisService;
	@Resource
	private UserRewardTaskService userRewardTaskService;
	@Resource
	private CostService costService;
	@Resource
	private UserService userService;
	@Resource
	private MailService mailService;
	@Resource
	private LevelRedisService userLevelService;
	@Resource
	private LogService logService;
	@Resource
	private ActivityService activityService;
	@Resource
	private UserEquipService userEquipService;
	@Resource
	private LevelRedisService levelRedisService;
	@Resource
	private UserEquipPokedeService userEquipPokedeService;
	
//	public ResultConst zhaohuanTask(UserBean user, int id) {
////		UserRewardTask oldTask = userRewardTaskService.getUserRewardTask(user, id);
////		if (oldTask != null && oldTask.getStatus() == 0) 
////			return ErrorConst.BOSS_HAS_ZHAOHUAN;
//		List<UserRewardTask> userRewardTaskList = userRewardTaskService.getUserRewardTaskList(user);
//		int rewardTaskCount = 0;
//		for (UserRewardTask urt : userRewardTaskList) {
//			if (urt.getStatus() != REWARDTASK_STATUS.END_VALUE)
//				rewardTaskCount++;
//		}
//		
//		if (rewardTaskCount >= 20) 
//			return ErrorConst.REWARDTASK_IS_LIMIT_ERROR;
//		
//		user.setRewardTaskIndex(user.getRewardTaskIndex() + 1);
//		userService.updateUser(user);
//		
//		UserRewardTask.Builder newTask = UserRewardTask.newBuilder(initUserRewardTask(user, id, user.getRewardTaskIndex()));
//		userRewardTaskService.updateUserRewardTask(user, newTask.build());
//		
//		return SuccessConst.USE_PROP;
//	}
	
	public ResultConst submitRewardTaskScore(UserBean user, int index, boolean ret, MultiReward.Builder rewards, List<UserInfo> errorUserList, List<UserEquipBean> userEquipList) {
		UserRewardTask userRewardTask = userRewardTaskService.getUserRewardTask(user, index);
		if (userRewardTask == null || userRewardTask.getStatus() != REWARDTASK_STATUS.LIVE_VALUE || userRewardTask.getLeftcount() == 0) {
			return ErrorConst.NOT_MONSTER;
		}
		RewardTask rewardTask = userRewardTask.getTask();
		if(ret) {
			ResultConst result = SuccessConst.BOSS_SUBMIT_SUCCESS;
			EventConfig event = levelRedisService.getEvent(rewardTask.getEventid());
			if(event.hasCost()) {
				int costId = costService.canCost(user, event.getCost());
				if (costId == 0) {
					errorUserList.add(user.buildShort());
					result = ErrorConst.NOT_ENOUGH_PROP;
				}
			}
			
			ResultConst result2 = handleRewardTaskRoom(user, index, rewardTask, errorUserList);
			if (result instanceof ErrorConst || result2 instanceof ErrorConst)
				return result;
			
			if(event.hasCost())
				costService.cost(user, event.getCost());
			UserRewardTask.Builder builder = UserRewardTask.newBuilder(userRewardTask);
			if(builder.getTask().getType() == 2){
				userRewardTaskService.refresh(builder);
			}else{
				builder.setLeftcount(builder.getLeftcount()-1);
				if(builder.getLeftcount() == 0)
					builder.setStatus(REWARDTASK_STATUS.END_VALUE);
			}
			userRewardTaskService.updateUserRewardTask(user, builder.build());
			if(event.hasCost()){
				UserEquipBean userEquip = userEquipService.selectUserEquip(user.getId(), event.getCost().getItemid());
				if (userEquip != null && userEquip.getEquipCount() > 0)
					userEquipList.add(userEquip);
			}
			rewards.addAllLoot(rewardTask.getLootlistList());
			rewards.addAllLoot(event.getLootlistList());
			if(rewardTask.getType() == 2){//深渊
				for(int i = rewards.getLootCount() - 1; i >= 0; i--) {
					int itemid = rewards.getLoot(i).getItemid();
					if(itemid/10000*10000 == RewardConst.EQUIPMENT) {
						UserEquipPokedeBean bean = userEquipPokedeService.selectUserEquipPokede(user, itemid);
						if(bean != null){
							rewards.getLootBuilder(i).setItemid(24010);
						}
					}
				}
			}
		}

		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.USERID, "" + user.getId());
		params.put(LogString.SERVERID, "" + user.getServerId());
		params.put(LogString.BOSSID, "" + rewardTask.getEventid());
		if(userRewardTask.hasRoomInfo()) {
			params.put(LogString.TEAM, "1");
		} else {
			params.put(LogString.TEAM, "0");
		}
		params.put(LogString.RESULT, ret ? "1" : "0");
		params.put(LogString.DPS, "0");
		UserLevelBean userLevel = userLevelService.getUserLevel(user);
		params.put(LogString.LEVEL, "" + userLevel.getUnlockDaguan());
		params.put(LogString.ZHANLI, "" + user.getZhanli());
		params.put(LogString.VIPLEVEL, "" + user.getVip());
		
		logService.sendLog(params, LogString.LOGTYPE_REWARDBOSS);
		
		return SuccessConst.BOSS_SUBMIT_SUCCESS;
	}
	
	public UserRewardTaskRoom createRoom(UserBean user, int index) {
		UserRewardTask ut = userRewardTaskService.getUserRewardTask(user, index);
		if (ut == null || ut.getStatus() != REWARDTASK_STATUS.LIVE_VALUE) {
			return null;
		}
		UserRewardTaskRoom room = rewardTaskRedisService.getUserRewardTaskRoom(user.getId(), ut.getIndex());
		if (room != null)
			return room;
		
		UserInfo create = userService.getCache(user.getServerId(), user.getId());
		UserRewardTaskRoom.Builder builder = UserRewardTaskRoom.newBuilder();
		builder.setCreateUserId(user.getId());
		builder.setIndex(ut.getIndex());
		RoomInfo.Builder roomInfoBuilder = RoomInfo.newBuilder();
		roomInfoBuilder.setIndex(ut.getIndex());
		roomInfoBuilder.setUser(create);
		builder.addRoomInfo(roomInfoBuilder.build());
		builder.setEventid(ut.getTask().getEventid());
		rewardTaskRedisService.setUserRewardTaskRoom(builder.build());
		
		UserRewardTask.Builder utBuilder = UserRewardTask.newBuilder(ut);
		utBuilder.setRoomInfo(roomInfoBuilder.build());
		userRewardTaskService.updateUserRewardTask(user, utBuilder.build());
		return builder.build();
		
	}
	
	private ResultConst handleRewardTaskRoom(UserBean user, int index, RewardTask rewardTask, List<UserInfo> errorUserList) {
		UserRewardTaskRoom room = rewardTaskRedisService.getUserRewardTaskRoom(user.getId(), index);
		if (room == null) {
			activityService.completeRewardTask(user.getId(), rewardTask.getType());//单独完成悬赏任务
			return SuccessConst.BOSS_SUBMIT_SUCCESS;
		}
		EventConfig event = levelRedisService.getEvent(rewardTask.getEventid());
		
		boolean enoughProp = true;
		for (RoomInfo roomInfo : room.getRoomInfoList()) {
			UserInfo userinfo = roomInfo.getUser();
			if (userinfo.getId() == user.getId())
				continue;
			UserBean other = userService.getUserOther(userinfo.getId());
			if(event.hasCost()) {
				int costId = costService.canCost(other, event.getCost());
				if (costId == 0) {
					errorUserList.add(userinfo);
					enoughProp = false;
				}
			}
		}
		
		if (!enoughProp)
			return ErrorConst.NOT_ENOUGH_PROP;
		
		for (RoomInfo roomInfo : room.getRoomInfoList()) {
			UserInfo userinfo = roomInfo.getUser();
			if (userinfo.getId() != user.getId()) {
				if(event.hasCost())
					costService.cost(userinfo.getId(), event.getCost());
				UserRewardTask.Builder builder = UserRewardTask.newBuilder(userRewardTaskService.getUserRewardTask(userinfo.getId(), roomInfo.getIndex()));
				builder.setStatus(REWARDTASK_STATUS.CANREWARD_VALUE);
				userRewardTaskService.updateUserRewardTask(userinfo.getId(), builder.build());
			}
			activityService.completeRewardTask(userinfo.getId(), rewardTask.getType());
		}
		
		rewardTaskRedisService.delUserRewardTaskRoom(user, index);
		
		return SuccessConst.BOSS_SUBMIT_SUCCESS;
	}
	
	public List<UserInfo> getNotEnoughPropUser(UserRewardTaskRoom room) {
		List<UserInfo> userInfoList = new ArrayList<UserInfo>();
		EventConfig event = levelRedisService.getEvent(room.getEventid());
		if(!event.hasCost())
			return userInfoList;
		for (RoomInfo roomInfo : room.getRoomInfoList()) {
			UserInfo userinfo = roomInfo.getUser();

			UserBean other = userService.getUserOther(userinfo.getId());
			int costId = costService.canCost(other, event.getCost());
			if (costId == 0) {
				userInfoList.add(userinfo);
			}
		}
		
		return userInfoList;
	}
	
	public UserRewardTaskRoom getUserRoom(UserBean user, int index) {
		UserRewardTask ut = userRewardTaskService.getUserRewardTask(user, index);
		if (ut == null || ut.getRoomInfo() == null)
			return null;
		
		UserRewardTaskRoom room = rewardTaskRedisService.getUserRewardTaskRoom(ut.getRoomInfo().getUser().getId(), ut.getRoomInfo().getIndex());
		
		return room;
	}
	
	public UserRewardTaskRoom inviteFightRewardTask(UserBean user, long createUserId, List<Long> userIds, int id, int index, UserRewardTask.Builder userRewardTaskBuilder) {
		if (userIds.isEmpty()) {//接收邀请
			UserRewardTaskRoom room = rewardTaskRedisService.getUserRewardTaskRoom(createUserId, index);
			if (room == null)
				return null;
			UserRewardTask rewardTask = userRewardTaskService.getUserRewardTask(createUserId, index);
			if (rewardTask == null)
				return null;
			
			UserRewardTaskRoom.Builder builder = UserRewardTaskRoom.newBuilder(room);
			
			Map<Integer, UserRewardTask> map = userRewardTaskService.getUserRewardTaskList(user);
			int rewardTaskCount = 0;
			for (UserRewardTask urt : map.values()) {
				if (urt.getStatus() != REWARDTASK_STATUS.END_VALUE)
					rewardTaskCount++;
			}
			if (rewardTaskCount >= 20) {
				builder.setStatus(REWARDTASK_STATUS.LIMIT_VALUE);
				return builder.build();
			}
			
			
			for (RoomInfo roomInfo : builder.getRoomInfoList()) {
				if (roomInfo.getUser().getId() == user.getId()) {
					builder.setStatus(REWARDTASK_STATUS.HAS_IN_VALUE);
					return builder.build();
				}
			}
			if (builder.getRoomInfoCount() >= rewardTask.getTask().getMembers()) {
				builder.setStatus(REWARDTASK_STATUS.FULL_VALUE);//房间人数已满
				return builder.build();
			}
			
			userRewardTaskBuilder.mergeFrom(rewardTask);
			user.setRewardTaskIndex(Math.max(20, user.getRewardTaskIndex() + 1));
			userRewardTaskBuilder.setIndex(user.getRewardTaskIndex());
			for (int i = 0; i < builder.getRoomInfoCount(); ++i) {
				if (builder.getRoomInfo(i).getUser().getId() == createUserId) {
					userRewardTaskBuilder.setRoomInfo(builder.getRoomInfo(i));
					break;
				}
					
			}
			userRewardTaskService.updateUserRewardTask(user, userRewardTaskBuilder.build());
			
			RoomInfo.Builder roomInfoBuilder = RoomInfo.newBuilder();
			roomInfoBuilder.setIndex(rewardTask.getIndex());
			roomInfoBuilder.setUser(userService.getCache(user.getServerId(), user.getId()));
			builder.addRoomInfo(roomInfoBuilder.build());
			rewardTaskRedisService.setUserRewardTaskRoom(builder.build());
			
			return builder.build();
		} else { //邀请别人
			UserRewardTaskRoom room = rewardTaskRedisService.getUserRewardTaskRoom(createUserId, index);
			if (room == null) {
				return null;
			}
			
			for (long userId : userIds) {
				sendInviteMail(user, userId, index);
			}
			
			return room;
		}
	}
	
	public ResultConst quitRoom(UserBean user, long quitUserId, int index, UserRewardTask.Builder rewardTaskBuilder, UserRewardTaskRoom.Builder builder) {
		UserRewardTask userRewardTask = userRewardTaskService.getUserRewardTask(user.getId(), index);
		
		if (userRewardTask.getRoomInfo() == null)
			return ErrorConst.ROOM_IS_NOT_EXIST_ERROR;
		
		UserRewardTaskRoom room = rewardTaskRedisService.getUserRewardTaskRoom(userRewardTask.getRoomInfo().getUser().getId(), userRewardTask.getRoomInfo().getIndex());
		if (room == null)
			return ErrorConst.ROOM_IS_NOT_EXIST_ERROR;
		
		if (user.getId() != quitUserId && room.getCreateUserId() != user.getId()) {
			return ErrorConst.BOSS_ROOM_CAN_NOT_QUIT_OTHER;
		}
		
		rewardTaskBuilder.mergeFrom(userRewardTask);
		builder.mergeFrom(room);
		if (builder.getCreateUserId() == user.getId() && user.getId() == quitUserId) {
			rewardTaskRedisService.delUserRewardTaskRoom(user, builder.getIndex());
			for (RoomInfo roomInfo :builder.getRoomInfoList()) {
				if (roomInfo.getUser().getId() != user.getId()) {
					UserRewardTask.Builder userRewardTaskBuilder = UserRewardTask.newBuilder(userRewardTaskService.getUserRewardTask(roomInfo.getUser().getId(), roomInfo.getIndex()));
					userRewardTaskBuilder.setStatus(REWARDTASK_STATUS.END_VALUE);
					userRewardTaskService.updateUserRewardTask(roomInfo.getUser().getId(), userRewardTaskBuilder.build());
				}
			}
			builder.clearRoomInfo();
			rewardTaskBuilder.clearRoomInfo();
		} else {
			for (int i = 0; i < builder.getRoomInfoCount(); ++i) {
				if (builder.getRoomInfo(i).getUser().getId() == quitUserId) {
					if (quitUserId != user.getId()) {
						UserRewardTask.Builder userRewardTaskBuilder = UserRewardTask.newBuilder(userRewardTaskService.getUserRewardTask(quitUserId, builder.getRoomInfo(i).getIndex()));
						userRewardTaskBuilder.clearRoomInfo();
						userRewardTaskBuilder.setStatus(REWARDTASK_STATUS.END_VALUE);
						userRewardTaskService.updateUserRewardTask(quitUserId, userRewardTaskBuilder.build());	
					}
					builder.removeRoomInfo(i);
					break;
				}
			}
			rewardTaskRedisService.setUserRewardTaskRoom(builder.build());
			if (quitUserId == user.getId()) {
				rewardTaskBuilder.clearRoomInfo();
				rewardTaskBuilder.setStatus(REWARDTASK_STATUS.END_VALUE);
			}
		}
		
		userRewardTaskService.updateUserRewardTask(user, rewardTaskBuilder.build());
		
		return SuccessConst.BOSS_ROOM_QUIT_SUCCESS;
	}
	
	public ResultConst giveupRewardtask(UserBean user, int index, UserRewardTask.Builder rewardTaskBuilder, UserRewardTaskRoom.Builder builder) {
		UserRewardTask userRewardTask = userRewardTaskService.getUserRewardTask(user.getId(), index);
		
		if (userRewardTask.hasRoomInfo() && userRewardTask.getRoomInfo() != null) {
			ResultConst ret = quitRoom(user, user.getId(), index, rewardTaskBuilder, builder);
			if (ret instanceof ErrorConst)
				return ret;
		}
		
		rewardTaskBuilder.mergeFrom(userRewardTask);
		rewardTaskBuilder.setStatus(REWARDTASK_STATUS.END_VALUE);
		
		userRewardTaskService.updateUserRewardTask(user, rewardTaskBuilder.build());
		
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.USERID, "" + user.getId());
		params.put(LogString.SERVERID, "" + user.getServerId());
		params.put(LogString.BOSSID, "" + userRewardTask.getTask().getEventid());
		if(userRewardTask.hasRoomInfo()) {
			params.put(LogString.TEAM, "1");
		} else {
			params.put(LogString.TEAM, "0");
		}
		params.put(LogString.RESULT, "3");
		params.put(LogString.DPS, "0");
		UserLevelBean userLevel = userLevelService.getUserLevel(user);
		params.put(LogString.LEVEL, "" + userLevel.getUnlockDaguan());
		params.put(LogString.ZHANLI, "" + user.getZhanli());
		params.put(LogString.VIPLEVEL, "" + user.getVip());
		
		logService.sendLog(params, LogString.LOGTYPE_REWARDBOSS);
		
		return SuccessConst.REWARDTASK_GIVEUP_SUCCESS;
	}
	
	public MultiReward.Builder getRewardList(UserBean user, int index, UserRewardTask.Builder builder) {
		MultiReward.Builder rewards = MultiReward.newBuilder();
		UserRewardTask userRewardTask = userRewardTaskService.getUserRewardTask(user.getId(), index);
		if (userRewardTask.getStatus() == REWARDTASK_STATUS.CANREWARD_VALUE) {
			builder.mergeFrom(userRewardTask);
			builder.setStatus(REWARDTASK_STATUS.END_VALUE);
			userRewardTaskService.updateUserRewardTask(user, builder.build());
			
			Map<String, String> params = new HashMap<String, String>();
			params.put(LogString.USERID, "" + user.getId());
			params.put(LogString.SERVERID, "" + user.getServerId());
			params.put(LogString.BOSSID, "" + userRewardTask.getTask().getEventid());
			params.put(LogString.TEAM, "2");
			params.put(LogString.RESULT, "2");
			params.put(LogString.DPS, "0");
			UserLevelBean userLevel = userLevelService.getUserLevel(user);
			params.put(LogString.LEVEL, "" + userLevel.getUnlockDaguan());
			params.put(LogString.ZHANLI, "" + user.getZhanli());
			params.put(LogString.VIPLEVEL, "" + user.getVip());
			
			logService.sendLog(params, LogString.LOGTYPE_REWARDBOSS);
			
			EventConfig event = levelRedisService.getEvent(userRewardTask.getTask().getEventid());
			rewards.addAllLoot(userRewardTask.getTask().getLootlistList());
			rewards.addAllLoot(event.getLootlistList());
			return rewards;
		}
		
		return rewards;
	}
	
//	private List<RewardBean> getBossloot(int id, UserBean user, int team, int dps) {
//		int itemid1 = 0;
//		int itemcount1 = 0;
//		int itemid2 = 0;
//		int itemcount2 = 0;
//		int itemid3 = 0;
//		int itemcount3 = 0;
//		int itemid4 = 0;
//		int itemcount4 = 0;
//		List<RewardBean> rewardList = new ArrayList<RewardBean>();
//		BosslootGroup bosslootGroup = rewardTaskRedisService.getBosslootGroup(id);
//		for (int i = 0; i < bosslootGroup.getOrderList().size(); ++i) {
//			Bossloot bossloot = bosslootGroup.getOrder(i);
//			int randomWeight = RandomUtils.nextInt(bossloot.getWeightall()) + 1;
//			if (randomWeight <= bossloot.getWeight1()) {
//				if (i == 0) {
//					itemid1 = bossloot.getItemid1();
//					itemcount1 = bossloot.getItemcount1();
//				} else if (i == 1) {
//					itemid2 = bossloot.getItemid1();
//					itemcount2 = bossloot.getItemcount1();
//				} else if (i == 2) {
//					itemid3 = bossloot.getItemid1();
//					itemcount3 = bossloot.getItemcount1();
//				} else if (i == 3) {
//					itemid4 = bossloot.getItemid1();
//					itemcount4 = bossloot.getItemcount1();
//				}
//				rewardList.add(RewardBean.init(bossloot.getItemid1(), bossloot.getItemcount1()));
//				continue;
//			}
//			
//			randomWeight -= bossloot.getWeight1();
//			if (randomWeight <= bossloot.getWeight2()) {
//				if (i == 0) {
//					itemid1 = bossloot.getItemid2();
//					itemcount1 = bossloot.getItemcount2();
//				} else if (i == 1) {
//					itemid2 = bossloot.getItemid2();
//					itemcount2 = bossloot.getItemcount2();
//				} else if (i == 2) {
//					itemid3 = bossloot.getItemid2();
//					itemcount3 = bossloot.getItemcount2();
//				} else if (i == 3) {
//					itemid4 = bossloot.getItemid2();
//					itemcount4 = bossloot.getItemcount2();
//				}
//				rewardList.add(RewardBean.init(bossloot.getItemid2(), bossloot.getItemcount2()));
//				continue;
//			}
//			
//			randomWeight -= bossloot.getWeight2();
//			if (randomWeight <= bossloot.getWeight3()) {
//				if (i == 0) {
//					itemid1 = bossloot.getItemid3();
//					itemcount1 = bossloot.getItemcount3();
//				} else if (i == 1) {
//					itemid2 = bossloot.getItemid3();
//					itemcount2 = bossloot.getItemcount3();
//				} else if (i == 2) {
//					itemid3 = bossloot.getItemid3();
//					itemcount3 = bossloot.getItemcount3();
//				} else if (i == 3) {
//					itemid4 = bossloot.getItemid3();
//					itemcount4 = bossloot.getItemcount3();
//				}
//				rewardList.add(RewardBean.init(bossloot.getItemid3(), bossloot.getItemcount3()));
//				continue;
//			}
//		}
//	
//		UserLevelBean userLevel = userLevelService.getUserLevel(user);
//		if(userLevel != null)
//		logService.sendWorldbossLog(user.getServerId(), user.getId(), id, team, 1, dps, itemid1, itemcount1, 
//				itemid2, itemcount2, itemid3, itemcount3, itemid4, itemcount4, userLevel.getUnlockDaguan(), user.getZhanliMax(), user.getVip());
//		
//		return rewardList;
//	}
	
//	private UserRewardTask initUserRewardTask(UserBean user, int id, int index) {
//		RewardTask rewardTask = rewardTaskRedisService.getRewardTask(id);
//		UserRewardTask.Builder builder = UserRewardTask.newBuilder();
//		builder.setId(id);
//		builder.setType(rewardTask.getType());
//		builder.setIndex(index);
//		builder.setStatus(REWARDTASK_STATUS.LIVE_VALUE);
//		int weightall = 0;
//		for (RewardTaskEnemy enemy : rewardTask.getEnemyList()) {
//			weightall += enemy.getWeight();
//		}
//		
//		for (int i = 0; i < rewardTask.getEnemyCount(); ++i) {
//			RewardTaskEnemy enemy = rewardTask.getEnemy(i);
//			int rand = RandomUtils.nextInt(weightall);
//			if (rand < enemy.getWeight()) {
//				builder.setEnemyid(enemy.getEnemyid());
//				break;
//			}
//			weightall -= enemy.getWeight();
//		}
//		
//		return builder.build();
//	}
	
	private void sendInviteMail(UserBean user, long userId, int id) {
		String content = "邀请你一起伐木boss！";
		MailBean mail = MailBean.buildMail(userId, user, content, MailConst.TYPE_INVITE_FIGHTBOSS_MAIL, id);
		mailService.addMail(mail);
		log.debug("mail is:" + mail.toJson());
	}
}
