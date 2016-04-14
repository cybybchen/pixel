package com.trans.pixel.model.userinfo;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.protoc.Commands.HeroInfo;
import com.trans.pixel.protoc.Commands.UserHero;
import com.trans.pixel.utils.TypeTranslatedUtil;

public class UserHeroBean {
	private long id = 0;
	private long userId = 0;
	private int heroId = 0;
	private String heroInfo = "{}";
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
	public int getHeroId() {
		return heroId;
	}
	public void setHeroId(int heroId) {
		this.heroId = heroId;
	}
	public String getHeroInfo() {
		return heroInfo;
	}
	public void setHeroInfo(String heroInfo) {
		this.heroInfo = heroInfo;
	}
	
	public List<Integer> refreshNewHeroInfo() {
		List<Integer> newHeroInfoIdList = new ArrayList<Integer>();
		List<HeroInfoBean> heroInfoList = toHeroInfoList();
		for (HeroInfoBean heroInfo : heroInfoList) {
			if (heroInfo.isNew()) {
				heroInfo.setNew(false);
				newHeroInfoIdList.add(heroInfo.getId());
			}
		}
		
		updateHeroInfo(heroInfoList);
		
		return newHeroInfoIdList;
	}
	
	public void clearOld(List<Integer> infoIds) {
		List<HeroInfoBean> heroInfoList = toHeroInfoList();
		for (int i = 0; i < heroInfoList.size(); ++ i) {
			HeroInfoBean heroInfo = heroInfoList.get(i);
			if (!infoIds.contains(heroInfo.getId())) {
				heroInfoList.remove(i);
				--i;
			}	
		}
		
		updateHeroInfo(heroInfoList);
	}
	
	public void delHeros(List<Integer> infoIds) {
		List<HeroInfoBean> heroInfoList = toHeroInfoList();
		for (int i = 0; i < heroInfoList.size(); ++ i) {
			HeroInfoBean heroInfo = heroInfoList.get(i);
			if (infoIds.contains(heroInfo.getId())) {
				heroInfoList.remove(i);
				--i;
			}	
		}
		
		updateHeroInfo(heroInfoList);
	}
	
	public HeroInfoBean getHeroInfoByInfoId(int infoId) {
		List<HeroInfoBean> heroInfoList = toHeroInfoList();
		for (HeroInfoBean heroInfo : heroInfoList) {
			if (heroInfo.getId() == infoId)
				return heroInfo;
		}
		
		return null;
	}
	
	public void updateHeroInfo(HeroInfoBean hero) {
		List<HeroInfoBean> heroInfoList = toHeroInfoList();
		for (HeroInfoBean heroInfo : heroInfoList) {
			if (heroInfo.getId() == hero.getId()) {
				heroInfo.copy(hero);
				updateHeroInfo(heroInfoList);
				return ;
			}
		}
		
		hero.setId(calInfoId());
		heroInfoList.add(hero);
		updateHeroInfo(heroInfoList);
	}
	
	private int calInfoId() {
		List<HeroInfoBean> heroInfoList = toHeroInfoList();
		int infoId = 1;
		while (true) {
			int calId = infoId;
			for (HeroInfoBean hero : heroInfoList) {
				if (hero.getId() == infoId) {
					++calId;
					break;
				}
			}
			
			if (calId > infoId) 
				infoId = calId;
			else 
				break;
		}
		
		return infoId;
	}
	
	private void updateHeroInfo(List<HeroInfoBean> heroInfoList) {
		JSONObject json = new JSONObject();
		json.put(HERO_INFO_LIST, heroInfoList);
		
		heroInfo = json.toString();
	}
	
	private List<HeroInfoBean> toHeroInfoList() {
		List<HeroInfoBean> heroInfoList = new ArrayList<HeroInfoBean>();
		JSONObject json = JSONObject.fromObject(heroInfo);
		JSONArray array = TypeTranslatedUtil.jsonGetArray(json, HERO_INFO_LIST);
		for (int i = 0;i < array.size(); ++i) {
			HeroInfoBean heroInfo = HeroInfoBean.fromJson(array.getString(i));
			heroInfoList.add(heroInfo);
		}
		
		return heroInfoList;
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(USER_ID, userId);
		json.put(HERO_ID, heroId);
		json.put(HERO_INFO, heroInfo);
		
		return json.toString();
	}
	public static UserHeroBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		UserHeroBean bean = new UserHeroBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getLong(ID));
		bean.setUserId(json.getLong(USER_ID));
		bean.setHeroId(json.getInt(HERO_ID));
		bean.setHeroInfo(json.getString(HERO_INFO));

		return bean;
	}
	
	public UserHero buildUserHero() {
		UserHero.Builder builder = UserHero.newBuilder();
		List<HeroInfo> heroInfoBuilderList = new ArrayList<HeroInfo>();
		List<HeroInfoBean> heroInfoList = toHeroInfoList();
		for (HeroInfoBean heroInfo : heroInfoList) {
			HeroInfo heroInfoBuilder = heroInfo.buildHeroInfo();
			heroInfoBuilderList.add(heroInfoBuilder);
			
		}
		builder.setUserId(userId);
		builder.setHeroId(heroId);
		builder.addAllHeroInfo(heroInfoBuilderList);
		
		return builder.build();
	}
	
	private static final String ID = "id";
	private static final String USER_ID = "user_id";
	private static final String HERO_ID = "hero_id";
	private static final String HERO_INFO = "hero_info";
	private static final String HERO_INFO_LIST = "hero_info_list";
}
