package com.trans.pixel.model;

public class UserRoleBean {
	public long id = 0;
	public long userId = 0;
	public int roleId = 0;
	public int level = 0;
	public int star = 0;
	public int grade = 0;
	public String skillDes = "";
	public int weaponLevel = 0;
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
	public int getRoleId() {
		return roleId;
	}
	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getStar() {
		return star;
	}
	public void setStar(int star) {
		this.star = star;
	}
	public int getGrade() {
		return grade;
	}
	public void setGrade(int grade) {
		this.grade = grade;
	}
	public String getSkillDes() {
		return skillDes;
	}
	public void setSkillDes(String skillDes) {
		this.skillDes = skillDes;
	}
	public int getWeaponLevel() {
		return weaponLevel;
	}
	public void setWeaponLevel(int weaponLevel) {
		this.weaponLevel = weaponLevel;
	}
}
