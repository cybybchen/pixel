package com.trans.pixel.model.mapper;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.UnionBean;

public interface UnionMapper {
	public UnionBean selectUnionById(@Param("id") int id);
	
	public UnionBean selectUnionByServerIdAndName(@Param("server_id") int serverId, @Param("name") String name);
	
	public void createUnion(UnionBean unionBean);
	
	public void updateUnion(UnionBean unionBean);
}
