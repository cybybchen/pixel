package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

	public String getCdkeyRewardedStr(UserBean user){
		String value =  redis.getCdkeyRewarded(user);
		if(value == null)
			value = mapper.selectById(user.getId());
		if(value == null)
			value = "";
		return value;
	}
	
	public void saveCdkeyRewarded(UserBean user, List<String> rewarded){
		String value = String.join(",", rewarded);
		mapper.update(user.getId(), value);
		redis.saveCdkeyRewarded(user, value);
	}
	
	/*on master server*/
	public void updateToDB(long userId) {
		String value = redis.getCdkeyRewarded(userId);
		if(value != null)
			mapper.update(userId, value);
	}
	
	public String popDBKey(){
		return redis.popDBKey();
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
	
	public void delCdkeyConfig(String id){
		redis.delCdkeyConfig(id);
	}
	
	List<String> getAvaiCdkeys(String id){
		return redis.getAvaiCdkeys(id);
	}
	
	List<String> addCdkeyConfig(Cdkey.Builder cdkey, int length){
		return redis.addCdkeyConfig(cdkey, length);
	}
	
	Map<Integer, String> getCdkeyConfigs(){
		return redis.getCdkeyConfigs();
	}
}
