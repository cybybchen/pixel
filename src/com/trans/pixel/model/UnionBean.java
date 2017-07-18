package com.trans.pixel.model;

import com.trans.pixel.protoc.UnionProto.Union;

public class UnionBean {
	private int id = 0;
	private String name = "";
	private int level = 0;
	private int serverId = 0;
	private int icon = 0;
	private int point = 0;
	private int rank = 0;
	private String killMonsterRecord = "";
	private String costRecord = "";
	private String bossRecord = "";
	private String bossEndTime = "";
	private int maxCount = 0;
	// private List<UnionUserBean> unionUserList = new
	// ArrayList<UnionUserBean>();
	// private List<MailBean> mailList = new ArrayList<MailBean>();
	private int exp = 0;

	public UnionBean() {

	}

	public UnionBean(Union.Builder union) {
		setId(union.getId());
		setName(union.getName());
		setLevel(union.getLevel());
		setPoint(union.getPoint());
		setKillMonsterRecord(union.getKillMonsterRecord());
		setCostRecord(union.getCostRecord());
		setBossRecord(union.getBossRecord());
		setBossEndTime(union.getBossEndTime());
		setExp(union.getExp());
		setMaxCount(union.getMaxCount());
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public int getPoint() {
		return point;
	}

	public void setPoint(int point) {
		this.point = point;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public String getKillMonsterRecord() {
		return killMonsterRecord;
	}

	public void setKillMonsterRecord(String killMonsterRecord) {
		this.killMonsterRecord = killMonsterRecord;
	}

	public String getCostRecord() {
		return costRecord;
	}

	public void setCostRecord(String costRecord) {
		this.costRecord = costRecord;
	}

	public String getBossRecord() {
		return bossRecord;
	}

	public void setBossRecord(String bossRecord) {
		this.bossRecord = bossRecord;
	}

	public String getBossEndTime() {
		return bossEndTime;
	}

	public void setBossEndTime(String bossEndTime) {
		this.bossEndTime = bossEndTime;
	}

	// public void updateKillMonsterRecord(int targetId, int count) {
	// int targetCount = 0;
	// JSONObject json = new JSONObject();
	// try {
	// json = JSONObject.fromObject(killMonsterRecord);
	// targetCount = json.getInt("" + targetId) + count;
	// } catch (Exception e) {

	// }
	// json.put("" + targetId, targetCount);
	// killMonsterRecord = json.toString();
	// }
	// public void updateCostRecord(int targetId, int count) {
	// int targetCount = 0;
	// JSONObject json = new JSONObject();
	// try {
	// json = JSONObject.fromObject(costRecord);
	// targetCount = json.getInt("" + targetId) + count;
	// } catch (Exception e) {

	// }
	// json.put("" + targetId, targetCount);
	// costRecord = json.toString();
	// }
	// public void updateUnionBossRecord(int bossId) {
	// int bossCount = 0;
	// JSONObject json = new JSONObject();
	// try {
	// json = JSONObject.fromObject(bossRecord);
	// bossCount = json.getInt("" + bossId) + 1;
	// } catch (Exception e) {

	// }
	// json.put("" + bossId, bossCount);
	// bossRecord = json.toString();
	// }
	// public int getUnionBossCount(int bossId) {
	// int bossCount = 0;
	// JSONObject json = new JSONObject();
	// try {
	// json = JSONObject.fromObject(bossRecord);
	// bossCount = json.getInt("" + bossId);
	// } catch (Exception e) {

	// }
	// return bossCount;
	// }
	// public void updateUnionBossEndTime(int bossId) {
	// JSONObject json = new JSONObject();
	// try {
	// json = JSONObject.fromObject(bossEndTime);
	// json.put("" + bossId, DateUtil.getCurrentDateString());
	// } catch (Exception e) {

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public int getMaxCount() {
		return maxCount;
	}

	public void setMaxCount(int maxCount) {
		this.maxCount = maxCount;
	}

	// }
	// bossEndTime = json.toString();
	// }
	// public String getUnionBossEndTime(int bossId) {
	// JSONObject json = new JSONObject();
	// String time = "";
	// try {
	// json = JSONObject.fromObject(bossEndTime);
	// time = json.getString("" + bossId);
	// } catch (Exception e) {
	// time = "";
	// }
	// return time;
	// }
	public Union build() {
		Union.Builder union = Union.newBuilder();
		union.setId(id);
		union.setRank(rank);
		union.setName(name);
		union.setIcon(icon);
		union.setLevel(level);
		union.setPoint(point);
		union.setCount(1);
		union.setMaxCount(maxCount);
		union.setZhanli(0);
		union.setKillMonsterRecord(killMonsterRecord);
		union.setCostRecord(costRecord);
		union.setBossRecord(bossRecord);
		union.setBossEndTime(bossEndTime);
		union.setExp(exp);

		return union.build();
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	// private static final String ID = "id";
	// private static final String NAME = "name";
	// private static final String ICON = "icon";
	// private static final String LEVEL = "level";
	// private static final String POINT = "point";
	// private static final String SERVER_ID = "server_id";
	// private static final String UNION_USER_LIST = "union_user_list";
	// private static final String MAIL_LIST = "mail_list";
}
