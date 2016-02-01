package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.UnionBean;

public interface UnionMapper {
	public UnionBean selectUnionById(@Param("id") int id);
	
	public UnionBean selectUnionByServerIdAndName(@Param("serverid") int serverId, @Param("name") String name);

	public List<UnionBean> selectUnionsByServerId(@Param("serverid") int serverId);
	
	public void createUnion(UnionBean unionBean);
	
	public void updateUnion(UnionBean unionBean);
}
