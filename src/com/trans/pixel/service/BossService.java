package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import com.trans.pixel.protoc.Commands.BossGroupRecord;
import com.trans.pixel.protoc.Commands.BossRecord;
import com.trans.pixel.protoc.Commands.BossRoomRecord;
import com.trans.pixel.protoc.Commands.Bossgroup;
import com.trans.pixel.protoc.Commands.Bossloot;
import com.trans.pixel.protoc.Commands.BosslootGroup;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.service.redis.BossRedisService;
import com.trans.pixel.service.redis.LevelRedisService;
import com.trans.pixel.utils.DateUtil;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class BossService {

	private static final Logger log = LoggerFactory.getLogger(BossService.class);
	
	@Resource
	private BossRedisService bossRedisService;
	@Resource
	private LogService logService;
	@Resource
	private LevelRedisService userLevelService;
	@Resource
	private UserService userService;
	@Resource
	private MailService mailService;
	
	public List<RewardBean> submitBosskill(UserBean user, int groupId, int bossId) {
		BossGroupRecord userBossGroup = bossRedisService.getZhaohuanBoss(user);
		if (userBossGroup != null && userBossGroup.getGroupId() == groupId) {
//			List<BossRecord> bossRecordList = new ArrayList<BossRecord>(userBossGroup.getBossRecordList());
			for (int i = 0; i < userBossGroup.getBossRecordList().size(); ++i) {
				BossRecord bossRecord = userBossGroup.getBossRecord(i);
				BossRecord.Builder builder = BossRecord.newBuilder(bossRecord);
				if (bossRecord.getBossId() == bossId && bossRecord.getEndTime().isEmpty()) {
					int userHasKillCount = bossRedisService.getBosskillCount(user.getId(), groupId, bossId);
					Bossgroup bossGroup = bossRedisService.getBossgroup(groupId);
					if (userHasKillCount >= bossGroup.getCount())
						break;
					builder.setEndTime(DateUtil.getCurrentDateString());
					BossGroupRecord.Builder bossgroupBuilder = BossGroupRecord.newBuilder(userBossGroup);
//					bossgroupBuilder.clearBossRecord();
					bossgroupBuilder.setBossRecord(i, builder.build());
//					bossgroupBuilder.addAllBossRecord(bossRecordList);
					bossRedisService.zhaohuanBoss(user, bossgroupBuilder.build());
					
					bossRedisService.addBosskillCount(user.getId(), groupId, bossId);
					return getBossloot(bossId, user, 0 , 0);
				}
			}
			
			return null;
		}
		BossGroupRecord bossGroupRecord = bossRedisService.getBossGroupRecord(user.getServerId(), groupId);
		if (bossGroupRecord != null) {
			for (BossRecord bossRecord :bossGroupRecord.getBossRecordList()) {
				if (bossRecord.getBossId() == bossId) {
					int userHasKillCount = bossRedisService.getBosskillCount(user.getId(), groupId, bossId);
					Bossgroup bossGroup = bossRedisService.getBossgroup(groupId);
					if (userHasKillCount >= bossGroup.getCount())
						break;
					
					bossRedisService.addBosskillCount(user.getId(), groupId, bossId);
					
					return getBossloot(bossId, user, 0, 0);
				}
			}
		}
		
		return new ArrayList<RewardBean>();
	}
	
	private List<RewardBean> getBossloot(int bossId, UserBean user, int team, int dps) {
		int itemid1 = 0;
		int itemcount1 = 0;
		int itemid2 = 0;
		int itemcount2 = 0;
		int itemid3 = 0;
		int itemcount3 = 0;
		int itemid4 = 0;
		int itemcount4 = 0;
		List<RewardBean> rewardList = new ArrayList<RewardBean>();
		BosslootGroup bosslootGroup = bossRedisService.getBosslootGroup(bossId);
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
	
		UserLevelBean userLevel = userLevelService.getUserLevel(user.getId());
		if(userLevel != null)
		logService.sendWorldbossLog(user.getServerId(), user.getId(), bossId, team, 1, dps, itemid1, itemcount1, 
				itemid2, itemcount2, itemid3, itemcount3, itemid4, itemcount4, userLevel.getUnlockDaguan(), user.getZhanliMax(), user.getVip());
		
		return rewardList;
	}
	
	public List<BossGroupRecord> getBossGroupRecord(UserBean user) {
		List<BossGroupRecord> list = bossRedisService.getBossGroupRecordList(user.getServerId());
		if (list.size() == 0) {
			list = randomDailyBoss();
			bossRedisService.setBossgroupRecord(list, user.getServerId());
		}
		
		BossGroupRecord bossGroupRecord = bossRedisService.getZhaohuanBoss(user);
		if (bossGroupRecord != null)
			list.add(bossGroupRecord);
		
		List<BossGroupRecord> userBossGroupList = new ArrayList<BossGroupRecord>();
		for (BossGroupRecord bossgroup : list) {
			BossGroupRecord.Builder builder = BossGroupRecord.newBuilder();
			List<BossRecord> bossRecordList = new ArrayList<BossRecord>();
			Bossgroup group = bossRedisService.getBossgroup(bossgroup.getGroupId());
			for (BossRecord bossRecord : bossgroup.getBossRecordList()) {
				if (bossgroup.getGroupId() == 4 && bossRedisService.getBosskillCount(user.getId(), bossgroup.getGroupId(), bossRecord.getBossId()) >= group.getCount()) {
					continue;
				}
				BossRecord.Builder bossRecordBuilder = BossRecord.newBuilder(bossRecord);
				bossRecordBuilder.setCount(bossRedisService.getBosskillCount(user.getId(), bossgroup.getGroupId(), bossRecord.getBossId()));
				bossRecordList.add(bossRecordBuilder.build());
			}
			builder.addAllBossRecord(bossRecordList);
			builder.setGroupId(bossgroup.getGroupId());
			userBossGroupList.add(builder.build());
		}
		
		return userBossGroupList;
		
	}
	
	private List<BossGroupRecord> randomDailyBoss() {
		List<BossGroupRecord> list = new ArrayList<BossGroupRecord>();
		Map<String, Bossgroup> map = bossRedisService.getBossgroupConfig();
		Iterator<Entry<String, Bossgroup>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Bossgroup> entry = it.next();
			BossGroupRecord.Builder builder = BossGroupRecord.newBuilder();
			List<BossRecord> bossRecordList = new ArrayList<BossRecord>();
			List<Integer> bossIdList = new ArrayList<Integer>();
			Bossgroup bossgroup = entry.getValue();
			if (bossgroup.getTime() != 0)
				continue;
			while (bossRecordList.size() < bossgroup.getBosscount()) {
				BossRecord.Builder bossRecord = BossRecord.newBuilder();
				int random = RandomUtils.nextInt(bossgroup.getBossCount());
				int bossId = bossgroup.getBoss(random).getBossid();
				if (bossIdList.contains(bossId))
					continue;
				bossRecord.setBossId(bossId);
				bossIdList.add(bossRecord.getBossId());
				bossRecordList.add(bossRecord.build());
			}
			builder.addAllBossRecord(bossRecordList);
			builder.setGroupId(TypeTranslatedUtil.stringToInt(entry.getKey()));
			list.add(builder.build());
		}
		
		return list;
	}
	
	public BossRoomRecord startBossRoom(UserBean user) {
		if (user.getBossRoomUserId() != user.getId())
			return null;
		BossRoomRecord record = bossRedisService.getBossRoomRecord(user, user.getId());
		if (record == null)
			return null;
		
		BossRoomRecord.Builder builder = BossRoomRecord.newBuilder(record);
		builder.setStatus(1);
		bossRedisService.setBossRoomRecord(builder.build());
		return builder.build();
	}
	
	public BossRoomRecord inviteFightBoss(UserBean user, long createUserId, List<Long> userIds, int groupId, int bossId, String startDate) {
		if (userIds.isEmpty()) {//接收邀请
			BossRoomRecord bossRoom = bossRedisService.getBossRoomRecordFirst(createUserId);
			if (bossRoom == null)
				return null;
			
			BossRoomRecord.Builder builder = BossRoomRecord.newBuilder(bossRoom);
			
			if (!builder.getCreateTime().equals(startDate))
				return null;
			
			if (builder.hasStatus() && builder.getStatus() == 1)
				return builder.build();
			
			int userHasKillCount = bossRedisService.getBosskillCount(user.getId(), builder.getGroupId(), builder.getBossId());
			Bossgroup bossGroup = bossRedisService.getBossgroup(builder.getGroupId());
			if (userHasKillCount >= bossGroup.getCount() && builder.getGroupId() < 4) {
				builder.setStatus(3);//没次数了
				return builder.build();
			}
			
			if (builder.getUserCount() >= 4) {
				builder.setStatus(4);//房间人数已满
				return builder.build();
			}
			
			log.debug("11111111");
			user.setBossRoomUserId(createUserId);
			userService.updateUser(user);
			
			
			List<UserInfo> userList = new ArrayList<UserInfo>(builder.getUserList()); 
			userList.add(userService.getCache(user.getServerId(), user.getId()));
			builder.clearUser();
			builder.addAllUser(userList);
			bossRedisService.setBossRoomRecord(builder.build());
			
			return builder.build();
		} else { //邀请别人
			String createTime = DateUtil.getCurrentDateString();
			
			user.setBossRoomUserId(user.getId());
			userService.updateUser(user);
			
			BossRoomRecord record = bossRedisService.getBossRoomRecord(user, user.getBossRoomUserId());
			if (record == null) {
				BossRoomRecord.Builder builder = BossRoomRecord.newBuilder();
				builder.setBossId(bossId);
				builder.setGroupId(groupId);
				builder.setCreateUserId(user.getId());
				builder.setCreateTime(createTime);
				builder.addUser(userService.getCache(user.getServerId(), user.getId()));
				
				bossRedisService.setBossRoomRecord(builder.build());
				record = builder.build();
			}
			
			for (long userId : userIds) {
				sendInviteMail(user, userId, groupId, bossId, record.getCreateTime());
			}
			
			return record;
		}
	}
	
	public BossRoomRecord createBossRoom(UserBean user, int groupId, int bossId) {
		user.setBossRoomUserId(user.getId());
		userService.updateUser(user);
		
		BossRoomRecord record = bossRedisService.getBossRoomRecord(user, user.getBossRoomUserId());
		if (record != null)
			return record;
		
		if (groupId == 4) {
			BossGroupRecord bossGroup = bossRedisService.getZhaohuanBoss(user);
			if (bossGroup == null)
				return null;
		
			for (int i = 0; i < bossGroup.getBossRecordList().size(); ++i) {
				BossRecord bossRecord = bossGroup.getBossRecord(i);
				if (bossRecord.getBossId() == bossId && bossRecord.getEndTime().isEmpty()) {
					int userHasKillCount = bossRedisService.getBosskillCount(user.getId(), groupId, bossId);
					Bossgroup group = bossRedisService.getBossgroup(groupId);
					if (userHasKillCount >= group.getCount())
						break;
					
					BossRoomRecord.Builder builder = BossRoomRecord.newBuilder();
					builder.setBossId(bossId);
					builder.setGroupId(groupId);
					builder.setCreateUserId(user.getId());
					builder.addUser(userService.getCache(user.getServerId(), user.getId()));
					builder.setCreateTime(DateUtil.getCurrentDateString());
					
					bossRedisService.setBossRoomRecord(builder.build());
					return builder.build();
				}
			}
		} else {
			BossRoomRecord.Builder builder = BossRoomRecord.newBuilder();
			builder.setBossId(bossId);
			builder.setGroupId(groupId);
			builder.setCreateUserId(user.getId());
			builder.addUser(userService.getCache(user.getServerId(), user.getId()));
			builder.setCreateTime(DateUtil.getCurrentDateString());
			
			bossRedisService.setBossRoomRecord(builder.build());
			return builder.build();
		}
		
		return null;
	}
	
	public ResultConst quitBossRoom(UserBean user, BossRoomRecord.Builder builder, long userId) {
		if (user.getId() != userId && builder.getCreateUserId() != user.getId()) {
			return ErrorConst.BOSS_ROOM_CAN_NOT_QUIT_OTHER;
		}
		
//		if (builder.hasStatus() && builder.getStatus() == 1) {
//			return ErrorConst.BOSS_ROOM_HAS_START;
//		}
		
		if (builder.getCreateUserId() == user.getId() && user.getId() == userId) {
			bossRedisService.delBossRoomRecord(user.getId());
			builder.clearUser();
			user.setBossRoomUserId(0);
			userService.updateUser(user);
		} else {
			for (int i = 0; i < builder.getUserCount(); ++i) {
				if (builder.getUser(i).getId() == userId) {
					builder.removeUser(i);
					break;
				}
			}
			for (int i = 0; i < builder.getBossRecordCount(); ++i) {
				if (builder.getBossRecord(i).getUserId() == userId) {
					builder.removeBossRecord(i);
					break;
				}
			}
			bossRedisService.setBossRoomRecord(builder.build());
		}
		
		return SuccessConst.BOSS_ROOM_QUIT_SUCCESS;
	}
	
	public BossRoomRecord getBossRoomRecord(UserBean user) {
		if (user.getBossRoomUserId() == 0)
			return null;
		BossRoomRecord record = bossRedisService.getBossRoomRecord(user, user.getBossRoomUserId());
		if (record == null) {
			user.setBossRoomUserId(0);
			userService.updateUser(user);
		}
		
		return record;
	}
	
	public ResultConst submitBossRoomScore(UserBean user, int percent, List<RewardBean> rewardList, BossRoomRecord.Builder builder) {
		if (!builder.hasStatus() || builder.getStatus() != 1)
			return ErrorConst.BOSS_ROOM_IS_NOT_START_OTHER;
		
		int totalPercent = 0;
		boolean first = true;//首次提交成绩
		for (int i = 0; i < builder.getBossRecordList().size(); i++) {
			BossRecord bossRecord = builder.getBossRecord(i);
			BossRecord.Builder bossBuilder = BossRecord.newBuilder(bossRecord);
			if (bossBuilder.getUserId() == user.getId()) {
				first = false;
				if (bossBuilder.getCount() >= percent)
					return SuccessConst.BOSS_SUBMIT_SUCCESS;
				
				bossBuilder.setCount(percent);
				builder.setBossRecord(i, bossBuilder.build());
				bossRedisService.setBossRoomRecord(builder.build());
			}
			totalPercent += bossBuilder.getCount();
		}
		if (first) {
			BossRecord.Builder bossBuilder = BossRecord.newBuilder();
			bossBuilder.setUserId(user.getId());
			bossBuilder.setCount(percent);
			builder.addBossRecord(bossBuilder.build());
			bossRedisService.setBossRoomRecord(builder.build());
			totalPercent += percent;
		}
		
		if (totalPercent >= 10000) {
			bossRedisService.addBosskillCount(user.getId(), builder.getGroupId(), builder.getBossId());
			rewardList.addAll(getBossloot(builder.getBossId(), user, builder.getCreateUserId() == user.getId() ? 1 : 2, percent));
			for (UserInfo userInfo : builder.getUserList()) {
				if (userInfo.getId() != user.getId()) {
					sendBossRoomWinRewardMail(userInfo.getId(), rewardList);
					bossRedisService.addBosskillCount(userInfo.getId(), builder.getGroupId(), builder.getBossId());
				}
			}
			
			bossRedisService.delBossRoomRecord(builder.getCreateUserId());
			builder.setStatus(2);//打死
		} else {
			UserLevelBean userLevel = userLevelService.getUserLevel(user.getId());
			logService.sendWorldbossLog(user.getServerId(), user.getId(), builder.getBossId(), builder.getCreateUserId() == user.getId() ? 1 : 2, 0, percent, 0, 0, 
					0, 0, 0, 0, 0, 0, userLevel.getUnlockDaguan(), user.getZhanliMax(), user.getVip());
		}
		
		return SuccessConst.BOSS_SUBMIT_SUCCESS;
	}
	
	public void zhaohuanBoss(UserBean user, int bossId) {
		Bossgroup bossgroup = bossRedisService.getBossgroup(4);//召唤的boss全在这里
		if (bossgroup == null)
			return;
		BossGroupRecord bossGroup = bossRedisService.getZhaohuanBoss(user);
		BossGroupRecord.Builder builder = BossGroupRecord.newBuilder();
		builder.setGroupId(4);
		if (bossGroup != null)
			builder = BossGroupRecord.newBuilder(bossRedisService.getZhaohuanBoss(user));
//		List<BossRecord> bossRecordList = new ArrayList<BossRecord>(builder.getBossRecordList());
		for (int i = 0; i < builder.getBossRecordList().size(); ++i) {
			BossRecord bossRecord = builder.getBossRecord(i);
			if (bossRecord.getBossId() == bossId) {
//				int userHasKillCount = bossRedisService.getBosskillCount(user.getId(), 4, bossId);
//				Bossgroup bg = bossRedisService.getBossgroup(4);
				if (!bossRecord.getEndTime().isEmpty()) {//boss已被击杀
					BossRecord.Builder bossRecordBuilder = BossRecord.newBuilder(bossRecord);
//					bossRecordBuilder.setBossId(bossId);
					bossRecordBuilder.setEndTime("");
//					bossRecord = bossRecordBuilder.build();
//					builder.clearBossRecord();
//					builder.addAllBossRecord(bossRecordList);
					builder.setBossRecord(i, bossRecordBuilder.build());
					bossRedisService.zhaohuanBoss(user, builder.build());
				}
				
				return;
			}
		}
		
		//boss未被召唤过
		BossRecord.Builder bossRecordBuilder = BossRecord.newBuilder();
		bossRecordBuilder.setBossId(bossId);
		bossRecordBuilder.setEndTime("");
//		bossRecordList.add(bossRecordBuilder.build());
//		builder.clearBossRecord();
		builder.addBossRecord(bossRecordBuilder.build());
		bossRedisService.zhaohuanBoss(user, builder.build());
		
	}
	
	private void sendInviteMail(UserBean user, long userId, int groupId, int bossId, String createTime) {
		String content = "邀请你一起伐木boss！";
		MailBean mail = MailBean.buildMail(userId, user.getId(), user.getVip(), user.getIcon(), user.getUserName(), content, MailConst.TYPE_INVITE_FIGHTBOSS_MAIL, groupId * 1000000 + bossId);
		mail.setStartDate(createTime);
		mailService.addMail(mail);
		log.debug("mail is:" + mail.toJson());
	}
	
	private void sendBossRoomWinRewardMail(long userId, List<RewardBean> rewardList) {
		String content = "boss被击杀啦！";
		MailBean mail = MailBean.buildSystemMail1(userId, content, rewardList);
		mailService.addMail(mail);
	}
}
