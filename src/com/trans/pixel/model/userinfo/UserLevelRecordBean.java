package com.trans.pixel.model.userinfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import com.trans.pixel.model.XiaoguanBean;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class UserLevelRecordBean {
	private long id = 0;
	private long userId = 0;
	private int lastLevelResultTime = 0;
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
			json.put("" + xg.getDaguan(), Math.min(xg.getXiaoguan(), json.getInt("" + xg.getDaguan())));
		}
		
		return json.toString();
	}
	
	public static int getXiaoguanRecord(String string, int daguan) {
		JSONObject json = JSONObject.fromObject(string);
		return json.getInt("" + daguan);
	}
	
	public static String updateXiaoguanRecord(String string, XiaoguanBean xg) {
		JSONObject json = JSONObject.fromObject(string);
		json.put("" + xg.getDaguan(), Math.max(json.getInt("" + xg.getDaguan()), xg.getXiaoguan()));
		
		return json.toString();
	}
	
	public Map<String, String> toMap() {
		Map<String, String> levelRecordMap = new HashMap<String, String>();
		levelRecordMap.put(ID, "" + id);
		levelRecordMap.put(USER_ID, "" + userId);
		levelRecordMap.put(LAST_LEVEL_RESULT_TIME, "" + lastLevelResultTime);
		levelRecordMap.put(PUTONG_LEVEL, "" + putongLevel);
		levelRecordMap.put(KUNNAN_LEVEL, kunnanLevel);
		levelRecordMap.put(DIYU_LEVEL, diyuLevel);
		
		return levelRecordMap;
	}
	public static UserLevelRecordBean convertLevelRecordMapToUserLevelRecordBean(Map<String, String> levelRecordMap) {
		if (levelRecordMap == null)
			return null;
		UserLevelRecordBean levelRecord = new UserLevelRecordBean();
		
		levelRecord.setId(TypeTranslatedUtil.stringToLong(levelRecordMap.get(ID)));
		levelRecord.setUserId(TypeTranslatedUtil.stringToLong(levelRecordMap.get(USER_ID)));
		levelRecord.setLastLevelResultTime(TypeTranslatedUtil.stringToInt(levelRecordMap.get(LAST_LEVEL_RESULT_TIME)));
		levelRecord.setPutongLevel(TypeTranslatedUtil.stringToInt(levelRecordMap.get(PUTONG_LEVEL)));
		levelRecord.setKunnanLevel(levelRecordMap.get(KUNNAN_LEVEL));
		levelRecord.setDiyuLevel(levelRecordMap.get(DIYU_LEVEL));

		return levelRecord;
	}
	
	private static final String ID = "id";
	private static final String USER_ID = "user_id";
	private static final String LAST_LEVEL_RESULT_TIME = "last_level_result_time";
	private static final String PUTONG_LEVEL = "putong_level";
	private static final String KUNNAN_LEVEL = "kunnan_level";
	private static final String DIYU_LEVEL = "diyu_level";
}
