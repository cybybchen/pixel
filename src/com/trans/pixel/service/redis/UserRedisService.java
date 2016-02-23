package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.UserDailyData;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.protoc.Commands.VipInfo;
import com.trans.pixel.protoc.Commands.VipList;

@Repository
public class UserRedisService extends RedisService{
	Logger logger = LoggerFactory.getLogger(UserRedisService.class);
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	public final static String USERDAILYDATA = RedisKey.PREFIX+RedisKey.USERDAILYDATA_PREFIX;
	public final static String VIP = RedisKey.PREFIX+"Vip";
	
	public UserBean getUserByUserId(final long userId) {
		return redisTemplate.execute(new RedisCallback<UserBean>() {
			@Override
			public UserBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_PREFIX + userId);
				
				return UserBean.convertUserMapToUserBean(bhOps.entries());
			}
		});
	}

	public UserDailyData.Builder getUserDailyData(UserBean user) {
		UserDailyData.Builder builder = UserDailyData.newBuilder();
		String value = hget(USERDAILYDATA+user.getId(), "UserDailyData");
		if(value != null && parseJson(value, builder))
			return builder;
		else{//每日首次登陆
			builder.setPurchaseCoinLeft(1);
			VipInfo vip = getVip(user.getVip());
			if(vip != null){
				builder.setPurchaseCoinLeft(builder.getPurchaseCoinLeft() + vip.getDianjin());
			}
			saveUserDailyData(user.getId(), builder);
		}
		return builder;
	}

	public void saveUserDailyData(final long userId, UserDailyData.Builder builder) {
		if(builder == null)
			return;
		hput(USERDAILYDATA+userId, "UserDailyData", formatJson(builder.build()));
	}
	
	public void updateUser(final UserBean user) {
		saveUserDailyData(user.getId(), user.getUserDailyData());
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_PREFIX + user.getId());
				
				if(user.getId() == 0)
					logger.error("empty user date");
				logger.debug("user is:" + user.toMap());
				bhOps.putAll(user.toMap());
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				
				return null;
			}
		});
	}

	public String getUserId(final int serverId, final String account) {
		return hget(RedisKey.PREFIX+"Account_"+serverId, account);
	}
	
	public <T> void setUserId(final int serverId, final String account, final T userId) {
		hput(RedisKey.PREFIX+"Account_"+serverId, account, userId+"");
	}
	
	public void cache(UserInfo user){
		hput(RedisKey.PREFIX+"UserCache", user.getId()+"", formatJson(user));
	}
	
	/**
	 * get other user(can be null)
	 */
	public <T> UserInfo getCache(T userId){
		String value = hget(RedisKey.PREFIX+"UserCache", userId+"");
		UserInfo.Builder builder = UserInfo.newBuilder();
		if(value != null && parseJson(value, builder))
			return builder.build();
		else
			return null;
	}
	
	/**
	 * get other user
	 */
	public <T> List<UserInfo> getCaches(List<T> userIds){
		List<String> keys = new ArrayList<String>();
		for(T userId : userIds){
			keys.add(userId+"");
		}
		List<String> values  = hget(RedisKey.PREFIX+"UserCache", keys);
		List<UserInfo> users = new ArrayList<UserInfo>();
		for(String value : values){
			UserInfo.Builder builder = UserInfo.newBuilder();
			if(value != null && parseJson(value, builder))
				users.add(builder.build());
		}
		return users;
	}

	public VipInfo getVip(int id){
		if(id <= 0)
			return null;
		VipInfo.Builder builder = VipInfo.newBuilder();
		String value = hget(VIP, id+"");
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			return buildVip(id);
		}
	}
	
	public VipInfo buildVip(int id){
		String xml = ReadConfig("lol_vip.xml");
		VipList.Builder builder = VipList.newBuilder();
		parseXml(xml, builder);
		Map<String, String> keyvalue = new HashMap<String, String>();
		for(VipInfo vip : builder.getVipList()){
			keyvalue.put(vip.getVip()+"", formatJson(vip));
		}
		hputAll(VIP, keyvalue);
		for(VipInfo vip : builder.getVipList()){
			if(id == vip.getVip())
				return vip;
		}
		return null;
	}
}
