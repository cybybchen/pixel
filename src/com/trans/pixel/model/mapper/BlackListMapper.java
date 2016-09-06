package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.BlackListBean;

public interface BlackListMapper {
	public BlackListBean selectBlackListById(@Param("userId") long userId);
	
	public List<BlackListBean> selectBlackLists();
	
	public void updateBlackList(BlackListBean blacklistbean);

	public void deleteBlackList(@Param("userId") long userId);
}
