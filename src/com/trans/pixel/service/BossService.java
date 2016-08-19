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
	
	public List<RewardBean> submitBosskill(UserBean user, int groupId, int bossId) {
		BossGroupRecord bossGroupRecord = bossRedisService.getBossGroupRecord(groupId);
		if (bossGroupRecord != null) {
			for (BossRecord bossRecord :bossGroupRecord.getBossRecordList()) {
				if (bossRecord.getBossId() == bossId) {
					int userHasKillCount = bossRedisService.getBosskillCount(user.getId(), groupId, bossId);
					Bossgroup bossGroup = bossRedisService.getBossgroup(groupId);
					if (userHasKillCount >= bossGroup.getCount())
						break;
					
					bossRedisService.addBosskillCount(user.getId(), groupId, bossId);
					return getBossloot(groupId);
				}
			}
		}
		
		return new ArrayList<RewardBean>();
	}
	
	private List<RewardBean> getBossloot(int groupId) {
		List<RewardBean> rewardList = new ArrayList<RewardBean>();
		BosslootGroup bosslootGroup = bossRedisService.getBosslootGroup(groupId);
		for (Bossloot bossloot : bosslootGroup.getLootList()) {
			int randomWeight = RandomUtils.nextInt(bossloot.getWeightall()) + 1;
			if (randomWeight <= bossloot.getWeight1()) {
				rewardList.add(RewardBean.init(bossloot.getItemid1(), bossloot.getItemcount1()));
				continue;
			}
			
			randomWeight -= bossloot.getWeight1();
			if (randomWeight <= bossloot.getWeight2()) {
				rewardList.add(RewardBean.init(bossloot.getItemid2(), bossloot.getItemcount2()));
				continue;
			}
			
			randomWeight -= bossloot.getWeight2();
			if (randomWeight <= bossloot.getWeight3()) {
				rewardList.add(RewardBean.init(bossloot.getItemid3(), bossloot.getItemcount3()));
				continue;
			}
		}
		
		return rewardList;
	}
	
	public List<BossGroupRecord> getBossGroupRecord(UserBean user) {
		List<BossGroupRecord> list = bossRedisService.getBossGroupRecordList();
		if (list.size() == 0) {
			list = randomDailyBoss();
			bossRedisService.setBossgroupRecord(list);
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
