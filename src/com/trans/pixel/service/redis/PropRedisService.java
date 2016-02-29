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
import com.trans.pixel.model.PackageBean;

@Service
public class PropRedisService {
	@Resource
	public RedisTemplate<String, String> redisTemplate;
	
	public List<PackageBean> getPropList() {
		return redisTemplate.execute(new RedisCallback<List<PackageBean>>() {
			@Override
			public List<PackageBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.PROP_KEY);
				
				List<PackageBean> list = new ArrayList<PackageBean>();
				Set<Entry<String, String>> set = bhOps.entries().entrySet();
				Iterator<Entry<String, String>> itr = set.iterator();
				while(itr.hasNext()) {
					Entry<String, String> entry = itr.next();
					PackageBean bean = PackageBean.fromJson(entry.getValue());
					if (bean != null) {
						list.add(bean);
					}
				}
				
				return list;
			}
		
		});
	}
	
	public void setPropList(final List<PackageBean> list) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.PROP_KEY);
				
				for (PackageBean bean : list) {
					bhOps.put("" + bean.getItemid(), bean.toJson());
				}
				
				return null;
			}
		
		});
	}
	
	public PackageBean getProp(final int id) {
		return redisTemplate.execute(new RedisCallback<PackageBean>() {
			@Override
			public PackageBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.PROP_KEY);
				
				PackageBean bean = PackageBean.fromJson(bhOps.get("" + id));
				return bean;
			}
		
		});
	}
	
}
