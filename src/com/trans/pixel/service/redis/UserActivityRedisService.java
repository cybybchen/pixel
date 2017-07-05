package com.trans.pixel.service.redis;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.ActivityProto.Activity;
import com.trans.pixel.protoc.ActivityProto.UserKaifu;
import com.trans.pixel.protoc.ActivityProto.UserRichang;
import com.trans.pixel.utils.DateUtil;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class UserActivityRedisService extends RedisService {

	public void updateUserRichang(long userId, UserRichang ur, int cycle, String endTime) {
		String key = buildRichangRedisKey(ur.getType(), userId);
		this.set(key, formatJson(ur));
		if (cycle == 1)
			this.expireAt(key, DateUtil.setToDayEndTime(DateUtil.getDate()));
		else
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
	public void updateUserKaifu(long userId, UserKaifu uk, int cycle, int lastTime) {
		String key = buildKaifuRedisKey(uk.getType(), userId);
		this.set(key, formatJson(uk));
		if (cycle == 1)
			this.expireAt(key, DateUtil.setToDayEndTime(DateUtil.getDate()));
		else
			this.expire(key, RedisExpiredConst.EXPIRED_USERINFO_30DAY);
		
		if (lastTime == -1 && cycle == 0)
			sadd(RedisKey.PUSH_MYSQL_KEY+RedisKey.USER_ACTIVITY_KAIFU_PREFIX, userId+"#"+uk.getType());
	}
	
	public String popDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY+RedisKey.USER_ACTIVITY_KAIFU_PREFIX);
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
	
	/**
	 * activity type
	 */
	public boolean isGetActivityReward(final long userId, final int type, final int id) {
		String key = RedisKey.ACTIVITY_REWARD_STATUS_PREFIX + type + RedisKey.SPLIT + id;
		if (TypeTranslatedUtil.stringToInt(hget(key, "" + userId)) == 1)
			return true;
		else
			return false;
	}
	
	public void setUserGetRewardState(final long userId, final Activity activity) {
		String key = RedisKey.ACTIVITY_REWARD_STATUS_PREFIX + activity.getType() + RedisKey.SPLIT + activity.getId();
		this.hput(key, "" + userId, "1");
		int activityType = activity.getActivitytype();
		
		if (activityType == 1)
			this.expireAt(key, DateUtil.setToDayEndTime(DateUtil.getDate()));
		else if (activityType == 0)
			this.expireAt(key, DateUtil.getDate(activity.getEndtime()));
				
	}
	
	public void setActivityCompleteExpire(final int type, final String time) {
		String key = RedisKey.ACTIVITY_COMPLETE_COUNT_PREFIX + type;
		expireAt(key, DateUtil.getDate(time));
	}
	
	public void resetActivityComplete(final int type) {
		String key = RedisKey.ACTIVITY_COMPLETE_COUNT_PREFIX + type;
		delete(key);
	}
	
	public int addActivityCount(final long userId, final int add, final int type) {
		String key = RedisKey.ACTIVITY_COMPLETE_COUNT_PREFIX + type;
		int count = TypeTranslatedUtil.stringToInt(hget(key, "" + userId));
		count += add;
		hput(key, "" + userId, "" + count);
		return count;
	}
}
