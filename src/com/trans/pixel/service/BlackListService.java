package com.trans.pixel.service;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ActivityConst;
import com.trans.pixel.model.BlackListBean;
import com.trans.pixel.model.mapper.BlackListMapper;
import com.trans.pixel.service.redis.BlackListRedisService;

import net.sf.json.JSONObject;

@Service
public class BlackListService {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(BlackListService.class);
	
	@Resource
	private BlackListRedisService redis;
	@Resource
	private BlackListMapper mapper;
	@Resource
	private ActivityService activityService;
	
	public List<BlackListBean> getBlackLists() {
		List<BlackListBean> blackLists = redis.getBlackLists();
		if (blackLists.isEmpty()) {
			blackLists = mapper.selectBlackLists();
			if (blackLists != null && blackLists.size() > 0)
				redis.setBlackLists(blackLists);
		}
		
		return blackLists;
	}

	public Map<String, String> getBlackListMap() {
		Map<String, String> keyvalue = redis.getBlackListMap();
		if (keyvalue.isEmpty()) {
			List<BlackListBean> blackLists = mapper.selectBlackLists();
			if (blackLists != null && blackLists.size() > 0)
				redis.setBlackLists(blackLists);
		}
		keyvalue = redis.getBlackListMap();
		
		return keyvalue;
	}
	
	public BlackListBean getBlackList(long userId) {
		Map<String, String> keyvalue = getBlackListMap();
		String value = keyvalue.get(userId+"");
		if(value == null)
			return null;
		JSONObject json = JSONObject.fromObject(value);
		Object object = JSONObject.toBean(json, BlackListBean.class);
		return (BlackListBean)object;
	}

	public void updateBlackList(BlackListBean bean) {
		redis.updateBlackList(bean);
		mapper.updateBlackList(bean);
		if(bean.isNoranklist()){
			redis.deleteRank(bean);
			activityService.deleteKaifu2Score(bean.getUserId(), bean.getServerId(), ActivityConst.KAIFU2_ZHANLI);
		}
	}

	public void deleteBlackList(long userid) {
		BlackListBean bean = redis.getBlackList(userid);
		if(bean == null)
			return;
		redis.deleteBlackList(bean);
		mapper.deleteBlackList(bean.getUserId());
	}
	
	public boolean isNotalk(long userid){
		BlackListBean bean = redis.getBlackList(userid);
		if(bean == null)
			return false;
		return bean.isNotalk();
	}

	public boolean isNologin(long userid){
		BlackListBean bean = redis.getBlackList(userid);
		if(bean == null)
			return false;
		return bean.isNologin();
	}

	public boolean isNoranklist(long userid){
		BlackListBean bean = redis.getBlackList(userid);
		if(bean == null)
			return false;
		return bean.isNoranklist();
	}

	public boolean isNodiscuss(long userid){
		BlackListBean bean = redis.getBlackList(userid);
		if(bean == null)
			return false;
		return bean.isNodiscuss();
	}
	
	public boolean isNoaccount(String account){
		return redis.isNoaccount(account);
	}

	public boolean isNoDevice(String idfa){
		return redis.isNoidfa(idfa);
	}
}
