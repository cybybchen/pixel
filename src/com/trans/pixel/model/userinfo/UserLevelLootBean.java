package com.trans.pixel.model.userinfo;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.protoc.Commands.UserLootLevel;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class UserLevelLootBean {
	private long id = 0;
	private long userId = 0;
	private int packageCount = 0;
	private int lootLevel = 0;
	private int levelLootStartTime = 0;
	private String lootTime = "";
	private String lootRewardRecord = "";
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public int getPackageCount() {
		return packageCount;
	}
	public void setPackageCount(int packageCount) {
		this.packageCount = packageCount;
	}
	public int getLootLevel() {
		return lootLevel;
	}
	public void setLootLevel(int lootLevel) {
		this.lootLevel = lootLevel;
	}
	public int getLevelLootStartTime() {
		return levelLootStartTime;
	}
	public void setLevelLootStartTime(int levelLootStartTime) {
		this.levelLootStartTime = levelLootStartTime;
	}
	public String getLootTime() {
		return lootTime;
	}
	public void setLootTime(String lootTime) {
		this.lootTime = lootTime;
	}
	
	public String getLootRewardRecord() {
		return lootRewardRecord;
	}
	public void setLootRewardRecord(String lootRewardRecord) {
		this.lootRewardRecord = lootRewardRecord;
	}
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(USER_ID, userId);
		json.put(PACKAGE_COUNT, packageCount);
		json.put(LOOT_LEVEL, lootLevel);
		json.put(LEVEL_LOOT_START_TIME, levelLootStartTime);
		json.put(LOOT_TIME, lootTime);
		json.put(LOOT_REWARD_RECORD, lootRewardRecord);
		
		return json.toString();
	}
	public static UserLevelLootBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		UserLevelLootBean bean = new UserLevelLootBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getInt(ID));
		bean.setUserId(json.getInt(USER_ID));
		bean.setPackageCount(json.getInt(PACKAGE_COUNT));
		bean.setLootLevel(json.getInt(LOOT_LEVEL));
		bean.setLevelLootStartTime(json.getInt(LEVEL_LOOT_START_TIME));
		bean.setLootTime(json.getString(LOOT_TIME));
		bean.setLootRewardRecord(json.getString(LOOT_REWARD_RECORD));
		
		return bean;
	}
	
	public void updateLootTime(int xiaoguanId, int levelLootTime, int levelId) {
		int lastLootTime = 0;
		JSONObject json = new JSONObject();
		try {
			json = JSONObject.fromObject(lootTime);
			lastLootTime = json.getInt("" + xiaoguanId) + (int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND) - levelLootStartTime;
			while (lastLootTime - levelLootTime >= 0) {
				updateLootRewardRecord(levelId);
				lastLootTime -= levelLootTime;
			}
		} catch (Exception e) {
			
		}
		json.put(xiaoguanId, lastLootTime);
		lootTime = json.toString();
	}
	
	public void updateLootRewardRecord(int levelId) {
		if (splitLootReward().length >= packageCount)
			return;
		if (lootRewardRecord.equals(""))
			lootRewardRecord = "" + levelId;
		else
			lootRewardRecord = lootRewardRecord + LOOT_REWARD_RECORD_SEPARATOR + levelId;
	}
	
	public List<Integer> getRewardRecordList() {
		String[] rewardRecords = splitLootReward();
		List<Integer> rewardRecordList = new ArrayList<Integer>();
		for (int i = 0; i < rewardRecords.length; ++i) {
			rewardRecordList.add(TypeTranslatedUtil.stringToInt(rewardRecords[i]));
		}
		lootRewardRecord = "";
		return rewardRecordList;
	}
	
	private String[] splitLootReward() {
		return lootRewardRecord.split("\\" + LOOT_REWARD_RECORD_SEPARATOR);
	}
	
	public UserLootLevel buildUserLootLevel() {
		UserLootLevel.Builder builder = UserLootLevel.newBuilder();
		builder.setLevelLootStartTime(levelLootStartTime);
		builder.setLootLevel(lootLevel);
		builder.setLootTime(lootTime);
		builder.setPackageCount(packageCount);
		builder.setLootRewardRecord(lootRewardRecord);
		
		return builder.build();
	}
	
	public void clearRewardRecord() {
		lootRewardRecord = "";
	}
	
	public void initLootTime() {
		JSONObject json = new JSONObject();
		int initTime = 20 * 60;
		json.put(1, initTime);
		json.put(2, initTime);
		json.put(3, initTime);
		json.put(4, initTime);
		json.put(5, initTime);
		
		lootTime = json.toString();
	}
	
	private static final String ID = "id";
	private static final String USER_ID = "user_id";
	private static final String PACKAGE_COUNT = "package_count";
	private static final String LOOT_LEVEL = "loot_level";
	private static final String LEVEL_LOOT_START_TIME = "levelLootStartTime";
	private static final String LOOT_TIME = "loot_time";
	private static final String LOOT_REWARD_RECORD = "loot_reward_record";
	
	private static final String LOOT_REWARD_RECORD_SEPARATOR = "|";
}
