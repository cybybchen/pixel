package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.SkillLevelBean;
import com.trans.pixel.service.cache.CacheService;

@Repository
public class SkillRedisService extends CacheService {
	
	public SkillRedisService() {
		getSkillLevelList();
	}
	
	public SkillLevelBean getSkillLevelById(final int id) {
		String value = hget(RedisKey.PREFIX + RedisKey.SKILLLEVEL_KEY, "" + id);
		
		return SkillLevelBean.fromJson(value);
	}
	
	public List<SkillLevelBean> getSkillLevelList() {
		List<SkillLevelBean> skillLevelList = new ArrayList<SkillLevelBean>();
		Map<String, String> map = hget(RedisKey.PREFIX + RedisKey.SKILLLEVEL_KEY);
		if (map == null || map.isEmpty()) {
			skillLevelList = SkillLevelBean.xmlParse();
			if (skillLevelList != null && skillLevelList.size() != 0) {
				setSkillLevelList(skillLevelList);
				
				return skillLevelList;
			}
		}
		Iterator<Entry<String, String>> it = map.entrySet().iterator();
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
	
	public void setSkillLevelList(final List<SkillLevelBean> skillLevelList) {
		for (SkillLevelBean skillLevel : skillLevelList) {
			hput(RedisKey.PREFIX + RedisKey.SKILLLEVEL_KEY, "" + skillLevel.getId(), skillLevel.toJson());
		}
	}
}
