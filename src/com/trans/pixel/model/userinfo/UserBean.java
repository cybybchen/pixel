package com.trans.pixel.model.userinfo;

import java.util.HashMap;
import java.util.Map;

import com.trans.pixel.utils.TypeTranslatedUtil;

public class UserBean {
	public long id = 0;
	public String account = "";
	public String userName = "";
	public int serverId = 0;
	public int unionId = 0;
	public int jewel = 0;
	public int coin = 0;
	public int exp = 0;
	public int pointVS = 0;
	public int pointLadder = 0;
	public int pointExpedition = 0;
	public int pointUnion = 0;
	public int completeLevel = 0;
	public long receiveMessageTimeStamp = 0;
	public int lastLootTime = 0;
	public int refreshLeftTimes = 0;
	public String lastLoginTime = "";
	public int ladderModeLeftTimes = 0;
	public long ladderModeHistoryTop = 10000;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public int getServerId() {
		return serverId;
	}
	public void setServerId(int serverId) {
		this.serverId = serverId;
	}
	public int getUnionId() {
		return unionId;
	}
	public void setUnionId(int unionId) {
		this.unionId = unionId;
	}
	public int getJewel() {
		return jewel;
	}
	public void setJewel(int jewel) {
		this.jewel = jewel;
	}
	public int getCoin() {
		return coin;
	}
	public void setCoin(int coin) {
		this.coin = coin;
	}
	public int getExp() {
		return exp;
	}
	public void setExp(int exp) {
		this.exp = exp;
	}
	public int getPointVS() {
		return pointVS;
	}
	public void setPointVS(int pointVS) {
		this.pointVS = pointVS;
	}
	public int getPointLadder() {
		return pointLadder;
	}
	public void setPointLadder(int pointLadder) {
		this.pointLadder = pointLadder;
	}
	public int getPointExpedition() {
		return pointExpedition;
	}
	public void setPointExpedition(int pointExpedition) {
		this.pointExpedition = pointExpedition;
	}
	public int getPointUnion() {
		return pointUnion;
	}
	public void setPointUnion(int pointUnion) {
		this.pointUnion = pointUnion;
	}
	public int getCompleteLevel() {
		return completeLevel;
	}
	public void setCompleteLevel(int completeLevel) {
		this.completeLevel = completeLevel;
	}
	public long getReceiveMessageTimeStamp() {
		return receiveMessageTimeStamp;
	}
	public void setReceiveMessageTimeStamp(long receiveMessageTimeStamp) {
		this.receiveMessageTimeStamp = receiveMessageTimeStamp;
	}
	public int getLastLootTime() {
		return lastLootTime;
	}
	public void setLastLootTime(int lastLootTime) {
		this.lastLootTime = lastLootTime;
	}
	public int getRefreshLeftTimes() {
		return refreshLeftTimes;
	}
	public void setRefreshLeftTimes(int refreshLeftTimes) {
		this.refreshLeftTimes = refreshLeftTimes;
	}
	public String getLastLoginTime() {
		return lastLoginTime;
	}
	public void setLastLoginTime(String lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}
	public int getLadderModeLeftTimes() {
		return ladderModeLeftTimes;
	}
	public void setLadderModeLeftTimes(int ladderModeLeftTimes) {
		this.ladderModeLeftTimes = ladderModeLeftTimes;
	}
	public long getLadderModeHistoryTop() {
		return ladderModeHistoryTop;
	}
	public void setLadderModeHistoryTop(long ladderModeHistoryTop) {
		this.ladderModeHistoryTop = ladderModeHistoryTop;
	}
	public Map<String, String> toMap() {
		Map<String, String> userMap = new HashMap<String, String>();
		userMap.put(ID, "" + id);
		userMap.put(ACCOUNT, account);
		userMap.put(USERNAME, userName);
		userMap.put(SERVER_ID, "" + serverId);
		userMap.put(UNION_ID, "" + unionId);
		userMap.put(JEWEL, "" + jewel);
		userMap.put(COIN, "" + coin);
		userMap.put(EXP, "" + exp);
		userMap.put(POINT_VS, "" + pointVS);
		userMap.put(POINT_LADDER, "" + pointLadder);
		userMap.put(POINT_EXPEDITION, "" + pointExpedition);
		userMap.put(POINT_UNION, "" + pointUnion);
		userMap.put(COMPLETE_LEVEL, "" + completeLevel);
		userMap.put(RECEIVE_MESSAGE_TIMESTAMP, "" + receiveMessageTimeStamp);
		userMap.put(LAST_LOOT_TIME, "" + lastLootTime);
		userMap.put(REFRESH_LEFT_TIMES, "" + refreshLeftTimes);
		userMap.put(LAST_LOGIN_TIME, "" + lastLoginTime);
		userMap.put(LADDER_MODE_LEFT_TIMES, "" + ladderModeLeftTimes);
		userMap.put(LADDER_MODE_HISTORY_TOP, "" + ladderModeHistoryTop);
		
		return userMap;
	}
	
	public static UserBean convertUserMapToUserBean(Map<String, String> userMap) {
		if (userMap == null || userMap.size() == 0)
			return null;
		
		UserBean userBean = new UserBean();
		userBean.setId(TypeTranslatedUtil.stringToLong(userMap.get(ID)));
		userBean.setAccount(userMap.get(ACCOUNT));
		userBean.setUserName(userMap.get(USERNAME));
		userBean.setServerId(TypeTranslatedUtil.stringToInt(userMap.get(SERVER_ID)));
		userBean.setUnionId(TypeTranslatedUtil.stringToInt(userMap.get(UNION_ID)));
		userBean.setJewel(TypeTranslatedUtil.stringToInt(userMap.get(JEWEL)));
		userBean.setCoin(TypeTranslatedUtil.stringToInt(userMap.get(COIN)));
		userBean.setExp(TypeTranslatedUtil.stringToInt(userMap.get(EXP)));
		userBean.setPointVS(TypeTranslatedUtil.stringToInt(userMap.get(POINT_VS)));
		userBean.setPointLadder(TypeTranslatedUtil.stringToInt(userMap.get(POINT_LADDER)));
		userBean.setPointExpedition(TypeTranslatedUtil.stringToInt(userMap.get(POINT_EXPEDITION)));
		userBean.setPointUnion(TypeTranslatedUtil.stringToInt(userMap.get(POINT_UNION)));
		userBean.setCompleteLevel(TypeTranslatedUtil.stringToInt(userMap.get(COMPLETE_LEVEL)));
		userBean.setReceiveMessageTimeStamp(TypeTranslatedUtil.stringToInt(userMap.get(RECEIVE_MESSAGE_TIMESTAMP)));
		userBean.setLastLootTime(TypeTranslatedUtil.stringToInt(userMap.get(LAST_LOOT_TIME)));
		userBean.setRefreshLeftTimes(TypeTranslatedUtil.stringToInt(userMap.get(REFRESH_LEFT_TIMES)));
		userBean.setLastLoginTime(userMap.get(LAST_LOGIN_TIME));
		userBean.setLadderModeLeftTimes(TypeTranslatedUtil.stringToInt(userMap.get(LADDER_MODE_LEFT_TIMES)));
		userBean.setLadderModeHistoryTop(TypeTranslatedUtil.stringToLong(userMap.get(LADDER_MODE_HISTORY_TOP)));
		
		return userBean;
	}
	
	private final static String ID = "id";
	private final static String ACCOUNT = "account";
	private final static String USERNAME = "name";
	private final static String SERVER_ID = "server_id";
	private final static String UNION_ID = "union_id";
	private final static String JEWEL = "jewel";
	private final static String COIN = "coin";
	private final static String EXP = "exp";
	private final static String POINT_VS = "point_vs";
	private final static String POINT_LADDER = "point_ladder";
	private final static String POINT_EXPEDITION = "point_expedition";
	private final static String POINT_UNION = "point_union";
	private final static String COMPLETE_LEVEL = "complete_level";
	private final static String RECEIVE_MESSAGE_TIMESTAMP = "receive_message_timestamp";
	private final static String LAST_LOOT_TIME = "last_loot_time";
	private final static String REFRESH_LEFT_TIMES = "refresh_left_times";
	private final static String LAST_LOGIN_TIME = "last_login_time";
	private final static String LADDER_MODE_LEFT_TIMES = "ladder_mode_left_times";
	private final static String LADDER_MODE_HISTORY_TOP = "ladder_mode_history_mode";
}