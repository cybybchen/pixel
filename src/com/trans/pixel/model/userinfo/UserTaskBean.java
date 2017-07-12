package com.trans.pixel.model.userinfo;

import java.util.ArrayList;
import java.util.List;

import com.trans.pixel.protoc.TaskProto.UserTask;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class UserTaskBean {
	private long id = 0;
	private long userId = 0;
	private int targetId = 0;
	private int targetCount = 0;
	private String targetRecord = "";
//	public UserTaskBean(long id, long userId, int targetId, int targetCount, String targetRecord) {
//		this.id = id;
//		this.userId = userId;
//		this.targetId = targetId;
//		this.targetCount = targetCount;
//		this.targetRecord = targetRecord;
//	}
	public static UserTaskBean init(long userId, UserTask userTask) {
		UserTaskBean ut = new UserTaskBean();
		ut.setUserId(userId);
		ut.setTargetId(userTask.getTargetid());
		ut.setTargetCount(userTask.getProcess());
		ut.setTargetRecord(composeTargetRecord(userTask.getHeroidList()));
		
		return ut;
	}
	
	public UserTask build() {
		UserTask.Builder builder = UserTask.newBuilder();
		builder.setTargetid(targetId);
		builder.setProcess(targetCount);
		builder.addAllHeroid(splitHeroId(targetRecord));
		
		return builder.build();
	}
	
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
	public int getTargetId() {
		return targetId;
	}
	public void setTargetId(int targetId) {
		this.targetId = targetId;
	}
	public int getTargetCount() {
		return targetCount;
	}
	public void setTargetCount(int targetCount) {
		this.targetCount = targetCount;
	}
	public String getTargetRecord() {
		return targetRecord;
	}
	public void setTargetRecord(String targetRecord) {
		this.targetRecord = targetRecord;
	}
	private static String composeTargetRecord(List<Integer> heroIdList) {
		String record = "";
		for (int heroId : heroIdList) {
			record += SPLIT + heroId;
		}
		
		if (record.isEmpty())
			return "";
		 
		return record.substring(1);
	}
	private List<Integer> splitHeroId(String record) {
		List<Integer> heroIdList = new ArrayList<Integer>();
		if (record.isEmpty())
			return heroIdList;
		String[] heroIds = record.split(SPLIT);
		for (String heroIdStr : heroIds) {
			heroIdList.add(TypeTranslatedUtil.stringToInt(heroIdStr));
		}
		
		return heroIdList;
	}
	private static final String SPLIT = "\\|";
}
