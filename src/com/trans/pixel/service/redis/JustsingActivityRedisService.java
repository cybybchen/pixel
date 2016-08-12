package com.trans.pixel.service.redis;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;

@Repository
public class JustsingActivityRedisService extends RedisService{
	
	public void addJustsingCdkRecord(String idfa, int type) {
		this.sadd(RedisKey.JUSTSING_CDK_RECORD_PREFIX + type, idfa);
	}
	
	public boolean hasinJustsingCdkRecord(String idfa, int type) {
		return this.sismember(RedisKey.JUSTSING_CDK_RECORD_PREFIX + type, idfa);
	}
	
	public String getCdk(int type) {
		return this.spop(RedisKey.JUSTSING_CDK_PREFIX + type);
	}
	
	public void setCdkList(int type, List<String> cdkList) {
		for (String cdk : cdkList) {
			this.sadd(RedisKey.JUSTSING_CDK_PREFIX + type, cdk);
		}
	}
	
	public String getJustsingActivityAvailableTime() {
		return this.get(RedisKey.JUSTSING_ACTIVITY_AVAILABLE_TIME_KEY);
	}
}
