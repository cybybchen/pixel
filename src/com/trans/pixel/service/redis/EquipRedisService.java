package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.EquipmentBean;
import com.trans.pixel.model.RewardBean;

@Service
public class EquipRedisService {
	@Resource
	public RedisTemplate<String, String> redisTemplate;
	
	public List<EquipmentBean> getEquipList() {
		return redisTemplate.execute(new RedisCallback<List<EquipmentBean>>() {
			@Override
			public List<EquipmentBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, Integer, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.EQUIP_KEY);
				
				List<EquipmentBean> list = new ArrayList<EquipmentBean>();
				Set<Entry<Integer, String>> set = bhOps.entries().entrySet();
				Iterator<Entry<Integer, String>> itr = set.iterator();
				while(itr.hasNext()) {
					Entry<Integer, String> entry = itr.next();
					EquipmentBean bean = EquipmentBean.fromJson(entry.getValue());
					if (bean != null) {
						list.add(bean);
					}
				}
				
				return list;
			}
		
		});
	}
	
	public void setEquipList(final List<EquipmentBean> list) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, Integer, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.EQUIP_KEY);
				
				for (EquipmentBean bean : list) {
					bhOps.put(bean.getItemid(), bean.toJson());
				}
				
				return null;
			}
		
		});
	}
	
	public EquipmentBean getEquip(final int id) {
		return redisTemplate.execute(new RedisCallback<EquipmentBean>() {
			@Override
			public EquipmentBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, Integer, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.EQUIP_KEY);
				
				EquipmentBean bean = EquipmentBean.fromJson(bhOps.get(id));
				return bean;
			}
		
		});
	}
	
}
