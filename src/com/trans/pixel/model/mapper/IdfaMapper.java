package com.trans.pixel.model.mapper;

import com.trans.pixel.model.IdfaBean;

public interface IdfaMapper {

	public void addIdfa(IdfaBean idfa);
	
	public IdfaBean queryIdfa(String idfa);
	
	public void updateStatus(IdfaBean idfa);
}
