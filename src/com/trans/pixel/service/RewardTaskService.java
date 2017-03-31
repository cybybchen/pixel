package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.RewardTaskProto.RewardTask;
import com.trans.pixel.protoc.RewardTaskProto.RewardTaskEnemy;
import com.trans.pixel.protoc.RewardTaskProto.RoomInfo;
import com.trans.pixel.protoc.RewardTaskProto.UserRewardTask;
import com.trans.pixel.protoc.RewardTaskProto.UserRewardTask.REWARDTASK_STATUS;
import com.trans.pixel.protoc.RewardTaskProto.UserRewardTaskRoom;
import com.trans.pixel.protoc.UnionProto.Bossloot;
import com.trans.pixel.protoc.UnionProto.BosslootGroup;
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
	
	public ResultConst zhaohuanTask(UserBean user, int id) {
		UserRewardTask oldTask = userRewardTaskService.getUserRewardTask(user, id);
		if (oldTask != null && oldTask.getStatus() == 0) 
			return ErrorConst.BOSS_HAS_ZHAOHUAN;
		
		user.setRewardTaskIndex(user.getRewardTaskIndex() + 1);
		userService.updateUser(user);
		
		UserRewardTask.Builder newTask = UserRewardTask.newBuilder(initUserRewardTask(user, id, user.getRewardTaskIndex()));
		userRewardTaskService.updateUserRewardTask(user, newTask.build());
		
		return SuccessConst.USE_PROP;
	}
	
	public ResultConst submitRewardTaskScore(UserBean user, int index, boolean ret, MultiReward.Builder rewards, UserInfo.Builder errorUser, List<UserEquipBean> userEquipList) {
		UserRewardTask ut = userRewardTaskService.getUserRewardTask(user, index);
		if (ut == null || ut.getStatus() != REWARDTASK_STATUS.LIVE_VALUE || ut.getEnemyid() == 0) {
			return ErrorConst.SUBMIT_BOSS_SCORE_ERROR;
		}
		RewardTask rewardTask = rewardTaskRedisService.getRewardTask(ut.getId());
		int costId = costService.canCostOnly(user, rewardTask.getCostList());
		if (costId == 0) {
			errorUser.mergeFrom(user.build());
			return ErrorConst.NOT_ENOUGH_PROP;
		}
		
		ResultConst result = handleRewardTaskRoom(user, index, rewardTask, errorUser);
		if (result instanceof ErrorConst)
			return result;
		
		costService.costOnly(user, rewardTask.getCostList());
		UserRewardTask.Builder builder = UserRewardTask.newBuilder(ut);
		builder.setStatus(REWARDTASK_STATUS.END_VALUE);
		userRewardTaskService.updateUserRewardTask(user, builder.build());
		
		userEquipList.add(userEquipService.selectUserEquip(user.getId(), costId));
		rewards.addAllLoot(RewardBean.buildRewardInfoList(getBossloot(ut.getEnemyid(), user, 0 , 0)));
		
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
		builder.setBossId(ut.getId());
		builder.setCreateUserId(user.getId());
		builder.setIndex(ut.getIndex());
		RoomInfo.Builder roomInfoBuilder = RoomInfo.newBuilder();
		roomInfoBuilder.setIndex(ut.getIndex());
		roomInfoBuilder.setUser(create);
		builder.addRoomInfo(roomInfoBuilder.build());
		builder.setEnemyId(ut.getEnemyid());
		rewardTaskRedisService.setUserRewardTaskRoom(builder.build());
		
		UserRewardTask.Builder utBuilder = UserRewardTask.newBuilder(ut);
		utBuilder.setRoomInfo(roomInfoBuilder.build());
		userRewardTaskService.updateUserRewardTask(user, utBuilder.build());
		
		return builder.build();
		
	}
	
	private ResultConst handleRewardTaskRoom(UserBean user, int index, RewardTask rewardTask, UserInfo.Builder errorUser) {
		UserRewardTaskRoom room = rewardTaskRedisService.getUserRewardTaskRoom(user.getId(), index);
		if (room == null)
			return SuccessConst.BOSS_SUBMIT_SUCCESS;;
		
		for (RoomInfo roomInfo : room.getRoomInfoList()) {
			UserInfo userinfo = roomInfo.getUser();
			if (userinfo.getId() == user.getId())
				continue;
			
			UserBean other = userService.getOther(userinfo.getId());
			int costId = costService.canCostOnly(other, rewardTask.getCostList());
			if (costId == 0) {
				errorUser.mergeFrom(userinfo);
				return ErrorConst.NOT_ENOUGH_PROP;
			}
		}
		
		for (RoomInfo roomInfo : room.getRoomInfoList()) {
			UserInfo userinfo = roomInfo.getUser();
			if (userinfo.getId() != user.getId()) {
				costService.costOnly(userinfo.getId(), rewardTask.getCostList());
				UserRewardTask.Builder builder = UserRewardTask.newBuilder(userRewardTaskService.getUserRewardTask(userinfo.getId(), roomInfo.getIndex()));
				builder.setStatus(REWARDTASK_STATUS.CANREWARD_VALUE);
				userRewardTaskService.updateUserRewardTask(userinfo.getId(), builder.build());
			}
			activityService.completeRewardTask(userinfo.getId(), rewardTask.getType());
		}
		
		rewardTaskRedisService.delUserRewardTaskRoom(user, index);
		
		return SuccessConst.BOSS_SUBMIT_SUCCESS;
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
			
			UserRewardTaskRoom.Builder builder = UserRewardTaskRoom.newBuilder(room);
			for (RoomInfo roomInfo : builder.getRoomInfoList()) {
				if (roomInfo.getUser().getId() == user.getId()) {
					builder.setStatus(REWARDTASK_STATUS.HAS_IN_VALUE);
					return builder.build();
				}
			}
			RewardTask rewardTask = rewardTaskRedisService.getRewardTask(room.getBossId());
			if (builder.getRoomInfoCount() >= rewardTask.getRenshu()) {
				builder.setStatus(REWARDTASK_STATUS.FULL_VALUE);//房间人数已满
				return builder.build();
			}
			
			user.setRewardTaskIndex(user.getRewardTaskIndex() + 1);
			userRewardTaskBuilder.mergeFrom(initUserRewardTask(user, room.getBossId(), user.getRewardTaskIndex()));
			userRewardTaskBuilder.setEnemyid(builder.getEnemyId());
			for (int i = 0; i < builder.getRoomInfoCount(); ++i) {
				if (builder.getRoomInfo(i).getUser().getId() == createUserId) {
					userRewardTaskBuilder.setRoomInfo(builder.getRoomInfo(i));
					break;
				}
					
			}
			userRewardTaskService.updateUserRewardTask(user, userRewardTaskBuilder.build());
			
			RoomInfo.Builder roomInfoBuilder = RoomInfo.newBuilder();
			roomInfoBuilder.setIndex(userRewardTaskBuilder.getIndex());
			roomInfoBuilder.setUser(userService.getCache(user.getServerId(), user.getId()));
			builder.addRoomInfo(roomInfoBuilder.build());
			rewardTaskRedisService.setUserRewardTaskRoom(builder.build());
			
			return builder.build();
		} else { //邀请别人
			UserRewardTaskRoom room = rewardTaskRedisService.getUserRewardTaskRoom(createUserId, index);
			if (room == null) {
//				UserRewardTaskRoom.Builder builder = UserRewardTaskRoom.newBuilder();
//				builder.setBossId(id);
//				builder.setCreateUserId(user.getId());
//				builder.addUser(userService.getCache(user.getServerId(), user.getId()));
//				
//				rewardTaskRedisService.setUserRewardTaskRoom(builder.build());
//				room = builder.build();
				
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
	
	public List<RewardBean> getRewardList(UserBean user, int index, UserRewardTask.Builder builder) {
		UserRewardTask userRewardTask = userRewardTaskService.getUserRewardTask(user.getId(), index);
		if (userRewardTask.getStatus() == REWARDTASK_STATUS.CANREWARD_VALUE) {
			builder.mergeFrom(userRewardTask);
			builder.setStatus(REWARDTASK_STATUS.END_VALUE);
			userRewardTaskService.updateUserRewardTask(user, builder.build());
			
			return getBossloot(userRewardTask.getEnemyid(), user, 0, 0);
		}
		
		return new ArrayList<RewardBean>();
	}
	
	private List<RewardBean> getBossloot(int id, UserBean user, int team, int dps) {
		int itemid1 = 0;
		int itemcount1 = 0;
		int itemid2 = 0;
		int itemcount2 = 0;
		int itemid3 = 0;
		int itemcount3 = 0;
		int itemid4 = 0;
		int itemcount4 = 0;
		List<RewardBean> rewardList = new ArrayList<RewardBean>();
		BosslootGroup bosslootGroup = rewardTaskRedisService.getBosslootGroup(id);
		for (int i = 0; i < bosslootGroup.getLootList().size(); ++i) {
			Bossloot bossloot = bosslootGroup.getLoot(i);
			int randomWeight = RandomUtils.nextInt(bossloot.getWeightall()) + 1;
			if (randomWeight <= bossloot.getWeight1()) {
				if (i == 0) {
					itemid1 = bossloot.getItemid1();
					itemcount1 = bossloot.getItemcount1();
				} else if (i == 1) {
					itemid2 = bossloot.getItemid1();
					itemcount2 = bossloot.getItemcount1();
				} else if (i == 2) {
					itemid3 = bossloot.getItemid1();
					itemcount3 = bossloot.getItemcount1();
				} else if (i == 3) {
					itemid4 = bossloot.getItemid1();
					itemcount4 = bossloot.getItemcount1();
				}
				rewardList.add(RewardBean.init(bossloot.getItemid1(), bossloot.getItemcount1()));
				continue;
			}
			
			randomWeight -= bossloot.getWeight1();
			if (randomWeight <= bossloot.getWeight2()) {
				if (i == 0) {
					itemid1 = bossloot.getItemid2();
					itemcount1 = bossloot.getItemcount2();
				} else if (i == 1) {
					itemid2 = bossloot.getItemid2();
					itemcount2 = bossloot.getItemcount2();
				} else if (i == 2) {
					itemid3 = bossloot.getItemid2();
					itemcount3 = bossloot.getItemcount2();
				} else if (i == 3) {
					itemid4 = bossloot.getItemid2();
					itemcount4 = bossloot.getItemcount2();
				}
				rewardList.add(RewardBean.init(bossloot.getItemid2(), bossloot.getItemcount2()));
				continue;
			}
			
			randomWeight -= bossloot.getWeight2();
			if (randomWeight <= bossloot.getWeight3()) {
				if (i == 0) {
					itemid1 = bossloot.getItemid3();
					itemcount1 = bossloot.getItemcount3();
				} else if (i == 1) {
					itemid2 = bossloot.getItemid3();
					itemcount2 = bossloot.getItemcount3();
				} else if (i == 2) {
					itemid3 = bossloot.getItemid3();
					itemcount3 = bossloot.getItemcount3();
				} else if (i == 3) {
					itemid4 = bossloot.getItemid3();
					itemcount4 = bossloot.getItemcount3();
				}
				rewardList.add(RewardBean.init(bossloot.getItemid3(), bossloot.getItemcount3()));
				continue;
			}
		}
	
		UserLevelBean userLevel = userLevelService.getUserLevel(user);
		if(userLevel != null)
		logService.sendWorldbossLog(user.getServerId(), user.getId(), id, team, 1, dps, itemid1, itemcount1, 
				itemid2, itemcount2, itemid3, itemcount3, itemid4, itemcount4, userLevel.getUnlockDaguan(), user.getZhanliMax(), user.getVip());
		
		return rewardList;
	}
	
	private UserRewardTask initUserRewardTask(UserBean user, int id, int index) {
		RewardTask rewardTask = rewardTaskRedisService.getRewardTask(id);
		UserRewardTask.Builder builder = UserRewardTask.newBuilder();
		builder.setId(id);
		builder.setType(rewardTask.getType());
		builder.setIndex(index);
		builder.setStatus(REWARDTASK_STATUS.LIVE_VALUE);
		int weightall = 0;
		for (RewardTaskEnemy enemy : rewardTask.getEnemyList()) {
			weightall += enemy.getWeight();
		}
		
		for (int i = 0; i < rewardTask.getEnemyCount(); ++i) {
			RewardTaskEnemy enemy = rewardTask.getEnemy(i);
			int rand = RandomUtils.nextInt(weightall);
			if (rand < enemy.getWeight()) {
				builder.setEnemyid(enemy.getEnemyid());
				break;
			}
			weightall -= enemy.getWeight();
		}
		
		return builder.build();
	}
	
	private void sendInviteMail(UserBean user, long userId, int id) {
		String content = "邀请你一起伐木boss！";
		MailBean mail = MailBean.buildMail(userId, user.getId(), user.getVip(), user.getIcon(), user.getUserName(), content, MailConst.TYPE_INVITE_FIGHTBOSS_MAIL, id);
		mailService.addMail(mail);
		log.debug("mail is:" + mail.toJson());
	}
}
