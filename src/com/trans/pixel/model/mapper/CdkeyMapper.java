package com.trans.pixel.model.mapper;

import org.apache.ibatis.annotations.Param;

public interface CdkeyMapper {
	public String selectById(@Param("id") long id);
	
	public void update(@Param("id") long id, @Param("cdkey") String cdkey);
}
