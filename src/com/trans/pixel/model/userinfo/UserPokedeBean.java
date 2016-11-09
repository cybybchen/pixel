package com.trans.pixel.model.userinfo;

import java.util.List;

import net.sf.json.JSONObject;

import com.trans.pixel.protoc.Commands.HeroInfo;

public class UserPokedeBean {
	private long userId = 0;
	private int heroId = 0;
	private int rare = 0;
	private int level = 0;
	private int count = 0;
	private int strengthen = 0;
	private int rank = 0;
	private String fetters = "";
	private int star = 0;
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public int getHeroId() {
		return heroId;
	}
	public void setHeroId(int heroId) {
		this.heroId = heroId;
	}
	public int getRare() {
		return rare;
	}
	public void setRare(int rare) {
		this.rare = rare;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getStrengthen() {
		return strengthen;
	}
	public void setStrengthen(int strengthen) {
		this.strengthen = strengthen;
	}
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public String getFetters() {
		return fetters;
	}
	public void setFetters(String fetters) {
		this.fetters = fetters;
	}
	public int getStar() {
		return star;
	}
	public void setStar(int star) {
		this.star = star;
	}
	public HeroInfo buildUserPokede(List<UserClearBean> userClearList) {
		HeroInfo.Builder builder = HeroInfo.newBuilder();
		
		builder.setHeroId(heroId);
		builder.setRare(rare);
		builder.setLevel(level);
		builder.setCount(count);
		for (UserClearBean userClear : userClearList) {
			builder.addClear(userClear.buildUserClear());
		}
		builder.setStrengthen(strengthen);
		builder.setRank(rank);
		builder.setStar(star);
		builder.setFetters(fetters);
		
		return builder.build();
	}
	
	public static UserPokedeBean fromJson(String value) {
		if (value == null)
			return null;
		JSONObject json = JSONObject.fromObject(value);
		return (UserPokedeBean) JSONObject.toBean(json, UserPokedeBean.class);
	}
	
	public void addFetterId(int fetterId) {
		if (fetters.isEmpty())
			fetters = fetters + fetterId;
		else
			fetters = fetters + "," + fetterId;
	}
	
	public boolean hasOpenedFetter(int fetterId) {
		String[] fetterIds = fetters.split(",");
		for (String fetter : fetterIds) {
			if (fetter.equals("" + fetterId))
				return true;
		}
		
		return false;
	}
}
