package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.MailConst;
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
import com.trans.pixel.service.redis.BossRedisService;
import com.trans.pixel.utils.DateUtil;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class BossService {

	@Resource
	private BossRedisService bossRedisService;
	@Resource
	private LogService logService;
	@Resource
	private UserLevelService userLevelService;
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
//					int userHasKillCount = bossRedisService.getBosskillCount(user.getId(), groupId, bossId);
//					Bossgroup bossGroup = bossRedisService.getBossgroup(groupId);
//					if (userHasKillCount >= bossGroup.getCount())
//						break;
					builder.setEndTime(DateUtil.getCurrentDateString());
					BossGroupRecord.Builder bossgroupBuilder = BossGroupRecord.newBuilder(userBossGroup);
//					bossgroupBuilder.clearBossRecord();
					bossgroupBuilder.setBossRecord(i, builder.build());
//					bossgroupBuilder.addAllBossRecord(bossRecordList);
					bossRedisService.zhaohuanBoss(user, bossgroupBuilder.build());
					
//					bossRedisService.addBosskillCount(user.getId(), groupId, bossId);
					return getBossloot(bossId, user);
				}
			}
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
					
					return getBossloot(bossId, user);
				}
			}
		}
		
		return new ArrayList<RewardBean>();
	}
	
	private List<RewardBean> getBossloot(int bossId, UserBean user) {
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
	
		UserLevelBean userLevel = userLevelService.selectUserLevelRecord(user.getId());
		logService.sendWorldbossLog(user.getServerId(), user.getId(), bossId, 1, itemid1, itemcount1, 
				itemid2, itemcount2, itemid3, itemcount3, itemid4, itemcount4, userLevel.getPutongLevel(), user.getZhanliMax(), user.getVip());
		
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
			for (BossRecord bossRecord : bossgroup.getBossRecordList()) {
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
	
	public BossRoomRecord inviteFightBoss(UserBean user, long createUserId, List<Long> userIds, int groupId, int bossId) {
		if (userIds.isEmpty()) {//接收邀请
			user.setBossRoomUserId(createUserId);
			userService.updateUser(user);
			
			BossRoomRecord.Builder builder = BossRoomRecord.newBuilder(bossRedisService.getBossRoomRecord(createUserId));
			List<Long> userIdList = new ArrayList<Long>(builder.getUserIdList()); 
			userIdList.add(user.getId());
			builder.clearUserId();
			builder.addAllUserId(userIdList);
			bossRedisService.setBossRoomRecord(builder.build());
			
			return builder.build();
		} else { //邀请别人
			for (long userId : userIds) {
				sendInviteMail(user, userId, groupId, bossId);
			}
			user.setBossRoomUserId(user.getId());
			userService.updateUser(user);
			
			BossRoomRecord.Builder builder = BossRoomRecord.newBuilder();
			builder.setBossId(bossId);
			builder.setGroupId(groupId);
			builder.setCreateUserId(user.getId());
			
			bossRedisService.setBossRoomRecord(builder.build());
			
			return builder.build();
		}
	}
	
	public void quitBossRoom(UserBean user) {
		if (user.getBossRoomUserId() == 0)
			return;
		BossRoomRecord record = bossRedisService.getBossRoomRecord(user.getBossRoomUserId());
		if (record == null)
			return;
		
		user.setBossRoomUserId(0);
		userService.updateUser(user);
		
		BossRoomRecord.Builder builder = BossRoomRecord.newBuilder(record);
		if (builder.getCreateUserId() == user.getId()) {
			bossRedisService.delBossRoomRecord(user.getId());
		} else {
			List<Long> userIdList = new ArrayList<Long>(builder.getUserIdList()); 
			userIdList.remove(user.getId());
			builder.clearUserId();
			builder.addAllUserId(userIdList);
			bossRedisService.setBossRoomRecord(builder.build());
		}
	}
	
	public BossRoomRecord getBossRoomRecord(UserBean user) {
		if (user.getBossRoomUserId() == 0)
			return null;
		BossRoomRecord record = bossRedisService.getBossRoomRecord(user.getBossRoomUserId());
		if (record == null) {
			user.setBossRoomUserId(0);
			userService.updateUser(user);
		}
		
		return record;
	}
	
	public BossRoomRecord submitBossRoomScore(UserBean user, int percent, List<RewardBean> rewardList) {
		if (user.getBossRoomUserId() == 0)
			return null;
		
		BossRoomRecord record = bossRedisService.getBossRoomRecord(user.getBossRoomUserId());
		if (record == null)
			return null;
		
		BossRoomRecord.Builder builder = BossRoomRecord.newBuilder(record);
		if (!builder.getUserIdList().contains(user.getId()))
			return null;
		
		int userHasKillCount = bossRedisService.getBosskillCount(user.getId(), builder.getGroupId(), builder.getBossId());
		Bossgroup bossGroup = bossRedisService.getBossgroup(builder.getGroupId());
		if (userHasKillCount >= bossGroup.getCount() && builder.getGroupId() < 4)
			return null;
		
		int totalPercent = 0;
		for (int i = 0; i < builder.getBossRecordList().size(); i++) {
			BossRecord bossRecord = builder.getBossRecord(i);
			if (bossRecord.getUserId() == user.getId()) {
				if (bossRecord.getCount() < percent)
					return builder.build();
				
				BossRecord.Builder bossBuilder = BossRecord.newBuilder(bossRecord);
				bossBuilder.setCount(percent);
				builder.setBossRecord(i, bossBuilder.build());
				bossRedisService.setBossRoomRecord(builder.build());
			}
			totalPercent += bossRecord.getCount();
		}
		
		if (totalPercent >= 100) {
			bossRedisService.addBosskillCount(user.getId(), builder.getGroupId(), builder.getBossId());
			rewardList = getBossloot(builder.getBossId(), user);
			for (long userId : builder.getUserIdList()) {
				if (userId != user.getId()) {
					sendBossRoomWinRewardMail(userId, rewardList);
					bossRedisService.addBosskillCount(userId, builder.getGroupId(), builder.getBossId());
				}
			}
		}
		
		return builder.build();
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
	
	private void sendInviteMail(UserBean user, long userId, int groupId, int bossId) {
		String content = "邀请你一起伐木boss！";
		MailBean mail = MailBean.buildMail(userId, user.getId(), user.getVip(), user.getIcon(), user.getUserName(), content, MailConst.TYPE_INVITE_FIGHTBOSS_MAIL, groupId * 1000000 + bossId);
		mailService.addMail(mail);
	}
	
	private void sendBossRoomWinRewardMail(long userId, List<RewardBean> rewardList) {
		String content = "boss被击杀啦！";
		MailBean mail = MailBean.buildSystemMail1(userId, content, rewardList);
		mailService.addMail(mail);
	}
}
