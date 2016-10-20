package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.trans.pixel.model.SkillBean;
import com.trans.pixel.model.SkillLevelBean;

@Repository
public class SkillRedisService {
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public SkillBean getSkillById(final int id) {
		return redisTemplate.execute(new RedisCallback<SkillBean>() {
			@Override
			public SkillBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.SKILL_KEY);
				
				
				return SkillBean.fromJson(bhOps.get("" + id));
			}
		});
	}
	
	public void setSkillList(final List<SkillBean> skillList) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.SKILL_KEY);
				
				for (SkillBean skill : skillList) {
					bhOps.put("" + skill.getId(), skill.toJson());
				}
				
				return null;
			}
		});
	}
	
	public SkillLevelBean getSkillLevelById(final int id) {
		return redisTemplate.execute(new RedisCallback<SkillLevelBean>() {
			@Override
			public SkillLevelBean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.SKILLLEVEL_KEY);
				
				
				return SkillLevelBean.fromJson(bhOps.get("" + id));
			}
		});
	}
	
	public List<SkillLevelBean> getSkillLevelList() {
		return redisTemplate.execute(new RedisCallback<List<SkillLevelBean>>() {
			@Override
			public List<SkillLevelBean> doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.SKILLLEVEL_KEY);
				
				List<SkillLevelBean> skillLevelList = new ArrayList<SkillLevelBean>();
				Iterator<Entry<String, String>> it = bhOps.entries().entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, String> entry = it.next();
					SkillLevelBean skillLevel = SkillLevelBean.fromJson(entry.getValue());
					if (skillLevel != null)
						skillLevelList.add(skillLevel);
				}
				Collections.sort(skillLevelList, new Comparator<SkillLevelBean>() {
			        public int compare(SkillLevelBean bean1, SkillLevelBean bean2) {
		                if (bean1.getId() < bean2.getId()) {
		                        return -1;
		                } else {
		                        return 1;
		                }
			        }});
				
				return skillLevelList;
			}
		});
	}
	
	public void setSkillLevelList(final List<SkillLevelBean> skillLevelList) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundHashOperations<String, String, String> bhOps = redisTemplate
						.boundHashOps(RedisKey.PREFIX + RedisKey.SKILLLEVEL_KEY);
				
				for (SkillLevelBean skillLevel : skillLevelList) {
					bhOps.put("" + skillLevel.getId(), skillLevel.toJson());
				}
				
				return null;
			}
		});
	}
}
