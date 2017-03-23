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
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.model.userinfo.UserPropBean;
import com.trans.pixel.protoc.Base.CostItem;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.RewardTaskProto.RewardTask;
import com.trans.pixel.protoc.RewardTaskProto.RewardTaskEnemy;
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
	
	public ResultConst zhaohuanTask(UserBean user, int id) {
		UserRewardTask oldTask = userRewardTaskService.getUserRewardTask(user, id);
		if (oldTask != null && oldTask.getStatus() == 0) 
			return ErrorConst.BOSS_HAS_ZHAOHUAN;
		
		UserRewardTask newTask = initUserRewardTask(user, id);
		userRewardTaskService.updateUserRewardTask(user, newTask);
		
		return SuccessConst.USE_PROP;
	}
	
	public ResultConst submitRewardTaskScore(UserBean user, int id, boolean ret, MultiReward.Builder rewards, UserInfo.Builder errorUser, List<UserPropBean> userPropList) {
		RewardTask rewardTask = rewardTaskRedisService.getRewardTask(id);
		UserRewardTask ut = userRewardTaskService.getUserRewardTask(user, id);
		if (ut == null || ut.getStatus() == 1) {
			return ErrorConst.SUBMIT_BOSS_SCORE_ERROR;
		}
		
		int costId = costService.canCostOnly(user, rewardTask.getCostList());
		if (costId == 0) {
			errorUser = UserInfo.newBuilder(user.build());
			return ErrorConst.NOT_ENOUGH_PROP;
		}
		
		ResultConst result = handleRewardTaskRoom(user, id, rewardTask.getCostList(), errorUser);
		if (result instanceof ErrorConst)
			return result;
		
		UserRewardTask.Builder builder = UserRewardTask.newBuilder(ut);
		builder.setStatus(1);
		userRewardTaskService.updateUserRewardTask(user, ut);
		userPropList.add(UserPropBean.initUserProp(user.getId(), costId, ""));
		
		return SuccessConst.BOSS_SUBMIT_SUCCESS;
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
	
	private ResultConst handleRewardTaskRoom(UserBean user, int id, List<CostItem> costList, UserInfo.Builder errorUser) {
		UserRewardTaskRoom room = rewardTaskRedisService.getUserRewardTaskRoom(user.getId(), id);
		if (room == null)
			return ErrorConst.SUBMIT_BOSS_SCORE_ERROR;
		
		for (UserInfo userinfo : room.getUserList()) {
			if (userinfo.getId() == user.getId())
				continue;
			
			UserBean other = userService.getOther(userinfo.getId());
			int costId = costService.canCostOnly(other, costList);
			if (costId == 0) {
				errorUser = UserInfo.newBuilder(userinfo);
				return ErrorConst.NOT_ENOUGH_PROP;
			}
		}
		
		for (UserInfo userinfo : room.getUserList()) {
			costService.costOnly(user, costList);
			UserRewardTask.Builder builder = UserRewardTask.newBuilder(userRewardTaskService.getUserRewardTask(userinfo.getId(), id));
			builder.setRoomStatus(REWARDTASK_STATUS.CANREWARD_VALUE);
			userRewardTaskService.updateUserRewardTask(userinfo.getId(), builder.build());
		}
		
		rewardTaskRedisService.delUserRewardTaskRoom(user, id);
		
		return SuccessConst.BOSS_SUBMIT_SUCCESS;
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
	
	public List<RewardBean> getRewardList(UserBean user, int id) {
		UserRewardTask userRewardTask = userRewardTaskService.getUserRewardTask(user.getId(), id);
		if (userRewardTask.getRoomStatus() == REWARDTASK_STATUS.CANREWARD_VALUE) {
			UserRewardTask.Builder builder = UserRewardTask.newBuilder(userRewardTask);
			builder.setRoomStatus(REWARDTASK_STATUS.END_VALUE);
			userRewardTaskService.updateUserRewardTask(user, builder.build());
			
			return getBossloot(id, user, 0, 0);
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
