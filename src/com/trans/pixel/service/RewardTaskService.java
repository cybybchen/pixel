package com.trans.pixel.service;

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
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.CostItem;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.RewardTaskProto.RewardTask;
import com.trans.pixel.protoc.RewardTaskProto.RewardTaskEnemy;
import com.trans.pixel.protoc.RewardTaskProto.UserRewardTask;
import com.trans.pixel.protoc.RewardTaskProto.UserRewardTask.REWARDTASK_STATUS;
import com.trans.pixel.protoc.RewardTaskProto.UserRewardTaskRoom;
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
	
	public ResultConst zhaohuanTask(UserBean user, int id) {
		UserRewardTask oldTask = userRewardTaskService.getUserRewardTask(user, id);
		if (oldTask != null && oldTask.getStatus() == 0) 
			return ErrorConst.BOSS_HAS_ZHAOHUAN;
		
		UserRewardTask newTask = initUserRewardTask(user, id);
		userRewardTaskService.updateUserRewardTask(user, newTask);
		
		return SuccessConst.USE_PROP;
	}
	
	public ResultConst submitRewardTaskScore(UserBean user, int id, int costId, boolean ret, MultiReward.Builder rewards) {
		RewardTask rewardTask = rewardTaskRedisService.getRewardTask(id);
		UserRewardTask ut = userRewardTaskService.getUserRewardTask(user, id);
		if (ut == null || ut.getStatus() == 1) {
			return ErrorConst.SUBMIT_BOSS_SCORE_ERROR;
		}
		for (CostItem cost : rewardTask.getCostList()) {
			if (cost.getCostid() == costId) {
				if (!costService.costAndUpdate(user, cost.getCostid(), cost.getCostcount()))
					return ErrorConst.NOT_ENOUGH_PROP;
				
				UserRewardTask.Builder builder = UserRewardTask.newBuilder(ut);
				builder.setStatus(1);
				userRewardTaskService.updateUserRewardTask(user, ut);
				
				handleRewardTaskRoom(user, id);
				
				return SuccessConst.BOSS_SUBMIT_SUCCESS;
			}
		}
		
		return ErrorConst.SUBMIT_BOSS_SCORE_ERROR;
	}
	
	public UserRewardTaskRoom createRoom(UserBean user, int id) {
		UserRewardTask ut = userRewardTaskService.getUserRewardTask(user, id);
		if (ut == null || ut.getStatus() != REWARDTASK_STATUS.LIVE_VALUE) {
			return null;
		}
		UserRewardTaskRoom room = rewardTaskRedisService.getUserRewardTaskRoom(user.getId(), id);
		if (room != null)
			return room;
		
		UserRewardTaskRoom.Builder builder = UserRewardTaskRoom.newBuilder();
		builder.setBossId(id);
		builder.setCreateUserId(user.getId());
		builder.addUser(userService.getCache(user.getServerId(), user.getId()));
		
		rewardTaskRedisService.setUserRewardTaskRoom(builder.build());
		
		UserRewardTask.Builder utBuilder = UserRewardTask.newBuilder(ut);
		utBuilder.setCreate(userService.getCache(user.getServerId(), user.getId()));
		userRewardTaskService.updateUserRewardTask(user, utBuilder.build());
		return builder.build();
		
	}
	
	private void handleRewardTaskRoom(UserBean user, int id) {
		UserRewardTaskRoom room = rewardTaskRedisService.getUserRewardTaskRoom(user.getId(), id);
		if (room == null)
			return;
		
		for (UserInfo userinfo : room.getUserList()) {
			if (userinfo.getId() == user.getId())
				continue;
			UserRewardTask.Builder builder = UserRewardTask.newBuilder(userRewardTaskService.getUserRewardTask(userinfo.getId(), id));
			builder.setRoomStatus(REWARDTASK_STATUS.CANREWARD_VALUE);
			userRewardTaskService.updateUserRewardTask(userinfo.getId(), builder.build());
		}
		
		rewardTaskRedisService.delUserRewardTaskRoom(user, id);
	}
	
	public UserRewardTaskRoom getUserRoom(UserBean user, int id) {
		UserRewardTask ut = userRewardTaskService.getUserRewardTask(user, id);
		if (ut == null || ut.getCreate() == null)
			return null;
		
		UserRewardTaskRoom room = rewardTaskRedisService.getUserRewardTaskRoom(ut.getCreate().getId(), id);
		
		return room;
	}
	
	public UserRewardTaskRoom inviteFightRewardTask(UserBean user, long createUserId, List<Long> userIds, int id) {
		if (userIds.isEmpty()) {//接收邀请
			UserRewardTaskRoom room = rewardTaskRedisService.getUserRewardTaskRoom(createUserId, id);
			if (room == null)
				return null;
			
			UserRewardTaskRoom.Builder builder = UserRewardTaskRoom.newBuilder(room);
			RewardTask rewardTask = rewardTaskRedisService.getRewardTask(id);
			if (builder.getUserCount() >= rewardTask.getRenshu()) {
				builder.setStatus(REWARDTASK_STATUS.FULL_VALUE);//房间人数已满
				return builder.build();
			}
			
			builder.addUser(userService.getCache(user.getServerId(), user.getId()));
			rewardTaskRedisService.setUserRewardTaskRoom(builder.build());
			
			UserRewardTask userRewardTask = userRewardTaskService.getUserRewardTask(user, id);
			UserRewardTask.Builder userRewardTaskBuilder = UserRewardTask.newBuilder();
			if (userRewardTask == null) { 
				userRewardTaskBuilder = UserRewardTask.newBuilder(initUserRewardTask(user, id));
				userRewardTaskBuilder.setStatus(REWARDTASK_STATUS.END_VALUE);
			} else {
				userRewardTaskBuilder = UserRewardTask.newBuilder(userRewardTask);
			}
			userRewardTaskBuilder.setCreate(userService.getCache(user.getServerId(), createUserId));
			userRewardTaskService.updateUserRewardTask(user, userRewardTaskBuilder.build());
			
			return builder.build();
		} else { //邀请别人
			UserRewardTaskRoom room = rewardTaskRedisService.getUserRewardTaskRoom(createUserId, id);
			if (room == null) {
				UserRewardTaskRoom.Builder builder = UserRewardTaskRoom.newBuilder();
				builder.setBossId(id);
				builder.setCreateUserId(user.getId());
				builder.addUser(userService.getCache(user.getServerId(), user.getId()));
				
				rewardTaskRedisService.setUserRewardTaskRoom(builder.build());
				room = builder.build();
			}
			
			for (long userId : userIds) {
				sendInviteMail(user, userId, id);
			}
			
			return room;
		}
	}
	
	public ResultConst quitRoom(UserBean user, long quitUserId, int id, UserRewardTask.Builder rewardTaskBuilder, UserRewardTaskRoom.Builder builder) {
		UserRewardTask userRewardTask = userRewardTaskService.getUserRewardTask(user.getId(), id);
		
		if (userRewardTask.getCreate() == null)
			return ErrorConst.ROOM_IS_NOT_EXIST_ERROR;
		
		UserRewardTaskRoom room = rewardTaskRedisService.getUserRewardTaskRoom(userRewardTask.getCreate().getId(), id);
		if (room == null)
			return ErrorConst.ROOM_IS_NOT_EXIST_ERROR;
		
		if (user.getId() != quitUserId && room.getCreateUserId() != user.getId()) {
			return ErrorConst.BOSS_ROOM_CAN_NOT_QUIT_OTHER;
		}
		
		rewardTaskBuilder = UserRewardTask.newBuilder(userRewardTask);
		builder = UserRewardTaskRoom.newBuilder(room);
		if (builder.getCreateUserId() == user.getId() && user.getId() == quitUserId) {
			rewardTaskRedisService.delUserRewardTaskRoom(user, builder.getBossId());
			builder.clearUser();
			rewardTaskBuilder.clearCreate();
		} else {
			for (int i = 0; i < builder.getUserCount(); ++i) {
				if (builder.getUser(i).getId() == quitUserId) {
					builder.removeUser(i);
					UserRewardTask.Builder userRewardTaskBuilder = UserRewardTask.newBuilder(userRewardTaskService.getUserRewardTask(quitUserId, id));
					userRewardTaskBuilder.clearCreate();
					userRewardTaskBuilder.setStatus(REWARDTASK_STATUS.END_VALUE);
					userRewardTaskService.updateUserRewardTask(user, userRewardTaskBuilder.build());
					break;
				}
			}
			rewardTaskRedisService.setUserRewardTaskRoom(builder.build());
			if (quitUserId == user.getId()) {
				rewardTaskBuilder.clearCreate();
				rewardTaskBuilder.setStatus(REWARDTASK_STATUS.END_VALUE);
			}
		}
		
		return SuccessConst.BOSS_ROOM_QUIT_SUCCESS;
	}
	
	private UserRewardTask initUserRewardTask(UserBean user, int id) {
		RewardTask rewardTask = rewardTaskRedisService.getRewardTask(id);
		UserRewardTask.Builder builder = UserRewardTask.newBuilder();
		builder.setId(id);
		builder.setType(rewardTask.getType());
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
