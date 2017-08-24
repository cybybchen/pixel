package com.trans.pixel.model.userinfo;

import com.trans.pixel.protoc.Base.FightInfo;
import com.trans.pixel.service.redis.RedisService;

public class UserFightInfoBean {
	private long it = 0;
	private long userId = 0;
	private int fightinfoId = 0;
	private String fightinfo = "";
	public UserFightInfoBean() {
		
	}
	public UserFightInfoBean(long userId, FightInfo fightInfo) {
		setFightinfoId(fightInfo.getId());
		setFightinfo(RedisService.formatJson(fightInfo));
		setUserId(userId);
	}
	public FightInfo build() {
		FightInfo.Builder builder = FightInfo.newBuilder();
		if (RedisService.parseJson(fightinfo, builder))
			return builder.build();
		
		return null;
	}
	public long getIt() {
		return it;
	}
	public void setIt(long it) {
		this.it = it;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public int getFightinfoId() {
		return fightinfoId;
	}
	public void setFightinfoId(int fightinfoId) {
		this.fightinfoId = fightinfoId;
	}
	public String getFightinfo() {
		return fightinfo;
	}
	public void setFightinfo(String fightinfo) {
		this.fightinfo = fightinfo;
	}
}
