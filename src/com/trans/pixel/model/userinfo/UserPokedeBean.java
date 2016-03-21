package com.trans.pixel.model.userinfo;

import com.trans.pixel.protoc.Commands.HeroInfo;

public class UserPokedeBean {
	private long userId = 0;
	private int heroId = 0;
	private int rare = 0;
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
	
	public HeroInfo buildUserPokede() {
		HeroInfo.Builder builder = HeroInfo.newBuilder();
		
		builder.setHeroId(heroId);
		builder.setRare(rare);
		
		return builder.build();
	}
}
