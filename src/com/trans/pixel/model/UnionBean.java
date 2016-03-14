package com.trans.pixel.model;

import com.trans.pixel.protoc.Commands.Union;

public class UnionBean {
	private int id = 0;
	private String name = "";
	private int level = 0;
	private int serverId = 0;
	private int icon = 0;
	private int point = 0;
	private int rank = 0;
//	private List<UnionUserBean> unionUserList = new ArrayList<UnionUserBean>();
//	private List<MailBean> mailList = new ArrayList<MailBean>();
	public UnionBean(){
		
	}
	public UnionBean(Union union){
		setId(union.getId());
		setName(union.getName());
		setLevel(union.getLevel());
		setPoint(union.getPoint());
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
	
	public Union build() {
		Union.Builder union = Union.newBuilder();
		union.setId(id);
		union.setRank(rank);
		union.setName(name);
		union.setIcon(icon);
		union.setLevel(level);
		union.setPoint(point);
		union.setCount(1);
		union.setMaxCount(30);
		
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

//	private static final String ID = "id";
//	private static final String NAME = "name";
//	private static final String ICON = "icon";
//	private static final String LEVEL = "level";
//	private static final String POINT = "point";
//	private static final String SERVER_ID = "server_id";
//	private static final String UNION_USER_LIST = "union_user_list";
//	private static final String MAIL_LIST = "mail_list";
}
