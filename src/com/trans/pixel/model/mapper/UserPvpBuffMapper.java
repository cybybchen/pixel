package com.trans.pixel.model.mapper;

import java.util.List;

import com.trans.pixel.model.userinfo.UserPvpBuffBean;

public interface UserPvpBuffMapper {
	public List<UserPvpBuffBean> getUserPvpBuff(long userId);
	public int updateUserPvpBuff(UserPvpBuffBean bean);
}