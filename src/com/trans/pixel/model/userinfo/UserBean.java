package com.trans.pixel.model.userinfo;

import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.protoc.Commands.JewelPool;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.service.LibaoService;

public class UserBean {
	private long id = 0;
	private String account = "";
	private String session = "";
	private int icon = 0;
	private String name = "";
	private int serverId = 0;
	private int unionId = 0;
	private int unionJob = 0;
	private int vip = 0;
	private int jewel = 0;
	private long coin = 0;
	private long exp = 0;
	private int pointPVP = 0;
	private int pointLadder = 0;
	private int pointExpedition = 0;
	private int pointUnion = 0;
	private int zhanli = 0;
	private int zhanliMax = 0;
	private int viplibao1 = 0;
	private int viplibao2 = 0;
	////////////////////////////////
	private long receiveMessageTimeStamp = 0;
	private int lastLootTime = 0;
//	private int refreshLeftTimes = 0;
	private String lastLoginTime = "";
	private String registerTime = "";
	private long redisTime = 0;
	private int ladderPurchaseTimes = 0;
	private int ladderModeLeftTimes = 0;
	private long ladderModeHistoryTop = 2501;
	private long freeLotteryCoinTime = 0;
	private int freeLotteryCoinLeftTime = 5;
	private long freeLotteryJewelTime = 0;
	private int dailyShopRefreshTime = 0;
	private int blackShopRefreshTime = 0;
	private int unionShopRefreshTime = 0;
	private int PVPShopRefreshTime = 0;
	private int ladderShopRefreshTime = 0;
	private int expeditionShopRefreshTime = 0;
	private int purchaseCoinTime = 0;
	private int purchaseCoinLeft = 0;
	private long pvpMonsterRefreshTime = 0;
	private long pvpBossRefreshTime = 0;
	private long pvpMineRefreshTime = 0;
	private long pvpMineGainTime = 0;
	private int pvpMineLeftTime = 0;
	private long purchaseTireLeftTime = 0;
	private long refreshExpeditionLeftTime = 0;
	private long baoxiangLeftTime = 0;
	private long zhibaoLeftTime = 0;
	private int signCount = 0;
	private String lastSignTime = "";
	private int loginDays = 0;
	private long areaMonsterRefreshTime = 0;
	private int areaEnergy = 0;
	private int areaUnlock = 0;
	private int pvpUnlock = 0;
	private int heroLimit = 0;
	private String composeSkill = "";
	private int rechargeRecord = 0;
	private long refreshPvpMapTime = 0;
	private int heroInfoId = 0;
	private int firstGetHeroId = 0;
	private int totalSignCount = 0;
	private String greenhand = "";
	private int advance = 0;
	private int skill = 0;
	private int failed = 0;
	private int shouchongIsComplete = 0;
	private int shouchongIsGetReward = 0;
	private long currentTeamid = 1;
	private int growJewelCount = 0;
	private int growJewelCountStatus = 0;
	private int growExpCount = 0;
	private int growExpCountStatus = 0;
	private String version = "";
	private int myactive = 0;
	/**
	 * 当前活跃度
	 */
	public int getMyactive() {
		return myactive;
	}
	/**
	 * 当前活跃度
	 */
	public void setMyactive(int myactive) {
		this.myactive = myactive;
	}
	/**
	 * 当前游戏版本
	 */
	public String getVersion() {
		return version;
	}
	/**
	 * 当前游戏版本
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	/**
	 * 成长钻石基金
	 */
	public int getGrowJewelCount() {
		return growJewelCount;
	}
	/**
	 * 成长钻石基金
	 */
	public void setGrowJewelCount(int growJewelCount) {
		this.growJewelCount = growJewelCount;
	}
	/**
	 * 成长钻石基金
	 */
	public int getGrowJewelCountStatus() {
		return growJewelCountStatus;
	}
	/**
	 * 成长钻石基金
	 */
	public void setGrowJewelCountStatus(int growJewelCountStatus) {
		this.growJewelCountStatus = growJewelCountStatus;
	}
	/**
	 * 成长经验基金
	 */
	public int getGrowExpCount() {
		return growExpCount;
	}
	/**
	 * 成长经验基金
	 */
	public void setGrowExpCount(int growExpCount) {
		this.growExpCount = growExpCount;
	}
	/**
	 * 成长经验基金
	 */
	public int getGrowExpCountStatus() {
		return growExpCountStatus;
	}
	/**
	 * 成长经验基金
	 */
	public void setGrowExpCountStatus(int growExpCountStatus) {
		this.growExpCountStatus = growExpCountStatus;
	}
	/**
	 * 上阵队伍id
	 */
	public long getCurrentTeamid() {
		return currentTeamid;
	}
	/**
	 * 上阵队伍id
	 */
	public void setCurrentTeamid(long currentTeamid) {
		this.currentTeamid = currentTeamid;
	}
	/**
	 * vip免费礼包领取状态
	 */
	public int getViplibao1() {
		return viplibao1;
	}
	/**
	 * vip免费礼包领取状态
	 */
	public void setViplibao1(int viplibao1) {
		this.viplibao1 = viplibao1;
	}
	/**
	 * vip折扣礼包购买状态
	 */
	public int getViplibao2() {
		return viplibao2;
	}
	/**
	 * vip折扣礼包购买状态
	 */
	public void setViplibao2(int viplibao2) {
		this.viplibao2 = viplibao2;
	}
	/**
	 * 下次pvp刷新时间
	 */
	public long getRefreshPvpMapTime() {
		return refreshPvpMapTime;
	}
	/**
	 * 下次pvp刷新时间
	 */
	public void setRefreshPvpMapTime(long refreshPvpMapTime) {
		this.refreshPvpMapTime = refreshPvpMapTime;
	}
	/**
	 * 注册时间
	 */
	public String getRegisterTime() {
		return registerTime;
	}
	/**
	 * 注册时间
	 */
	public void setRegisterTime(String registerTime) {
		this.registerTime = registerTime;
	}
	/**
	 * Session
	 */
	public String getSession() {
		return session;
	}
	/**
	 * Session
	 */
	public void setSession(String session) {
		this.session = session;
	}
	/**
	 * 区域解锁进度
	 */
	public int getAreaUnlock() {
		return areaUnlock;
	}
	/**
	 * 区域解锁进度
	 */
	public void setAreaUnlock(int areaUnlock) {
		this.areaUnlock = areaUnlock;
	}
	/**
	 * 挂机pvp解锁进度
	 */
	public int getPvpUnlock() {
		return pvpUnlock;
	}
	/**
	 * 挂机pvp解锁进度
	 */
	public void setPvpUnlock(int pvpUnlock) {
		this.pvpUnlock = pvpUnlock;
	}
	/**
	 * 疲劳值
	 */
	public int getAreaEnergy() {
		return areaEnergy;
	}
	/**
	 * 疲劳值
	 */
	public void setAreaEnergy(int areaEnergy) {
		this.areaEnergy = areaEnergy;
	}
	/**
	 * 疲劳回满时间
	 */
	public long getAreaEnergyTime() {
		return areaEnergyTime;
	}
	/**
	 * 疲劳回满时间
	 */
	public void setAreaEnergyTime(long areaEnergyTime) {
		this.areaEnergyTime = areaEnergyTime;
	}

	private long areaEnergyTime = 0;
	/**
	 * 战力
	 */
	public int getZhanli() {
		return zhanli;
	}
	/**
	 * 战力
	 */
	public void setZhanli(int zhanli) {
		this.zhanli = zhanli;
	}
	/**
	 * 最高战力
	 */
	public int getZhanliMax() {
		return zhanliMax;
	}
	/**
	 * 最高战力
	 */
	public void setZhanliMax(int zhanli) {
		this.zhanliMax = zhanli;
	}
	/**
	 * 魄罗宝箱剩余购买次数
	 */
	public long getBaoxiangLeftTime() {
		return baoxiangLeftTime;
	}
	/**
	 * 魄罗宝箱剩余购买次数
	 */
	public void setBaoxiangLeftTime(long baoxiangLeftTime) {
		this.baoxiangLeftTime = baoxiangLeftTime;
	}
	/**
	 * 魄罗至宝剩余购买次数
	 */
	public long getZhibaoLeftTime() {
		return zhibaoLeftTime;
	}
	/**
	 * 魄罗至宝剩余购买次数
	 */
	public void setZhibaoLeftTime(long zhibaoLeftTime) {
		this.zhibaoLeftTime = zhibaoLeftTime;
	}
	/**
	 * 区域争夺疲劳购买次数
	 */
	public long getPurchaseTireLeftTime() {
		return purchaseTireLeftTime;
	}
	/**
	 * 区域争夺疲劳购买次数
	 */
	public void setPurchaseTireLeftTime(long purchaseTireLeftTime) {
		this.purchaseTireLeftTime = purchaseTireLeftTime;
	}
	/**
	 * 远征重置次数
	 */
	public long getRefreshExpeditionLeftTime() {
		return refreshExpeditionLeftTime;
	}
	/**
	 * 远征重置次数
	 */
	public void setRefreshExpeditionLeftTime(long refreshExpeditionLeftTime) {
		this.refreshExpeditionLeftTime = refreshExpeditionLeftTime;
	}
	/**
	 * 区域怪物刷新时间
	 */
	public long getAreaMonsterRefreshTime() {
		return areaMonsterRefreshTime;
	}
	/**
	 * 区域怪物刷新时间
	 */
	public void setAreaMonsterRefreshTime(long areaMonsterRefreshTime) {
		this.areaMonsterRefreshTime = areaMonsterRefreshTime;
	}
	/**
	 * pvp矿点对手剩余刷新次数
	 */
	public int getPvpMineLeftTime() {
		return pvpMineLeftTime;
	}
	/**
	 * pvp矿点对手剩余刷新次数
	 */
	public void setPvpMineLeftTime(int time) {
		this.pvpMineLeftTime = time;
	}
	/**
	 * pvp矿点对手刷新时间
	 */
	public long getPvpMineRefreshTime() {
		return pvpMineRefreshTime;
	}
	/**
	 * pvp矿点对手刷新时间
	 */
	public void setPvpMineRefreshTime(long pvpMineRefreshTime) {
		this.pvpMineRefreshTime = pvpMineRefreshTime;
	}
	/**
	 * pvp矿点收获时间
	 */
	public long getPvpMineGainTime() {
		return pvpMineGainTime;
	}
	/**
	 * pvp矿点收获时间
	 */
	public void setPvpMineGainTime(long pvpMineGainTime) {
		this.pvpMineGainTime = pvpMineGainTime;
	}
	/**
	 * pvp怪物刷新时间
	 */
	public long getPvpMonsterRefreshTime() {
		return pvpMonsterRefreshTime;
	}
	/**
	 * pvp怪物刷新时间
	 */
	public void setPvpMonsterRefreshTime(long pvpMonsterRefreshTime) {
		this.pvpMonsterRefreshTime = pvpMonsterRefreshTime;
	}
	/**
	 * pvpBOSS刷新时间
	 */
	public long getPvpBossRefreshTime() {
		return pvpBossRefreshTime;
	}
	/**
	 * pvpBOSS刷新时间
	 */
	public void setPvpBossRefreshTime(long pvpBossRefreshTime) {
		this.pvpBossRefreshTime = pvpBossRefreshTime;
	}
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
	 * vip等级
	 */
	public int getVip() {
		return vip;
	}
	/**
	 * vip等级
	 */
	public void setVip(int vip) {
		this.vip = vip;
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
	public long getCoin() {
		return coin;
	}
	/**
	 * 拥有金币
	 */
	public void setCoin(long coin) {
		this.coin = coin;
	}
	/**
	 * 拥有经验
	 */
	public long getExp() {
		return exp;
	}
	/**
	 * 拥有经验
	 */
	public void setExp(long exp) {
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
	 * hero上限
	 */
	public int getHeroLimit() {
		return heroLimit;
	}
	public void setHeroLimit(int heroLimit) {
		this.heroLimit = heroLimit;
		if (heroLimit == 0)
			this.heroLimit = 30;
	}
	//	/**
//	 * 挂机PVP刷新次数
//	 */
//	public int getRefreshLeftTimes() {
//		return refreshLeftTimes;
//	}
//	/**
//	 * 挂机PVP刷新次数
//	 */
//	public void setRefreshLeftTimes(int refreshLeftTimes) {
//		this.refreshLeftTimes = refreshLeftTimes;
//	}
	/**
	 * 登陆时间
	 */
	public String getLastLoginTime() {
		return lastLoginTime;
	}
	public long getRedisTime() {
		return redisTime;
	}
	/**
	 * 登陆时间
	 */
	public void setLastLoginTime(String lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}
	public void setRedisTime(long time) {
		this.redisTime = time;
	}
	/**
	 * 天梯
	 */
	public int getLadderPurchaseTimes() {
		return ladderPurchaseTimes;
	}
	/**
	 * 天梯
	 */
	public void setLadderPurchaseTimes(int ladderPurchaseTimes) {
		this.ladderPurchaseTimes = ladderPurchaseTimes;
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
	/**
	 * 普通商店刷新次数
	 */
	public int getDailyShopRefreshTime() {
		return dailyShopRefreshTime;
	}
	/**
	 * 普通商店刷新次数
	 */
	public void setDailyShopRefreshTime(int refreshTime) {
		dailyShopRefreshTime = refreshTime;
	}
	/**
	 * 黑市刷新次数
	 */
	public int getBlackShopRefreshTime() {
		return blackShopRefreshTime;
	}
	/**
	 * 黑市刷新次数
	 */
	public void setBlackShopRefreshTime(int refreshTime) {
		blackShopRefreshTime = refreshTime;
	}
	/**
	 * 工会商店刷新次数
	 */
	public int getUnionShopRefreshTime() {
		return unionShopRefreshTime;
	}
	/**
	 * 工会商店刷新次数
	 */
	public void setUnionShopRefreshTime(int refreshTime) {
		unionShopRefreshTime = refreshTime;
	}
	/**
	 * 挂机PVP商店刷新次数
	 */
	public int getPVPShopRefreshTime() {
		return PVPShopRefreshTime;
	}
	/**
	 * 挂机PVP商店刷新次数
	 */
	public void setPVPShopRefreshTime(int refreshTime) {
		PVPShopRefreshTime = refreshTime;
	}
	/**
	 * 天梯商店刷新次数
	 */
	public int getLadderShopRefreshTime() {
		return ladderShopRefreshTime;
	}
	/**
	 * 天梯商店刷新次数
	 */
	public void setLadderShopRefreshTime(int refreshTime) {
		ladderShopRefreshTime = refreshTime;
	}
	/**
	 * 远征商店刷新次数
	 */
	public int getExpeditionShopRefreshTime() {
		return expeditionShopRefreshTime;
	}
	/**
	 * 远征商店刷新次数
	 */
	public void setExpeditionShopRefreshTime(int refreshTime) {
		expeditionShopRefreshTime = refreshTime;
	}
	/**
	 * 点石成金次数
	 */
	public int getPurchaseCoinTime() {
		return purchaseCoinTime;
	}
	/**
	 * 点石成金次数
	 */
	public void setPurchaseCoinTime(int time) {
		purchaseCoinTime = time;
	}
	/**
	 * 点石成金剩余次数
	 */
	public int getPurchaseCoinLeft(){
		return purchaseCoinLeft;
	}
	/**
	 * 点石成金剩余次数
	 */
	public void setPurchaseCoinLeft(int time){
		purchaseCoinLeft = time;
	}
	public long getFreeLotteryCoinTime() {
		return freeLotteryCoinTime;
	}
	public void setFreeLotteryCoinTime(long freeLotteryCoinTime) {
		this.freeLotteryCoinTime = freeLotteryCoinTime;
	}
	public int getFreeLotteryCoinLeftTime() {
		return freeLotteryCoinLeftTime;
	}
	public void setFreeLotteryCoinLeftTime(int freeLotteryCoinLeftTime) {
		this.freeLotteryCoinLeftTime = freeLotteryCoinLeftTime;
	}
	public long getFreeLotteryJewelTime() {
		return freeLotteryJewelTime;
	}
	public void setFreeLotteryJewelTime(long freeLotteryJewelTime) {
		this.freeLotteryJewelTime = freeLotteryJewelTime;
	}
	/**
	 * 上次签到时间
	 */
	public String getLastSignTime() {
		return lastSignTime;
	}
	public void setLastSignTime(String lastSignTime) {
		this.lastSignTime = lastSignTime;
	}
	/**
	 * 签到次数
	 */
	public int getSignCount() {
		return signCount;
	}
	public void setSignCount(int signCount) {
		this.signCount = signCount;
	}
	/**
	 * 登录天数
	 */
	public int getLoginDays() {
		return loginDays;
	}
	public void setLoginDays(int loginDays) {
		this.loginDays = loginDays;
	}
	/**
	 * 组合技
	 */
	public String getComposeSkill() {
		return composeSkill;
	}
	public void setComposeSkill(String composeSkill) {
		this.composeSkill = composeSkill;
	}
	/**
	 * 充值的总金额
	 */
	public int getRechargeRecord() {
		return rechargeRecord;
	}
	public void setRechargeRecord(int rechargeRecord) {
		this.rechargeRecord = rechargeRecord;
	}
	/**
	 * 英雄的infoId，递增
	 */
	public int getHeroInfoId() {
		return heroInfoId;
	}
	public void setHeroInfoId(int heroInfoId) {
		this.heroInfoId = heroInfoId;
	}
	public int updateHeroInfoId() {
		heroInfoId++;
		return heroInfoId;
	}
	/**
	 * 首选英雄ID
	 */
	public int getFirstGetHeroId() {
		return firstGetHeroId;
	}
	public void setFirstGetHeroId(int firstGetHeroId) {
		this.firstGetHeroId = firstGetHeroId;
	}
	/**
	 * 总签到次数，不清0
	 * @return
	 */
	public int getTotalSignCount() {
		return totalSignCount;
	}
	public void setTotalSignCount(int totalSignCount) {
		this.totalSignCount = totalSignCount;
	}
	public String getGreenhand() {
		return greenhand;
	}
	public void setGreenhand(String greenhand) {
		this.greenhand = greenhand;
	}
	public int getAdvance() {
		return advance;
	}
	public void setAdvance(int advance) {
		this.advance = advance;
	}
	public int getShouchongIsComplete() {
		return shouchongIsComplete;
	}
	public void setShouchongIsComplete(int shouchongIsComplete) {
		this.shouchongIsComplete = shouchongIsComplete;
	}
	public int getShouchongIsGetReward() {
		return shouchongIsGetReward;
	}
	public void setShouchongIsGetReward(int shouchongIsGetReward) {
		this.shouchongIsGetReward = shouchongIsGetReward;
	}
	public int getSkill() {
		return skill;
	}
	public void setSkill(int skill) {
		this.skill = skill;
	}
	public int getFailed() {
		return failed;
	}
	public void setFailed(int failed) {
		this.failed = failed;
	}
	public UserBean init(int serverId, String account, String userName, int icon) {
		setAccount(account);
		setId(0);
		setServerId(serverId);
		setUserName(userName);
		setIcon(icon);
		setCoin(0);
		setJewel(0);
		setExp(0);
		setHeroLimit(30);
		setFreeLotteryCoinTime(System.currentTimeMillis());
		setFreeLotteryJewelTime(System.currentTimeMillis() - (long)(TimeConst.MILLIONSECONDS_PER_HOUR * (22 - 1)));
		return this;
	}
	public UserInfo buildShort() {
		UserInfo.Builder builder = UserInfo.newBuilder();
		builder.setId(id);
//		builder.setAccount(account);
		builder.setIcon(icon);
		builder.setName(name);
//		builder.setServerId(serverId);
		builder.setVip(vip);
		builder.setZhanli(zhanliMax);
		builder.setUnionId(unionId);
		builder.setUnionJob(unionJob);
		builder.setLastLoginTime(lastLoginTime);
		return builder.build();
	}
	
	public UserInfo buildUnionShort() {
		UserInfo.Builder builder = UserInfo.newBuilder();
		builder.setId(id);
//		builder.setAccount(account);
//		builder.setIcon(icon);
//		builder.setName(name);
//		builder.setServerId(serverId);
		builder.setUnionId(unionId);
//		builder.setUnionJob(unionJob);
//		builder.setZhanli(zhanliMax);
//		builder.setLastLoginTime(lastLoginTime);
		return builder.build();
	}
	
	public UserInfo buildUnionUser() {
		UserInfo.Builder builder = UserInfo.newBuilder();
		builder.setId(id);
//		builder.setAccount(account);
		builder.setIcon(icon);
		builder.setName(name);
//		builder.setServerId(serverId);
		builder.setUnionId(unionId);
		builder.setUnionJob(unionJob);
		builder.setZhanli(zhanliMax);
		builder.setLastLoginTime(lastLoginTime);
		return builder.build();
	}

	public UserInfo build() {
		UserInfo.Builder builder = UserInfo.newBuilder();
		builder.setId(id);
		builder.setAccount(account);
		builder.setSession(session);
		builder.setIcon(icon);
		builder.setName(name);
		builder.setServerId(serverId);
		builder.setUnionId(unionId);
		builder.setUnionJob(unionJob);
		builder.setJewel(jewel);
		builder.setCoin(coin);
		builder.setExp(exp);
		builder.setVip(vip);
		builder.setPointPVP(pointPVP);
		builder.setPointLadder(pointLadder);
		builder.setPointExpedition(pointExpedition);
		builder.setPointUnion(pointUnion);
		builder.setZhanli(zhanli);
//		builder.setReceiveMessageTimeStamp(receiveMessageTimeStamp);
//		builder.setLastLootTime(lastLootTime);
//		builder.setRefreshLeftTimes(refreshLeftTimes);
//		builder.setLastLoginTime(lastLoginTime);
		builder.setLadderPurchaseTimes(ladderPurchaseTimes);
		builder.setLadderModeLeftTimes(ladderModeLeftTimes);
//		builder.setLadderModeHistoryTop(ladderModeHistoryTop);
		builder.setFreeLotteryCoinTime(Math.max(0, (freeLotteryCoinTime-System.currentTimeMillis())/1000));
		builder.setFreeLotteryCoinLeftTime(freeLotteryCoinLeftTime);
		builder.setFreeLotteryJewelTime(Math.max(0, ((freeLotteryJewelTime + 22 * TimeConst.MILLIONSECONDS_PER_HOUR - System.currentTimeMillis()) / TimeConst.MILLIONSECONDS_PER_SECOND)));
		builder.setPVPMineLeftTime(pvpMineLeftTime);
		builder.setSignCount(signCount);
		builder.setLoginDays(loginDays);
		builder.setLastSignTime(lastSignTime);
		builder.setAreaEnergy(areaEnergy);
		builder.setAreaEnergyTime(areaEnergyTime);
		builder.setHeroLimit(heroLimit);
		builder.setComposeSkill(composeSkill);
		builder.setRechargeRecord(rechargeRecord);
		builder.setVipLibao1(viplibao1);
		builder.setVipLibao2(viplibao2);
		builder.setRegisterTime(registerTime);
		builder.setTotalSignCount(totalSignCount);
		builder.setFirstGetHeroId(firstGetHeroId);
		builder.setGreenhand(greenhand);
		builder.setAdvance(advance);
		builder.setShouchongIsComplete(shouchongIsComplete);
		builder.setShouchongIsGetReward(shouchongIsGetReward);
		for(int i = 1; i <= 6; i++){
			JewelPool.Builder pool = JewelPool.newBuilder();
			pool.setOrder(i);
			pool.setRecharged(growJewelCount);
			LibaoService.calJewelPoolRewarded(pool, growJewelCountStatus);
			builder.addGrowJewelCount(pool);

			pool.setOrder(i);
			pool.setRecharged(growExpCount);
			LibaoService.calJewelPoolRewarded(pool, growExpCountStatus);
			builder.addGrowExpCount(pool);
		}
		builder.setSkill(skill);
		builder.setFailed(failed);
		
		return builder.build();
	}
//	public Map<String, String> toMap() {
//		Map<String, String> userMap = new HashMap<String, String>();
//		userMap.put(ID, "" + id);
//		userMap.put(ACCOUNT, account);
//		userMap.put(ICON, ""+icon);
//		userMap.put(USERNAME, name);
//		userMap.put(SERVER_ID, "" + serverId);
//		userMap.put(UNION_ID, "" + unionId);
//		userMap.put(UNION_JOB, "" + unionJob);
//		userMap.put(JEWEL, "" + jewel);
//		userMap.put(COIN, "" + coin);
//		userMap.put(EXP, "" + exp);
//		userMap.put(VIP, "" + vip);
//		userMap.put(POINT_PVP, "" + pointPVP);
//		userMap.put(POINT_LADDER, "" + pointLadder);
//		userMap.put(POINT_EXPEDITION, "" + pointExpedition);
//		userMap.put(POINT_UNION, "" + pointUnion);
//		userMap.put(RECEIVE_MESSAGE_TIMESTAMP, "" + receiveMessageTimeStamp);
//		userMap.put(LAST_LOOT_TIME, "" + lastLootTime);
//		userMap.put(REFRESH_LEFT_TIMES, "" + refreshLeftTimes);
//		userMap.put(LAST_LOGIN_TIME, "" + lastLoginTime);
//		userMap.put(LADDER_MODE_LEFT_TIMES, "" + ladderModeLeftTimes);
//		userMap.put(LADDER_MODE_HISTORY_TOP, "" + ladderModeHistoryTop);
//		userMap.put(FREE_LOTTERY_COIN_TIME, "" + freeLotteryCoinTime);
//		userMap.put(FREE_LOTTERY_JEWEL_TIME, "" + freeLotteryJewelTime);
//		
//		return userMap;
//	}
//	
//	public static UserBean convertUserMapToUserBean(Map<String, String> userMap) {
//		if (userMap == null || userMap.size() == 0)
//			return null;
//		
//		UserBean userBean = new UserBean();
//		userBean.setId(TypeTranslatedUtil.stringToLong(userMap.get(ID)));
//		userBean.setAccount(userMap.get(ACCOUNT));
//		userBean.setIcon(TypeTranslatedUtil.stringToInt(userMap.get(ICON)));
//		userBean.setUserName(userMap.get(USERNAME));
//		userBean.setServerId(TypeTranslatedUtil.stringToInt(userMap.get(SERVER_ID)));
//		userBean.setUnionId(TypeTranslatedUtil.stringToInt(userMap.get(UNION_ID)));
//		userBean.setUnionJob(TypeTranslatedUtil.stringToInt(userMap.get(UNION_JOB)));
//		userBean.setJewel(TypeTranslatedUtil.stringToInt(userMap.get(JEWEL)));
//		userBean.setCoin(TypeTranslatedUtil.stringToInt(userMap.get(COIN)));
//		userBean.setExp(TypeTranslatedUtil.stringToInt(userMap.get(EXP)));
//		userBean.setVip(TypeTranslatedUtil.stringToInt(userMap.get(VIP)));
//		userBean.setPointPVP(TypeTranslatedUtil.stringToInt(userMap.get(POINT_PVP)));
//		userBean.setPointLadder(TypeTranslatedUtil.stringToInt(userMap.get(POINT_LADDER)));
//		userBean.setPointExpedition(TypeTranslatedUtil.stringToInt(userMap.get(POINT_EXPEDITION)));
//		userBean.setPointUnion(TypeTranslatedUtil.stringToInt(userMap.get(POINT_UNION)));
//		userBean.setReceiveMessageTimeStamp(TypeTranslatedUtil.stringToInt(userMap.get(RECEIVE_MESSAGE_TIMESTAMP)));
//		userBean.setLastLootTime(TypeTranslatedUtil.stringToInt(userMap.get(LAST_LOOT_TIME)));
//		userBean.setRefreshLeftTimes(TypeTranslatedUtil.stringToInt(userMap.get(REFRESH_LEFT_TIMES)));
//		userBean.setLastLoginTime(userMap.get(LAST_LOGIN_TIME));
//		userBean.setLadderModeLeftTimes(TypeTranslatedUtil.stringToInt(userMap.get(LADDER_MODE_LEFT_TIMES)));
//		userBean.setLadderModeHistoryTop(TypeTranslatedUtil.stringToLong(userMap.get(LADDER_MODE_HISTORY_TOP)));
//		userBean.setFreeLotteryCoinTime(TypeTranslatedUtil.stringToLong(userMap.get(FREE_LOTTERY_COIN_TIME)));
//		userBean.setFreeLotteryJewelTime(TypeTranslatedUtil.stringToLong(userMap.get(FREE_LOTTERY_JEWEL_TIME)));
//		
//		return userBean;
//	}
//
//	private final static String ID = "id";
//	private final static String ICON = "icon";
//	private final static String ACCOUNT = "account";
//	private final static String USERNAME = "name";
//	private final static String SERVER_ID = "server_id";
//	private final static String UNION_ID = "union_id";
//	private final static String UNION_JOB = "union_job";
//	private final static String JEWEL = "jewel";
//	private final static String COIN = "coin";
//	private final static String EXP = "exp";
//	private final static String VIP = "vip";
//	private final static String POINT_PVP = "point_pvp";
//	private final static String POINT_LADDER = "point_ladder";
//	private final static String POINT_EXPEDITION = "point_expedition";
//	private final static String POINT_UNION = "point_union";
//	private final static String RECEIVE_MESSAGE_TIMESTAMP = "receive_message_timestamp";
//	private final static String LAST_LOOT_TIME = "last_loot_time";
//	private final static String REFRESH_LEFT_TIMES = "refresh_left_times";
//	private final static String LAST_LOGIN_TIME = "last_login_time";
//	private final static String LADDER_MODE_LEFT_TIMES = "ladder_mode_left_times";
//	private final static String LADDER_MODE_HISTORY_TOP = "ladder_mode_history_mode";
//	private final static String FREE_LOTTERY_COIN_TIME = "free_lottery_coin_time";
//	private final static String FREE_LOTTERY_JEWEL_TIME = "free_lottery_jewel_time";
}
