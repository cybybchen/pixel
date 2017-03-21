package com.trans.pixel.model.userinfo;

import net.sf.json.JSONObject;

import com.trans.pixel.protoc.EquipProto.UserEquip;

public class UserEquipBean {
	private long id = 0;
	private long userId = 0;
	private int equipId = 0;
	private int equipCount = 0;
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
	public int getEquipId() {
		return equipId;
	}
	public void setEquipId(int equipId) {
		this.equipId = equipId;
	}
	public int getEquipCount() {
		return equipCount;
	}
	public void setEquipCount(int equipCount) {
		this.equipCount = equipCount;
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		json.put(ID, id);
		json.put(USER_ID, userId);
		json.put(EQUIP_ID, equipId);
		json.put(EQUIP_COUNT, equipCount);
		
		return json.toString();
	}
	public static UserEquipBean fromJson(String jsonString) {
		if (jsonString == null)
			return null;
		UserEquipBean bean = new UserEquipBean();
		JSONObject json = JSONObject.fromObject(jsonString);
		
		bean.setId(json.getLong(ID));
		bean.setUserId(json.getLong(USER_ID));
		bean.setEquipId(json.getInt(EQUIP_ID));
		bean.setEquipCount(json.getInt(EQUIP_COUNT));

		return bean;
	}
	
	public UserEquip buildUserEquip() {
		UserEquip.Builder builder = UserEquip.newBuilder();
		
		builder.setEquipId(equipId);
		builder.setEquipCount(equipCount);
		
		return builder.build();
	}
	
	public static UserEquipBean initUserEquip(int equipId, int equipCount) {
		UserEquipBean userEquip = new UserEquipBean();
		userEquip.setEquipId(equipId);
		userEquip.setEquipCount(equipCount);
		
		return userEquip;
	}
	
	private static final String ID = "id";
	private static final String USER_ID = "user_id";
	private static final String EQUIP_ID = "equip_id";
	private static final String EQUIP_COUNT = "equip_count";
}
