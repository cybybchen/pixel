package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RankConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.BlackListBean;
import com.trans.pixel.protoc.RewardTaskProto.Raid;
import com.trans.pixel.protoc.RewardTaskProto.RaidList;
import com.trans.pixel.service.cache.CacheService;

import net.sf.json.JSONObject;

@Repository
public class BlackListRedisService extends RedisService{
	
	public List<BlackListBean> getBlackLists() {
		List<BlackListBean> list = new ArrayList<BlackListBean>();
		Map<String, String> keyvalue = hget(RedisKey.PREFIX+RedisKey.BLACKLIST);
		for(String value : keyvalue.values()){
			JSONObject json = JSONObject.fromObject(value);
			Object object = JSONObject.toBean(json, BlackListBean.class);
			list.add((BlackListBean)object);
		}
		return list;
	}
	public Map<String, String> getBlackListMap() {
		return hget(RedisKey.PREFIX+RedisKey.BLACKLIST);
	}

	public void setBlackLists(List<BlackListBean> list) {
		for(BlackListBean bean : list)
			updateBlackList(bean);
	}

	public BlackListBean getBlackList(long userid) {
		String value = hget(RedisKey.PREFIX+RedisKey.BLACKLIST, userid+"");
		if(value == null)
			return null;
		JSONObject json = JSONObject.fromObject(value);
		Object object = JSONObject.toBean(json, BlackListBean.class);
		return (BlackListBean)object;
	}

	public void updateBlackList(BlackListBean bean) {
		hput(RedisKey.PREFIX+RedisKey.BLACKLIST, bean.getUserId()+"", JSONObject.fromObject(bean).toString());
		if(bean.isNoaccount())
			hput(RedisKey.PREFIX+RedisKey.BLACKLIST_ACCOUNT, bean.getAccount()+"", bean.getUserId()+"");
		else
			hdelete(RedisKey.PREFIX+RedisKey.BLACKLIST_ACCOUNT, bean.getAccount()+"");
		if(bean.isNoidfa() && (bean.getIdfa()+"").length() > 5) {
			hput(RedisKey.PREFIX+RedisKey.BLACKLIST_IDFA, bean.getIdfa()+"", bean.getUserId()+"");
		}else
			hdelete(RedisKey.PREFIX+RedisKey.BLACKLIST_IDFA, bean.getIdfa()+"");
	}

	public void deleteBlackList(BlackListBean bean) {
		hdelete(RedisKey.PREFIX+RedisKey.BLACKLIST, bean.getUserId()+"");
		hdelete(RedisKey.PREFIX+RedisKey.BLACKLIST_ACCOUNT, bean.getAccount()+"");
		hdelete(RedisKey.PREFIX+RedisKey.BLACKLIST_IDFA, bean.getIdfa()+"");
	}

	public void deleteRank(BlackListBean bean) {
		zremove(RedisKey.ZHANLI_RANK+bean.getServerId(), bean.getUserId()+"");
		zremove(RedisKey.ZHANLI_RANK_NODELETE+bean.getServerId(), bean.getUserId()+"");
		zremove(RedisKey.PREFIX + RedisKey.SERVER_PREFIX + bean.getServerId() + RedisKey.SPLIT + RedisKey.RANK_PREFIX + RankConst.TYPE_VIP_HUOYUE, bean.getUserId() + "");
		zremove(RedisKey.PREFIX + RedisKey.SERVER_PREFIX + bean.getServerId() + RedisKey.SPLIT + RedisKey.RANK_PREFIX + RankConst.TYPE_RECHARGE, bean.getUserId() + "");
		
		RaidList raidList = CacheService.getcache(RedisKey.RAID_CONFIG);
		for (Raid raid : raidList.getDataList()) {
			zremove(RedisKey.PREFIX + RedisKey.SERVER_PREFIX + "1" + RedisKey.SPLIT + RedisKey.RANK_PREFIX + RankConst.RAID_RANK_PREFIX + raid.getId(), bean.getUserId() + "");
		}
	}

	public boolean isNoaccount(String account){
		String value = hget(RedisKey.PREFIX+RedisKey.BLACKLIST_ACCOUNT, account);
		if(value == null)
			return false;
		else
			return true;
	}

	public boolean isNoidfa(String idfa){
		String value = hget(RedisKey.PREFIX+RedisKey.BLACKLIST_IDFA, idfa);
		if(value == null)
			return false;
		else
			return true;
	}
}
