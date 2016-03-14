package com.trans.pixel.service.redis;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.Commands.UserKaifu;
import com.trans.pixel.protoc.Commands.UserRichang;
import com.trans.pixel.utils.DateUtil;

@Service
public class UserActivityRedisService extends RedisService {

	public void updateUserRichang(long userId, UserRichang ur, String endTime) {
		String key = buildRichangRedisKey(ur.getType(), userId);
		this.set(key, formatJson(ur));
		this.expireAt(key, DateUtil.getDate(endTime));
	}
	
	public UserRichang getUserRichang(long userId, int type) {
		String key = buildRichangRedisKey(type, userId);
		String value = get(key);
		UserRichang.Builder builder = UserRichang.newBuilder();
		if(value!= null && parseJson(value, builder))
			return builder.build();
		else
			return null;
	}
	
	private String buildRichangRedisKey(int type, long userId) {
		return RedisKey.USER_ACTIVITY_RICHANG_PREFIX + type + RedisKey.SPLIT + RedisKey.USER_PREFIX + userId;
	}
	
	//kaifu activity
	public void updateUserKaifu(long userId, UserKaifu uk) {
		String key = buildKaifuRedisKey(uk.getType(), userId);
		this.set(key, formatJson(uk));
		this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_30DAY);
	}
	
	public UserKaifu getUserKaifu(long userId, int type) {
		String key = buildKaifuRedisKey(type, userId);
		String value = get(key);
		UserKaifu.Builder builder = UserKaifu.newBuilder();
		if (value != null && parseJson(value, builder))
			return builder.build();
		else 
			return null;
	}
	
	private String buildKaifuRedisKey(int type, long userId) {
		return RedisKey.USER_ACTIVITY_KAIFU_PREFIX + type + RedisKey.SPLIT + RedisKey.USER_PREFIX + userId;
	}
}
