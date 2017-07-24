package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.SkillLevelBean;
import com.trans.pixel.service.cache.CacheService;

@Repository
public class SkillRedisService extends CacheService {
	
	public SkillRedisService() {
		buildSkillLevelConfig();
	}
	
	public SkillLevelBean getSkillLevelById(final int id) {
		Map<Integer, SkillLevelBean> map = hgetcache(RedisKey.PREFIX + RedisKey.SKILLLEVEL_KEY);
		return map.get(id);
	}
	
	public List<SkillLevelBean> getSkillLevelList() {
		Map<Integer, SkillLevelBean> map = hgetcache(RedisKey.PREFIX + RedisKey.SKILLLEVEL_KEY);
		List<SkillLevelBean> skillLevelList = new ArrayList<SkillLevelBean>();
		Iterator<Entry<Integer, SkillLevelBean>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, SkillLevelBean> entry = it.next();
			SkillLevelBean skillLevel = entry.getValue();
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
	
	public void buildSkillLevelConfig() {
		List<SkillLevelBean> skillLevelList = SkillLevelBean.xmlParse();
		Map<Integer, SkillLevelBean> map = new HashMap<Integer, SkillLevelBean>();
		for (SkillLevelBean skillLevel : skillLevelList) {
			map.put(skillLevel.getId(), skillLevel);
		}
		hputcacheAll(RedisKey.PREFIX + RedisKey.SKILLLEVEL_KEY, map);
	}
}
