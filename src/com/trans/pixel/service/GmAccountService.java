package com.trans.pixel.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.GmAccountBean;
import com.trans.pixel.model.mapper.GmAccountMapper;
import com.trans.pixel.service.redis.GmAccountRedisService;

@Service
public class GmAccountService {
	@Resource
	private GmAccountRedisService redis;
	@Resource
	private GmAccountMapper mapper;
	
	private GmAccountBean buildGmAccount(String account){
		GmAccountBean bean = new GmAccountBean();
		bean.setAccount(account);
		if(account.equals("ybchen") || account.equals("xjwang")){
			bean.setMaster(1);
		}
		return bean;
	}
	
	public GmAccountBean getAccount(String account){
		GmAccountBean bean = mapper.queryGmAccount(account);
		if(bean == null){
			bean = buildGmAccount(account);
		}
		return bean;
	}
	
	public void updateGmAccount(GmAccountBean account){
		mapper.updateGmAccount(account);
	}
	
	public String getSession(String session){
		return redis.getSession(session);
	}
	
	public Map<String, GmAccountBean> getGmAccounts() {
		Map<String, String> map = redis.getGmAccounts();
		Map<String, GmAccountBean> accountMap = new HashMap<String, GmAccountBean>();
		for(Entry<String, String> entry : map.entrySet()){
			GmAccountBean bean = getAccount(entry.getKey());
			if(bean == null){
				bean = buildGmAccount(entry.getKey());
			}
			accountMap.put(entry.getKey(), bean);
		}
		return accountMap;
	}
}
