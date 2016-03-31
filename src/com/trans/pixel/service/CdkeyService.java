package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.CdkeyMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.Cdkey;
import com.trans.pixel.service.redis.CdkeyRedisService;

@Service
public class CdkeyService {
	@Resource
	private CdkeyRedisService redis;
	@Resource
	private CdkeyMapper mapper;
	
	public List<String> getCdkeyRewarded(UserBean user){
		String value =  redis.getCdkeyRewarded(user);
		List<String> rewarded =  new ArrayList<String>();
		if(value == null){
			value = mapper.selectById(user.getId());
		}
		if(value != null)
		for(String key : value.split(",")){
			rewarded.add(key);
		}
		return rewarded;
	}
	
	public void saveCdkeyRewarded(UserBean user, List<String> rewarded){
		String value = String.join(",", rewarded);
		mapper.update(user.getId(), value);
		redis.saveCdkeyRewarded(user, value);
	}
	
	public String getCdkey(String key){
		return redis.getCdkey(key);
	}

	public String getCdkeyOld(String key){
		return redis.getCdkeyOld(key);
	}

	public void delCdkey(String key, String value){
		redis.delCdkey(key, value);
	}
	
	public Cdkey getCdkeyConfig(String id){
		return redis.getCdkeyConfig(id);
	}
}
