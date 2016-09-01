package com.trans.pixel.service;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.IdfaMapper;

@Service
public class IdfaService {

	@Resource
	private IdfaMapper idfaMapper;
	public void addIdfa(Map<String, String> params) {
		idfaMapper.addIdfa(params.get("idfa"), params.get("ip"), params.get("callback"));
	}
	
	public int queryIdfa(String idfa) {
		Object ret = idfaMapper.queryIdfa(idfa);
		if (ret == null)
			return 0;
		else 
			return 1;
	}
}
