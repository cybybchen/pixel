package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.RechargeBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.RechargeProto.Rmb;
import com.trans.pixel.protoc.RechargeProto.RmbList;
import com.trans.pixel.protoc.RechargeProto.VipLibao;
import com.trans.pixel.protoc.RechargeProto.VipLibaoList;
import com.trans.pixel.protoc.RechargeProto.VipReward;
import com.trans.pixel.service.cache.CacheService;

@Service
public class RechargeRedisService extends RedisService {
//	private static Logger logger = Logger.getLogger(RechargeRedisService.class);
	
	public RechargeRedisService() {
		getRmbConfig(RedisKey.RMB_KEY);
		getRmbConfig(RedisKey.RMB1_KEY);
	}
	
	public Rmb getRmb(int id) {
		Rmb.Builder builder = Rmb.newBuilder();
		String value = CacheService.hgetcache(RedisKey.PREFIX + RedisKey.CONFIG_PREFIX + RedisKey.RMB_KEY, id+"");
		if(value != null && parseJson(value, builder))
			return builder.build();
		Map<Integer, Rmb> map = getRmbConfig(RedisKey.RMB_KEY);
		return map.get(id);
	}

	public Rmb getRmb1(int id) {
		Rmb.Builder builder = Rmb.newBuilder();
		String value = CacheService.hgetcache(RedisKey.PREFIX + RedisKey.CONFIG_PREFIX + RedisKey.RMB1_KEY, id+"");
		if(value != null && parseJson(value, builder))
			return builder.build();
		Map<Integer, Rmb> map = getRmbConfig(RedisKey.RMB1_KEY);
		return map.get(id);
	}
	
	public Map<Integer, Rmb> getRmbConfig(String rmbKey) {
		Map<String, String> keyvalue = CacheService.hgetcache(RedisKey.PREFIX + RedisKey.CONFIG_PREFIX + rmbKey);
		Map<Integer, Rmb> map = new HashMap<Integer, Rmb>();
		if(!keyvalue.isEmpty()){
			for(String value : keyvalue.values()){
				Rmb.Builder builder = Rmb.newBuilder();
				if(parseJson(value, builder)){
					map.put(builder.getId(), builder.build());
				}
			}
		}else {
			RmbList.Builder builder = RmbList.newBuilder();
			String xml = ReadConfig("ld_"+rmbKey+".xml");
			parseXml(xml, builder);
			xml = ReadConfig("ld_libao.xml");
			VipLibaoList.Builder list = VipLibaoList.newBuilder();
			parseXml(xml, list);
			for(Rmb.Builder rmb : builder.getDataBuilderList()) {
				for(VipLibao libao : list.getDataList()){
					if(libao.getItemid() == rmb.getReward().getItemid()) {
						for(VipReward vipreward : libao.getRewardList()) {
							RewardInfo.Builder reward = RewardInfo.newBuilder();
							reward.setItemid(vipreward.getItemid());
							reward.setCount(vipreward.getCount());
							rmb.addLibao(reward);
						}
						break;
					}
				}
				map.put(rmb.getId(), rmb.build());
				keyvalue.put(rmb.getId()+"", RedisService.formatJson(rmb.build()));
			}
			CacheService.hputcacheAll(RedisKey.PREFIX + RedisKey.CONFIG_PREFIX + rmbKey, keyvalue);
		}
		return map;
	}
	
	public void addRechargeRecord(RechargeBean recharge) {
		String key = RedisKey.PUSH_MYSQL_KEY + RedisKey.RECHARGE_KEY;
		sadd(key, JSONObject.fromObject(recharge).toString());
	}
	
	public String popDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY + RedisKey.RECHARGE_KEY);
	}
	
	public void addUserRecharge(long userId, MultiReward rewards) {
		String key = RedisKey.USER_RECHARGE_PREFIX + userId;
		this.set(key, formatJson(rewards), userId);
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_1HOUR, userId);
	}
	
	public MultiReward getUserRecharge(long userId) {
		String key = RedisKey.USER_RECHARGE_PREFIX + userId;
		String value = get(key, userId);
		if (value == null)
			return null;
		delete(key, userId);//查询之后删除，防止又一次查询
		MultiReward.Builder builder = MultiReward.newBuilder();
		if(parseJson(value, builder))
			return builder.build();
		
		return null;
	}
}
