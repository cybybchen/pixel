package com.trans.pixel.model.mapper;

import org.apache.ibatis.annotations.Param;

public interface IdfaMapper {

	public void addIdfa(@Param("idfa") String idfa, @Param("ip") String ip, @Param("callback") String callback);
	
	public String queryIdfa(String idfa);
}
