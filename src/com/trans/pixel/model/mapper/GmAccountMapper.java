package com.trans.pixel.model.mapper;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.GmAccountBean;

public interface GmAccountMapper {
	public GmAccountBean queryGmAccount(@Param("account")String account);
	
	public void updateGmAccount(GmAccountBean accountBean);
}
