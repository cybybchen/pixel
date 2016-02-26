package com.trans.pixel.model.userinfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trans.pixel.model.XiaoguanBean;
import com.trans.pixel.protoc.Commands.UserFriend;
import com.trans.pixel.protoc.Commands.UserLevel;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class UserLevelBean {
	private long id = 0;
	private long userId = 0;
	private int lastLevelResultTime = 0;
	private int levelPrepareTime = 0;
	private int putongLevel = 0;
	private String kunnanLevel = "";
	private String diyuLevel = "";
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
	public int getLastLevelResultTime() {
		return lastLevelResultTime;
	}
	public void setLastLevelResultTime(int lastLevelResultTime) {
		this.lastLevelResultTime = lastLevelResultTime;
	}
	public int getLevelPrepareTime() {
		return levelPrepareTime;
	}
	public void setLevelPrepareTime(int levelPrepareTime) {
		this.levelPrepareTime = levelPrepareTime;
	}
	public int getPutongLevel() {
		return putongLevel;
	}
	public void setPutongLevel(int putongLevel) {
		this.putongLevel = putongLevel;
	}
	public String getKunnanLevel() {
		return kunnanLevel;
	}
	public void setKunnanLevel(String kunnanLevel) {
		this.kunnanLevel = kunnanLevel;
	}
	public String getDiyuLevel() {
		return diyuLevel;
	}
	public void setDiyuLevel(String diyuLevel) {
		this.diyuLevel = diyuLevel;
	}
	
	public static String initLevelRecord(List<XiaoguanBean> xiaoguanList) {
		JSONObject json = new JSONObject();
		for (XiaoguanBean xg : xiaoguanList) {
			int originalXiaoguan = TypeTranslatedUtil.jsonGetInt(json, "" + xg.getDaguan());
			json.put("" + xg.getDaguan(), Math.min(xg.getXiaoguan(), originalXiaoguan));
		}
		Logger log = LoggerFactory.getLogger(UserLevelBean.class);
		log.debug("222:" + json.toString());
		return json.toString();
	}
	
	public static int getXiaoguanRecord(String string, int daguan) {
		JSONObject json = JSONObject.fromObject(string);
		return json.getInt("" + daguan);
	}
	
	public static String updateXiaoguanRecord(String string, XiaoguanBean xg) {
		JSONObject json = JSONObject.fromObject(string);
		int originalXiaoguan = TypeTranslatedUtil.jsonGetInt(json, "" + xg.getDaguan());
		json.put("" + xg.getDaguan(), Math.max(originalXiaoguan, xg.getXiaoguan()));
		
		return json.toString();
	}
	
//	public Map<String, String> toMap() {
//		Logger log = LoggerFactory.getLogger(UserLevelBean.class);
//		Map<String, String> levelRecordMap = new HashMap<String, String>();
//		levelRecordMap.put(ID, "" + id);
//		levelRecordMap.put(USER_ID, "" + userId);
//		levelRecordMap.put(LAST_LEVEL_RESULT_TIME, "" + lastLevelResultTime);
//		levelRecordMap.put(LEVEL_PREPARE_TIME, "" + levelPrepareTime);
//		levelRecordMap.put(PUTONG_LEVEL, "" + putongLevel);
//		levelRecordMap.put(KUNNAN_LEVEL, kunnanLevel);
//		levelRecordMap.put(DIYU_LEVEL, diyuLevel);
//		
//		log.debug("111: " + levelRecordMap);
//		
//		return levelRecordMap;
//	}
//	public static UserLevelBean convertLevelRecordMapToUserLevelRecordBean(Map<String, String> levelRecordMap) {
//		Logger log = LoggerFactory.getLogger(UserLevelBean.class);
//		log.debug("11122222222222: " + levelRecordMap);
//		if (levelRecordMap == null || levelRecordMap.size() == 0)
//			return null;
//		log.debug("11122222222222333333333333333333: ");
//		UserLevelBean levelRecord = new UserLevelBean();
//		
//		levelRecord.setId(TypeTranslatedUtil.stringToLong(levelRecordMap.get(ID)));
//		levelRecord.setUserId(TypeTranslatedUtil.stringToLong(levelRecordMap.get(USER_ID)));
//		levelRecord.setLastLevelResultTime(TypeTranslatedUtil.stringToInt(levelRecordMap.get(LAST_LEVEL_RESULT_TIME)));
//		levelRecord.setLevelPrepareTime(TypeTranslatedUtil.stringToInt(levelRecordMap.get(LEVEL_PREPARE_TIME)));
//		levelRecord.setPutongLevel(TypeTranslatedUtil.stringToInt(levelRecordMap.get(PUTONG_LEVEL)));
//		levelRecord.setKunnanLevel(levelRecordMap.get(KUNNAN_LEVEL));
//		levelRecord.setDiyuLevel(levelRecordMap.get(DIYU_LEVEL));
//
//		return levelRecord;
//	}
	
	public UserLevel buildUserLevel() {
		UserLevel.Builder builder = UserLevel.newBuilder();
		builder.setDiyuLevel(diyuLevel);
		builder.setKunnanLevel(kunnanLevel);
		builder.setPrepareTime(levelPrepareTime);
		builder.setPutongLevel(putongLevel);
		
		return builder.build();
	}
	
//	private static final String ID = "id";
//	private static final String USER_ID = "user_id";
//	private static final String LAST_LEVEL_RESULT_TIME = "last_level_result_time";
//	private static final String LEVEL_PREPARE_TIME = "level_prepare_time";
//	private static final String PUTONG_LEVEL = "putong_level";
//	private static final String KUNNAN_LEVEL = "kunnan_level";
//	private static final String DIYU_LEVEL = "diyu_level";
}
