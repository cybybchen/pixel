package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.protoc.Commands.BossGroupRecord;
import com.trans.pixel.protoc.Commands.BossRecord;
import com.trans.pixel.protoc.Commands.Bossgroup;
import com.trans.pixel.protoc.Commands.Bossloot;
import com.trans.pixel.protoc.Commands.BosslootGroup;
import com.trans.pixel.service.redis.BossRedisService;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class BossService {

	@Resource
	private BossRedisService bossRedisService;
	@Resource
	private LogService logService;
	@Resource
	private UserLevelService userLevelService;
	
	public List<RewardBean> submitBosskill(UserBean user, int groupId, int bossId) {
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
	
	public List<BossGroupRecord> randomDailyBoss() {
		List<BossGroupRecord> list = new ArrayList<BossGroupRecord>();
		Map<String, Bossgroup> map = bossRedisService.getBossgroupConfig();
		Iterator<Entry<String, Bossgroup>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Bossgroup> entry = it.next();
			BossGroupRecord.Builder builder = BossGroupRecord.newBuilder();
			List<BossRecord> bossRecordList = new ArrayList<BossRecord>();
			List<Integer> bossIdList = new ArrayList<Integer>();
			Bossgroup bossgroup = entry.getValue();
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
}
