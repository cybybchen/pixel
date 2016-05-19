package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface CdkeyConfigMapper {
	public String selectById(@Param("id") int id);

	public List<String> selectAll();
	
	public void update(@Param("id") int id, @Param("cdkey") String cdkey);

	public void delete(@Param("id") int id);
}
