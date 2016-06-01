package com.trans.pixel.model.mapper;

import java.util.List;

import com.trans.pixel.model.userinfo.UserLibaoBean;

public interface UserLibaoMapper {
	
	public List<UserLibaoBean> queryById(long userId);
	
	public int update(UserLibaoBean libao);
}
