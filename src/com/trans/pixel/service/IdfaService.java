package com.trans.pixel.service;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.IdfaBean;
import com.trans.pixel.model.mapper.IdfaMapper;

@Service
public class IdfaService {

	@Resource
	private IdfaMapper idfaMapper;
	public void addIdfa(Map<String, String> params) {
		IdfaBean idfa = initIdfa(params);
		idfaMapper.addIdfa(idfa);
	}
	
	public int queryIdfa(String idfa) {
		Object ret = idfaMapper.queryIdfa(idfa);
		if (ret == null)
			return 0;
		else 
			return 1;
	}
	
	public void updateIdfaStatus(String idfa) {
		Object ret = idfaMapper.queryIdfa(idfa);
		if (ret == null)
			return;
		
		IdfaBean idfaBean = (IdfaBean) ret;
		if (idfaBean.getIsRecall() == 0) {
			idfaBean.setIsRecall(1);
			idfaMapper.updateStatus(idfaBean);
		}
			
		
	}
	
	private IdfaBean initIdfa(Map<String, String> params) {
		IdfaBean idfa = new IdfaBean();
		idfa.setIdfa(params.get("idfa"));
		idfa.setIp(params.get("ip"));
		idfa.setCallback(params.get("callback"));
		
		return idfa;
	}
}
