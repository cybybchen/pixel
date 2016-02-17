package com.trans.pixel.model.userinfo;

import java.util.HashMap;
import java.util.Map;

import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.protoc.Commands.UserDailyData;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class UserBean {
	private long id = 0;
	private String account = "";
	private int icon = 0;
	private String name = "";
	private int serverId = 0;
	private int unionId = 0;
	private int unionJob = 0;
	private int jewel = 0;
	private int coin = 0;
	private int exp = 0;
	private int pointPVP = 0;
	private int pointLadder = 0;
	private int pointExpedition = 0;
	private int pointUnion = 0;
	////////////////////////////////
	private long receiveMessageTimeStamp = 0;
	private int lastLootTime = 0;
	private int refreshLeftTimes = 0;
	private String lastLoginTime = "";
	private int ladderModeLeftTimes = 0;
	private long ladderModeHistoryTop = 10000;
	private long freeLotteryCoinTime = 0;
	private long freeLotteryJewelTime = 0;
	private UserDailyData.Builder userDailyData = null;
	/**
	 * 用户ID
	 */
	public long getId() {
		return id;
	}
	/**
	 * 用户ID
	 */
	public void setId(long id) {
		this.id = id;
	}
	/**
	 * 用户帐号
	 */
	public String getAccount() {
		return account;
	}
	/**
	 * 用户帐号
	 */
	public void setAccount(String account) {
		this.account = account;
	}
	/**
	 * 用户头像
	 */
	public int getIcon() {
		return icon;
	}
	/**
	 * 用户头像
	 */
	public void setIcon(int icon) {
		this.icon = icon;
	}
	/**
	 * 用户昵称
	 */
	public String getUserName() {
		return name;
	}
	/**
	 * 用户昵称
	 */
	public void setUserName(String userName) {
		this.name = userName;
	}
	/**
	 * 所属服务器ID
	 */
	public int getServerId() {
		return serverId;
	}
	/**
	 * 所属服务器ID
	 */
	public void setServerId(int serverId) {
		this.serverId = serverId;
	}
	/**
	 * 所属工会ID
	 */
	public int getUnionId() {
		return unionId;
	}
	/**
	 * 所属工会ID
	 */
	public void setUnionId(int unionId) {
		this.unionId = unionId;
	}
	/**
	 * 拥有钻石
	 */
	public int getJewel() {
		return jewel;
	}
	/**
	 * 拥有钻石
	 */
	public void setJewel(int jewel) {
		this.jewel = jewel;
	}
	/**
	 * 拥有金币
	 */
	public int getCoin() {
		return coin;
	}
	/**
	 * 拥有金币
	 */
	public void setCoin(int coin) {
		this.coin = coin;
	}
	/**
	 * 拥有经验
	 */
	public int getExp() {
		return exp;
	}
	/**
	 * 拥有经验
	 */
	public void setExp(int exp) {
		this.exp = exp;
	}
	/**
	 * 拥有魔晶
	 */
	public int getPointPVP() {
		return pointPVP;
	}
	/**
	 * 拥有魔晶
	 */
	public void setPointPVP(int point) {
		this.pointPVP = point;
	}
	/**
	 * 拥有天梯币
	 */
	public int getPointLadder() {
		return pointLadder;
	}
	/**
	 * 拥有天梯币
	 */
	public void setPointLadder(int pointLadder) {
		this.pointLadder = pointLadder;
	}
	/**
	 * 拥有远征币
	 */
	public int getPointExpedition() {
		return pointExpedition;
	}
	/**
	 * 拥有远征币
	 */
	public void setPointExpedition(int pointExpedition) {
		this.pointExpedition = pointExpedition;
	}
	/**
	 * 拥有工会币
	 */
	public int getPointUnion() {
		return pointUnion;
	}
	/**
	 * 拥有工会币
	 */
	public void setPointUnion(int pointUnion) {
		this.pointUnion = pointUnion;
	}
	/**
	 * 留言板时间戳
	 */
	public long getReceiveMessageTimeStamp() {
		return receiveMessageTimeStamp;
	}
	/**
	 * 留言板时间戳
	 */
	public void setReceiveMessageTimeStamp(long receiveMessageTimeStamp) {
		this.receiveMessageTimeStamp = receiveMessageTimeStamp;
	}
	/**
	 * 挂机时间点
	 */
	public int getLastLootTime() {
		return lastLootTime;
	}
	/**
	 * 挂机时间点
	 */
	public void setLastLootTime(int lastLootTime) {
		this.lastLootTime = lastLootTime;
	}
	/**
	 * 挂机PVP刷新次数
	 */
	public int getRefreshLeftTimes() {
		return refreshLeftTimes;
	}
	/**
	 * 挂机PVP刷新次数
	 */
	public void setRefreshLeftTimes(int refreshLeftTimes) {
		this.refreshLeftTimes = refreshLeftTimes;
	}
	/**
	 * 登陆时间
	 */
	public String getLastLoginTime() {
		return lastLoginTime;
	}
	/**
	 * 登陆时间
	 */
	public void setLastLoginTime(String lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}
	/**
	 * 剩余天梯次数
	 */
	public int getLadderModeLeftTimes() {
		return ladderModeLeftTimes;
	}
	/**
	 * 剩余天梯次数
	 */
	public void setLadderModeLeftTimes(int ladderModeLeftTimes) {
		this.ladderModeLeftTimes = ladderModeLeftTimes;
	}
	/**
	 * 天梯历史最高排名
	 */
	public long getLadderModeHistoryTop() {
		return ladderModeHistoryTop;
	}
	/**
	 * 天梯历史最高排名
	 */
	public void setLadderModeHistoryTop(long ladderModeHistoryTop) {
		this.ladderModeHistoryTop = ladderModeHistoryTop;
	}
	/**
	 * 工会职务
	 */
	public int getUnionJob() {
		return unionJob;
	}
	/**
	 * 工会职务
	 */
	public void setUnionJob(int unionJob) {
		this.unionJob = unionJob;
	}
	public UserDailyData.Builder getUserDailyData() {
		return userDailyData;
	}
	public void setUserDailyData(UserDailyData.Builder builder) {
		userDailyData = builder;
	}
	/**
	 * 普通商店刷新次数
	 */
	public int getDailyShopRefreshTime() {
		return userDailyData.getDailyShopRefreshTime();
	}
	/**
	 * 普通商店刷新次数
	 */
	public void setDailyShopRefreshTime(int refreshTime) {
		userDailyData.setDailyShopRefreshTime(refreshTime);
	}
	/**
	 * 黑市刷新次数
	 */
	public int getBlackShopRefreshTime() {
		return userDailyData.getBlackShopRefreshTime();
	}
	/**
	 * 黑市刷新次数
	 */
	public void setBlackShopRefreshTime(int refreshTime) {
		userDailyData.setBlackShopRefreshTime(refreshTime);
	}
	/**
	 * 工会商店刷新次数
	 */
	public int getUnionShopRefreshTime() {
		return userDailyData.getUnionShopRefreshTime();
	}
	/**
	 * 工会商店刷新次数
	 */
	public void setUnionShopRefreshTime(int refreshTime) {
		userDailyData.setUnionShopRefreshTime(refreshTime);
	}
	/**
	 * 挂机PVP商店刷新次数
	 */
	public int getPVPShopRefreshTime() {
		return userDailyData.getPVPShopRefreshTime();
	}
	/**
	 * 挂机PVP商店刷新次数
	 */
	public void setPVPShopRefreshTime(int refreshTime) {
		userDailyData.setPVPShopRefreshTime(refreshTime);
	}
	/**
	 * 天梯商店刷新次数
	 */
	public int getLadderShopRefreshTime() {
		return userDailyData.getLadderShopRefreshTime();
	}
	/**
	 * 天梯商店刷新次数
	 */
	public void setLadderShopRefreshTime(int refreshTime) {
		userDailyData.setLadderShopRefreshTime(refreshTime);
	}
	/**
	 * 远征商店刷新次数
	 */
	public int getExpeditionShopRefreshTime() {
		return userDailyData.getExpeditionShopRefreshTime();
	}
	/**
	 * 远征商店刷新次数
	 */
	public void setExpeditionShopRefreshTime(int refreshTime) {
		userDailyData.setExpeditionShopRefreshTime(refreshTime);
	}
	public long getFreeLotteryCoinTime() {
		return freeLotteryCoinTime;
	}
	public void setFreeLotteryCoinTime(long freeLotteryCoinTime) {
		this.freeLotteryCoinTime = freeLotteryCoinTime;
	}
	public long getFreeLotteryJewelTime() {
		return freeLotteryJewelTime;
	}
	public void setFreeLotteryJewelTime(long freeLotteryJewelTime) {
		this.freeLotteryJewelTime = freeLotteryJewelTime;
	}
	
	public void init(int serverId, String account, String userName) {
		setAccount(account);
		setId(0);
		setServerId(serverId);
		setUserName(userName);
		setCoin(1000000);
		setJewel(1000000);
		setExp(1000000);
	}

	public UserInfo buildShort() {
		UserInfo.Builder builder = UserInfo.newBuilder();
		builder.setId(id);
//		builder.setAccount(account);
		builder.setIcon(icon);
		builder.setName(name);
//		builder.setServerId(serverId);
//		builder.setUnionId(unionId);
//		builder.setUnionJob(unionJob);
		return builder.build();
	}
	
//	public UserInfo buildUnionUser() {
//		UserInfo.Builder builder = UserInfo.newBuilder();
//		builder.setId(id);
//		builder.setAccount(account);
//		builder.setIcon(icon);
//		builder.setUserName(userName);
//		builder.setServerId(serverId);
//		builder.setUnionId(unionId);
//		builder.setUnionJob(unionJob);
//		return builder.build();
//	}

	public UserInfo build() {
		UserInfo.Builder builder = UserInfo.newBuilder();
		builder.setId(id);
		builder.setAccount(account);
		builder.setIcon(icon);
		builder.setName(name);
		builder.setServerId(serverId);
		builder.setUnionId(unionId);
		builder.setUnionJob(unionJob);
		builder.setJewel(jewel);
		builder.setCoin(coin);
		builder.setExp(exp);
		builder.setPointPVP(pointPVP);
		builder.setPointLadder(pointLadder);
		builder.setPointExpedition(pointExpedition);
		builder.setPointUnion(pointUnion);
//		builder.setReceiveMessageTimeStamp(receiveMessageTimeStamp);
//		builder.setLastLootTime(lastLootTime);
//		builder.setRefreshLeftTimes(refreshLeftTimes);
//		builder.setLastLoginTime(lastLoginTime);
//		builder.setLadderModeLeftTimes(ladderModeLeftTimes);
//		builder.setLadderModeHistoryTop(ladderModeHistoryTop);
		builder.setFreeLotteryCoinTime(Math.max(0, (int)((freeLotteryCoinTime + 24 * TimeConst.MILLIONSECONDS_PER_HOUR - System.currentTimeMillis()) / TimeConst.MILLIONSECONDS_PER_SECOND)));
		builder.setFreeLotteryJewelTime(Math.max(0, (int)((freeLotteryJewelTime + 24 * TimeConst.MILLIONSECONDS_PER_HOUR - System.currentTimeMillis()) / TimeConst.MILLIONSECONDS_PER_SECOND)));
		return builder.build();
	}
	public Map<String, String> toMap() {
		Map<String, String> userMap = new HashMap<String, String>();
		userMap.put(ID, "" + id);
		userMap.put(ACCOUNT, account);
		userMap.put(ICON, ""+icon);
		userMap.put(USERNAME, name);
		userMap.put(SERVER_ID, "" + serverId);
		userMap.put(UNION_ID, "" + unionId);
		userMap.put(UNION_JOB, "" + unionJob);
		userMap.put(JEWEL, "" + jewel);
		userMap.put(COIN, "" + coin);
		userMap.put(EXP, "" + exp);
		userMap.put(POINT_PVP, "" + pointPVP);
		userMap.put(POINT_LADDER, "" + pointLadder);
		userMap.put(POINT_EXPEDITION, "" + pointExpedition);
		userMap.put(POINT_UNION, "" + pointUnion);
		userMap.put(RECEIVE_MESSAGE_TIMESTAMP, "" + receiveMessageTimeStamp);
		userMap.put(LAST_LOOT_TIME, "" + lastLootTime);
		userMap.put(REFRESH_LEFT_TIMES, "" + refreshLeftTimes);
		userMap.put(LAST_LOGIN_TIME, "" + lastLoginTime);
		userMap.put(LADDER_MODE_LEFT_TIMES, "" + ladderModeLeftTimes);
		userMap.put(LADDER_MODE_HISTORY_TOP, "" + ladderModeHistoryTop);
		userMap.put(FREE_LOTTERY_COIN_TIME, "" + freeLotteryCoinTime);
		userMap.put(FREE_LOTTERY_JEWEL_TIME, "" + freeLotteryJewelTime);
		
		return userMap;
	}
	
	public static UserBean convertUserMapToUserBean(Map<String, String> userMap) {
		if (userMap == null || userMap.size() == 0)
			return null;
		
		UserBean userBean = new UserBean();
		userBean.setId(TypeTranslatedUtil.stringToLong(userMap.get(ID)));
		userBean.setAccount(userMap.get(ACCOUNT));
		userBean.setIcon(TypeTranslatedUtil.stringToInt(userMap.get(ICON)));
		userBean.setUserName(userMap.get(USERNAME));
		userBean.setServerId(TypeTranslatedUtil.stringToInt(userMap.get(SERVER_ID)));
		userBean.setUnionId(TypeTranslatedUtil.stringToInt(userMap.get(UNION_ID)));
		userBean.setUnionJob(TypeTranslatedUtil.stringToInt(userMap.get(UNION_JOB)));
		userBean.setJewel(TypeTranslatedUtil.stringToInt(userMap.get(JEWEL)));
		userBean.setCoin(TypeTranslatedUtil.stringToInt(userMap.get(COIN)));
		userBean.setExp(TypeTranslatedUtil.stringToInt(userMap.get(EXP)));
		userBean.setPointPVP(TypeTranslatedUtil.stringToInt(userMap.get(POINT_PVP)));
		userBean.setPointLadder(TypeTranslatedUtil.stringToInt(userMap.get(POINT_LADDER)));
		userBean.setPointExpedition(TypeTranslatedUtil.stringToInt(userMap.get(POINT_EXPEDITION)));
		userBean.setPointUnion(TypeTranslatedUtil.stringToInt(userMap.get(POINT_UNION)));
		userBean.setReceiveMessageTimeStamp(TypeTranslatedUtil.stringToInt(userMap.get(RECEIVE_MESSAGE_TIMESTAMP)));
		userBean.setLastLootTime(TypeTranslatedUtil.stringToInt(userMap.get(LAST_LOOT_TIME)));
		userBean.setRefreshLeftTimes(TypeTranslatedUtil.stringToInt(userMap.get(REFRESH_LEFT_TIMES)));
		userBean.setLastLoginTime(userMap.get(LAST_LOGIN_TIME));
		userBean.setLadderModeLeftTimes(TypeTranslatedUtil.stringToInt(userMap.get(LADDER_MODE_LEFT_TIMES)));
		userBean.setLadderModeHistoryTop(TypeTranslatedUtil.stringToLong(userMap.get(LADDER_MODE_HISTORY_TOP)));
		userBean.setFreeLotteryCoinTime(TypeTranslatedUtil.stringToLong(userMap.get(FREE_LOTTERY_COIN_TIME)));
		userBean.setFreeLotteryJewelTime(TypeTranslatedUtil.stringToLong(userMap.get(FREE_LOTTERY_JEWEL_TIME)));
		
		return userBean;
	}
	
	private final static String ID = "id";
	private final static String ICON = "icon";
	private final static String ACCOUNT = "account";
	private final static String USERNAME = "name";
	private final static String SERVER_ID = "server_id";
	private final static String UNION_ID = "union_id";
	private final static String UNION_JOB = "union_job";
	private final static String JEWEL = "jewel";
	private final static String COIN = "coin";
	private final static String EXP = "exp";
	private final static String POINT_PVP = "point_pvp";
	private final static String POINT_LADDER = "point_ladder";
	private final static String POINT_EXPEDITION = "point_expedition";
	private final static String POINT_UNION = "point_union";
	private final static String RECEIVE_MESSAGE_TIMESTAMP = "receive_message_timestamp";
	private final static String LAST_LOOT_TIME = "last_loot_time";
	private final static String REFRESH_LEFT_TIMES = "refresh_left_times";
	private final static String LAST_LOGIN_TIME = "last_login_time";
	private final static String LADDER_MODE_LEFT_TIMES = "ladder_mode_left_times";
	private final static String LADDER_MODE_HISTORY_TOP = "ladder_mode_history_mode";
	private final static String FREE_LOTTERY_COIN_TIME = "free_lottery_coin_time";
	private final static String FREE_LOTTERY_JEWEL_TIME = "free_lottery_jewel_time";
}
