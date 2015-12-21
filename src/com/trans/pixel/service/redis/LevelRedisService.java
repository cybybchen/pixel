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
import com.trans.pixel.model.DaguanBean;
import com.trans.pixel.model.XiaoguanBean;

@Repository
public class LevelRedisService {
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public XiaoguanBean getXiaoguanByLevelId(final int levelId) {
		return redisTemplate.execute(new RedisCallback<XiaoguanBean>() {
			@Override
			public XiaoguanBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.LEVEL_KEY);
				
				return XiaoguanBean.fromJson(bhOps.get("" + levelId));
			}
		});
	}
	
	public void setXiaoguanList(final List<XiaoguanBean> xiaoguanList) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.LEVEL_KEY);
				
				for (XiaoguanBean xiaoguan : xiaoguanList) {
					bhOps.put("" + xiaoguan.getId(), xiaoguan.toJson());
				}
				
				return null;
			}
		});
	}
	
	public void setXiaoguanListByDiff(final List<XiaoguanBean> xiaoguanList, final int diff) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.LEVEL_DIFF_PREDIX + diff);
				
				for (XiaoguanBean xiaoguan : xiaoguanList) {
					bhOps.put("" + xiaoguan.getId(), xiaoguan.toJson());
				}
				
				return null;
			}
		});
	}
	
	public List<XiaoguanBean> getXiaoguanListByDiff(final int diff) {
		return redisTemplate.execute(new RedisCallback<List<XiaoguanBean>>() {
			@Override
			public List<XiaoguanBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.LEVEL_DIFF_PREDIX + diff);
				
				List<XiaoguanBean> xgList = new ArrayList<XiaoguanBean>();
				Iterator<Entry<String, String>> itr = bhOps.entries().entrySet().iterator();
				while (itr.hasNext()) {
					Entry<String, String> entry = itr.next();
					XiaoguanBean xg = XiaoguanBean.fromJson(entry.getValue());
					xgList.add(xg);
				}
				return xgList;
			}
		});
	}
	
	public DaguanBean getDaguanById(final int id) {
		return redisTemplate.execute(new RedisCallback<DaguanBean>() {
			@Override
			public DaguanBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.DAGUAN_KEY);
				
				return DaguanBean.fromJson(bhOps.get("" + id));
			}
		});
	}
	
	public void setDaguanList(final List<DaguanBean> daguanList) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.DAGUAN_KEY);
				
				for (DaguanBean daguan : daguanList) {
					bhOps.put("" + daguan.getId(), daguan.toJson());
				}
				
				return null;
			}
		});
	}
	
	public List<DaguanBean> getDaguanList() {
		return redisTemplate.execute(new RedisCallback<List<DaguanBean>>() {
			@Override
			public List<DaguanBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.DAGUAN_KEY);
				
				List<DaguanBean> dgList = new ArrayList<DaguanBean>();
				Iterator<Entry<String, String>> itr = bhOps.entries().entrySet().iterator();
				while (itr.hasNext()) {
					Entry<String, String> entry = itr.next();
					DaguanBean dg = DaguanBean.fromJson(entry.getValue());
					dgList.add(dg);
				}
				return dgList;
			}
		});
	}
}
