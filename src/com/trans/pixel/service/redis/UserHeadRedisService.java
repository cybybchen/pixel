package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserHeadBean;

@Repository
public class UserHeadRedisService extends RedisService {
	
	public UserHeadBean selectUserHead(final long userId, final int headId) {
		String value = hget(buildUserHeadRedisKey(userId), "" + headId, userId);
		expire(buildUserHeadRedisKey(userId), RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
		JSONObject json = JSONObject.fromObject(value);
		return (UserHeadBean) JSONObject.toBean(json, UserHeadBean.class);
	}
	
	public void addUserHead(final UserHeadBean userHead) {
		hput(buildUserHeadRedisKey(userHead.getUserId()), "" + userHead.getHeadId(), JSONObject.fromObject(userHead).toString(), userHead.getUserId());
		expire(buildUserHeadRedisKey(userHead.getUserId()), RedisExpiredConst.EXPIRED_USERINFO_7DAY, userHead.getUserId());
	}
	
	public void setUserHeadList(final List<UserHeadBean> userHeadList, final long userId) {
		hputAll(buildUserHeadRedisKey(userId), convertListToMap(userHeadList));
		expire(buildUserHeadRedisKey(userId), RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
	}
	
	public List<UserHeadBean> selectUserHeadList(final long userId) {
		List<UserHeadBean> userHeadList = new ArrayList<UserHeadBean>();
		Map<String, String> map = hget(buildUserHeadRedisKey(userId), userId);
		Iterator<Entry<String, String>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			JSONObject json = JSONObject.fromObject(entry.getValue());
			UserHeadBean userHead = (UserHeadBean) JSONObject.toBean(json, UserHeadBean.class);
			if (userHead != null)
				userHeadList.add(userHead);
		}
		
		expire(buildUserHeadRedisKey(userId), RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
		
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
