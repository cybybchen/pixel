package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.RechargeBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.RechargeProto.Rmb;
import com.trans.pixel.protoc.RechargeProto.RmbList;
import com.trans.pixel.protoc.RechargeProto.RmbOrder;
import com.trans.pixel.protoc.RechargeProto.VipLibao;
import com.trans.pixel.protoc.RechargeProto.VipLibaoList;
import com.trans.pixel.protoc.RechargeProto.VipReward;
import com.trans.pixel.protoc.ShopProto.Libao;
import com.trans.pixel.service.cache.CacheService;

import net.sf.json.JSONObject;

@Service
public class RechargeRedisService extends RedisService {
//	private static Logger logger = Logger.getLogger(RechargeRedisService.class);
	
	public RechargeRedisService() {
		buildRmbConfig(RedisKey.RMB_KEY);
		buildRmbConfig(RedisKey.RMB1_KEY);
	}

	public void clearCanRecharge(long userId){
		String key = RedisKey.USER_LIBAOCANRECHARGE_PREFIX+userId;
		delete(key, userId);
	}
	
	public void saveCanRecharge(long userId, Libao libao){
		String key = RedisKey.USER_LIBAOCANRECHARGE_PREFIX+userId;
		set(key, RedisService.formatJson(libao), userId);
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
	}

	public Libao getCanRecharge(long userId){
		String key = RedisKey.USER_LIBAOCANRECHARGE_PREFIX+userId;
		String value = get(key, userId);
		Libao.Builder libao = Libao.newBuilder();
		if(value != null && RedisService.parseJson(value, libao))
			return libao.build();
		return null;
	}
	
	public Rmb getRmb(int id) {
		Map<Integer, Rmb> map = getRmbConfig(RedisKey.RMB_KEY);
		return map.get(id);
	}

	public Rmb getRmb1(int id) {
		Map<Integer, Rmb> map = getRmbConfig(RedisKey.RMB1_KEY);
		return map.get(id);
	}
	
	public Map<Integer, Rmb> getRmbConfig(String rmbKey) {
		Map<Integer, Rmb> map = CacheService.hgetcache(RedisKey.PREFIX + RedisKey.CONFIG_PREFIX + rmbKey);
		return map;
	}
	
	private void buildRmbConfig(String rmbKey) {
		String xml = ReadConfig("ld_"+rmbKey+".xml");
		Map<Integer, Rmb> map = new HashMap<Integer, Rmb>();
		RmbList.Builder builder = RmbList.newBuilder();
		parseXml(xml, builder);
		xml = ReadConfig("ld_libao.xml");
		VipLibaoList.Builder list = VipLibaoList.newBuilder();
		parseXml(xml, list);
		for(Rmb.Builder rmb : builder.getDataBuilderList()) {
			for(RmbOrder.Builder rmborder : rmb.getOrderBuilderList()) {
				if(rmborder.getStarttime().isEmpty())
					rmborder.clearStarttime();
				if(rmborder.getEndtime().isEmpty())
						rmborder.clearEndtime();
				for(VipLibao libao : list.getDataList()){
					if(libao.getItemid() == rmborder.getReward().getItemid()) {
						for(VipReward vipreward : libao.getRewardList()) {
							RewardInfo.Builder reward = RewardInfo.newBuilder();
							reward.setItemid(vipreward.getItemid());
							reward.setCount(vipreward.getCount());
							rmborder.addLibao(reward);
						}
						break;
					}
				}
			}
			map.put(rmb.getId(), rmb.build());
		}
		CacheService.hputcacheAll(RedisKey.PREFIX + RedisKey.CONFIG_PREFIX + rmbKey, map);
	}
	
	public void addRechargeRecord(RechargeBean recharge) {
		String key = RedisKey.PUSH_MYSQL_KEY + RedisKey.RECHARGE_KEY;
		sadd(key, JSONObject.fromObject(recharge).toString());
	}
	
	public String popDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY + RedisKey.RECHARGE_KEY);
	}
	
//	public void addUserRecharge(long userId, MultiReward rewards) {
//		String key = RedisKey.USER_RECHARGE_PREFIX + userId;
//		this.set(key, formatJson(rewards));
//		expire(key, RedisExpiredConst.EXPIRED_USERINFO_1HOUR);
//	}
	
//	public MultiReward getUserRecharge(long userId) {
//		String key = RedisKey.USER_RECHARGE_PREFIX + userId;
//		String value = get(key);
//		if (value == null)
//			return null;
//		this.delete(key);//查询之后删除，防止又一次查询
//		MultiReward.Builder builder = MultiReward.newBuilder();
//		if(parseJson(value, builder))
//			return builder.build();
//		
//		return null;
//	}
	
	public void addUserRecharge(long userId, RechargeBean recharge) {
		String key = RedisKey.USER_RECHARGE_PREFIX + userId;
		hput(key, recharge.getOrderId(), RedisService.toJson(recharge), userId);
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
	}
	
	public List<RechargeBean> getUserRecharge(long userId) {
		String key = RedisKey.USER_RECHARGE_PREFIX + userId;
		Map<String, String> map = hget(key, userId);
		if (map == null || map.isEmpty())
			return new ArrayList<RechargeBean>();
		this.delete(key, userId);//查询之后删除，防止又一次查询
		List<RechargeBean> rechargeList = new ArrayList<RechargeBean>();
		for (String value : map.values()) {
			Object object = RedisService.fromJson(value, RechargeBean.class);
			if (object != null)
				rechargeList.add((RechargeBean)object);
		}
		
		return rechargeList;
	}
	
	public void addUserLoginReward(long userId, MultiReward rewards) {
		String key = RedisKey.USER_LOGINREWARD_PREFIX + userId;
		lpush(key, RedisService.formatJson(rewards), userId);
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
	}
	
	public MultiReward getUserLoginReward(long userId) {
		String key = RedisKey.USER_LOGINREWARD_PREFIX + userId;
		List<String> values = lrange(key, userId);
		if (values == null || values.isEmpty())
			return null;
		this.delete(key, userId);//查询之后删除，防止又一次查询
		MultiReward.Builder rewards = MultiReward.newBuilder();
		for (String value : values) {
			MultiReward.Builder builder = MultiReward.newBuilder();
			if (RedisService.parseJson(value, builder))
				rewards.addAllLoot(builder.getLootList());
		}
		
		return rewards.build();
	}
}
