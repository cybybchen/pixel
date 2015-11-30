package com.trans.pixel.model.mapper;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.AccountBean;

public interface AccountMapper {
	public long queryUserId(@Param("serverId") int serverId, @Param("account") String account);
	
	public long registerAccount(AccountBean accountBean);
}
