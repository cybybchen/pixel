package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserHeadBean;

@Repository
public class UserHeadRedisService extends RedisService {
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	public UserHeadBean selectUserHead(final long userId, final int headId) {
		String value = this.hget(buildUserHeadRedisKey(userId), "" + headId);
		JSONObject json = JSONObject.fromObject(value);
		return (UserHeadBean) JSONObject.toBean(json, UserHeadBean.class);
	}
	
	public void addUserHead(final UserHeadBean userHead) {
		this.hput(buildUserHeadRedisKey(userHead.getUserId()), "" + userHead.getHeadId(), JSONObject.fromObject(userHead).toString());
	}
	
	public void setUserHeadList(final List<UserHeadBean> userHeadList, final long userId) {
		this.hputAll(buildUserHeadRedisKey(userId), convertListToMap(userHeadList));
	}
	
	public List<UserHeadBean> selectUserHeadList(final long userId) {
		List<UserHeadBean> userHeadList = new ArrayList<UserHeadBean>();
		Map<String, String> map = this.hget(buildUserHeadRedisKey(userId));
		Iterator<Entry<String, String>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			JSONObject json = JSONObject.fromObject(entry.getValue());
			UserHeadBean userHead = (UserHeadBean) JSONObject.toBean(json, UserHeadBean.class);
			if (userHead != null)
				userHeadList.add(userHead);
		}
		
		return userHeadList;
	}
	
	private String buildUserHeadRedisKey(long userId) {
		return RedisKey.USER_HEAD_PREFIX + userId;
	}
	
	private Map<String, String> convertListToMap(List<UserHeadBean> userHeadList) {
		Map<String, String> map = new HashMap<String, String>();
		for (UserHeadBean userHead : userHeadList) {
			map.put("" + userHead.getHeadId(), JSONObject.fromObject(userHead).toString());
		}
		
		return map;
	}
}
