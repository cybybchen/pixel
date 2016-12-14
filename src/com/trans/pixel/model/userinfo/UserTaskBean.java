package com.trans.pixel.model.userinfo;

import java.util.ArrayList;
import java.util.List;

import com.trans.pixel.protoc.Commands.UserTask;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class UserTaskBean {
	private long id = 0;
	private long userId = 0;
	private int targetId = 0;
	private int targetCount = 0;
	private String targetRecord = "";
	public UserTaskBean(long userId, UserTask userTask) {
		this.userId = userId;
		this.targetId = userTask.getTargetid();
		this.targetCount = userTask.getProcess();
		this.targetRecord = composeTargetRecord(userTask.getHeroidList());
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
	private String composeTargetRecord(List<Integer> heroIdList) {
		String record = "";
		for (int heroId : heroIdList) {
			record += SPLIT + heroId;
		}
		
		return record.substring(1);
	}
	private List<Integer> splitHeroId(String record) {
		List<Integer> heroIdList = new ArrayList<Integer>();
		String[] heroIds = record.split(SPLIT);
		for (String heroIdStr : heroIds) {
			heroIdList.add(TypeTranslatedUtil.stringToInt(heroIdStr));
		}
		
		return heroIdList;
	}
	private static final String SPLIT = "|";
}
