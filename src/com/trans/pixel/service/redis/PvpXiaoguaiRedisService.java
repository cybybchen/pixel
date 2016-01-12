package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.pvp.PvpXiaoguaiBean;

@Repository
public class PvpXiaoguaiRedisService {
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public PvpXiaoguaiBean getPvpXiaoguai(final int id) {
		return redisTemplate.execute(new RedisCallback<PvpXiaoguaiBean>() {
			@Override
			public PvpXiaoguaiBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.PVP_XIAOGUAI_REFIX);
				
				
				return PvpXiaoguaiBean.fromJson(bhOps.get("" + id));
			}
		});
	}
	
	public void setPvpXiaoguaiList(final List<PvpXiaoguaiBean> pvpXiaoguaiList) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.PVP_XIAOGUAI_REFIX);
				
				for (PvpXiaoguaiBean xiaoguai : pvpXiaoguaiList) {
					bhOps.put("" + xiaoguai.getId(), xiaoguai.toJson());
				}
				
				return null;
			}
		});
	}
	
	public List<PvpXiaoguaiBean> getPvpXiaoguaiList() {
		return redisTemplate.execute(new RedisCallback<List<PvpXiaoguaiBean>>() {
			@Override
			public List<PvpXiaoguaiBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.PVP_XIAOGUAI_REFIX);
				
				List<PvpXiaoguaiBean> list = new ArrayList<PvpXiaoguaiBean>();
				Iterator<Entry<String, String>> ite = bhOps.entries().entrySet().iterator();
				while (ite.hasNext()) {
					Entry<String, String> entry = ite.next();
					PvpXiaoguaiBean xiaoguai = PvpXiaoguaiBean.fromJson(entry.getValue());
					if (xiaoguai != null)
						list.add(xiaoguai);
				}
				
				return list;
			}
		});
	}
}
