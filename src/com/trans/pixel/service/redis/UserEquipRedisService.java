package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserEquipBean;

@Repository
public class UserEquipRedisService extends RedisService{
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public UserEquipBean selectUserEquip(final long userId, final int equipId) {
		return redisTemplate.execute(new RedisCallback<UserEquipBean>() {
			@Override
			public UserEquipBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_EQUIP_PREFIX + userId);
				
				
				return UserEquipBean.fromJson(bhOps.get("" + equipId));
			}
		});
	}
	
	public void updateUserEquip(final UserEquipBean userEquip) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_EQUIP_PREFIX + userEquip.getUserId());
				
				
				bhOps.put("" + userEquip.getEquipId(), userEquip.toJson());
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				
				return null;
			}
		});

		sadd(RedisKey.PUSH_MYSQL_KEY+RedisKey.USER_EQUIP_PREFIX, userEquip.getUserId()+"#"+userEquip.getEquipId());
	}
	
	public String popDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY+RedisKey.USER_EQUIP_PREFIX);
	}
	
	public void updateUserEquipList(final List<UserEquipBean> userEquipList, final long userId) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_EQUIP_PREFIX + userId);
				
				for (UserEquipBean userEquip : userEquipList) {
					bhOps.put("" + userEquip.getEquipId(), userEquip.toJson());
				}
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				
				return null;
			}
		});
	}
	
	public List<UserEquipBean> selectUserEquipList(final long userId) {
		return redisTemplate.execute(new RedisCallback<List<UserEquipBean>>() {
			@Override
			public List<UserEquipBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.USER_EQUIP_PREFIX + userId);
				
				List<UserEquipBean> userEquipList = new ArrayList<UserEquipBean>();
				Iterator<Entry<String, String>> ite = bhOps.entries().entrySet().iterator();
				while (ite.hasNext()) {
					Entry<String, String> entry = ite.next();
					UserEquipBean userEquip = UserEquipBean.fromJson(entry.getValue());
					if (userEquip != null)
						userEquipList.add(userEquip);
				}
				bhOps.expire(RedisExpiredConst.EXPIRED_USERINFO_DAYS, TimeUnit.DAYS);
				return userEquipList;
			}
		});
	}
}
