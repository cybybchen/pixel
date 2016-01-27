package com.trans.pixel.model;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.trans.pixel.model.userinfo.UnionUserBean;
import com.trans.pixel.protoc.Commands.Mail;
import com.trans.pixel.protoc.Commands.Union;
import com.trans.pixel.protoc.Commands.UnionUser;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class UnionBean {
	private int id = 0;
	private String unionName = "";
	private int rank = 0;
	private int serverId = 0;
	private List<UnionUserBean> unionUserList = new ArrayList<UnionUserBean>();
	private List<MailBean> mailList = new ArrayList<MailBean>();
	private int point = 0;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUnionName() {
		return unionName;
	}
	public void setUnionName(String unionName) {
		this.unionName = unionName;
	}
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public int getServerId() {
		return serverId;
	}
	public void setServerId(int serverId) {
		this.serverId = serverId;
	}
	public List<UnionUserBean> getUnionUserList() {
		return unionUserList;
	}
	public void setUnionUserList(List<UnionUserBean> unionUserList) {
		this.unionUserList = unionUserList;
	}
	public List<MailBean> getMailList() {
		return mailList;
	}
	public void setMailList(List<MailBean> mailList) {
		this.mailList = mailList;
	}
	public int getPoint() {
		return point;
	}
	public void setPoint(int point) {
		this.point = point;
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(UNION_NAME, unionName);
		json.put(RANK, rank);
		json.put(SERVER_ID, serverId);
		json.put(UNION_USER_LIST, unionUserList);
		json.put(MAIL_LIST, mailList);
		
		return json.toString();
	}
	public static UnionBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		UnionBean bean = new UnionBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getInt(ID));
		bean.setUnionName(json.getString(UNION_NAME));
		bean.setRank(json.getInt(RANK));
		bean.setServerId(json.getInt(SERVER_ID));
		List<UnionUserBean> list1 = new ArrayList<UnionUserBean>();
		JSONArray array1 = TypeTranslatedUtil.jsonGetArray(json, UNION_USER_LIST);
		for (int i = 0;i < array1.size(); ++i) {
			UnionUserBean unionUser = UnionUserBean.fromJson(array1.getString(i));
			list1.add(unionUser);
		}
		bean.setUnionUserList(list1);
		
		List<MailBean> list2 = new ArrayList<MailBean>();
		JSONArray array2 = TypeTranslatedUtil.jsonGetArray(json, MAIL_LIST);
		for (int i = 0;i < array2.size(); ++i) {
			MailBean mail = MailBean.fromJson(array2.getString(i));
			list2.add(mail);
		}
		bean.setMailList(list2);

		return bean;
	}
	
	public void deleteMail(MailBean mail) {
		mailList.remove(mail);
	}
	
	public MailBean getMail(int mailId) {
		for (MailBean mail : mailList) {
			if (mail.getId() == mailId) {
				return mail;
			}
		}
		
		return null;
	}
	
	public void addUnionUser(UnionUserBean unionUser) {
		unionUserList.add(unionUser);
	}
	
	public Union buildUnion() {
		Union.Builder union = Union.newBuilder();
		union.setId(id);
		union.setRank(rank);
		
		List<Mail> mailBuilderList = new ArrayList<Mail>();
		for (MailBean mail : mailList) {
			mailBuilderList.add(mail.buildMail());
		}
		union.addAllMail(mailBuilderList);
		
		List<UnionUser> unionUserBuilderList = new ArrayList<UnionUser>();
		for (UnionUserBean user : unionUserList) {
			unionUserBuilderList.add(user.buildUnionUser());
		}
		union.addAllUnionUser(unionUserBuilderList);
		
		return union.build();
	}
	
	private static final String ID = "id";
	private static final String UNION_NAME = "union_name";
	private static final String RANK = "rank";
	private static final String SERVER_ID = "server_id";
	private static final String UNION_USER_LIST = "union_user_list";
	private static final String MAIL_LIST = "mail_list";
}
