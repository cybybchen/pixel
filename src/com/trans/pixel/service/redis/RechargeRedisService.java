package com.trans.pixel.service.redis;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.RechargeBean;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.Rmb;
import com.trans.pixel.protoc.Commands.RmbList;

@Service
public class RechargeRedisService extends RedisService {
//	private static Logger logger = Logger.getLogger(RechargeRedisService.class);
	
	public Rmb getRmb(int id) {
		RmbList list = getRmbConfig(RedisKey.RMB_KEY);
		for(Rmb rmb : list.getRmbList()){
			if(rmb.getId() == id)
				return rmb;
		}
		return null;
	}

	public Rmb getRmb1(int id) {
		RmbList list = getRmbConfig(RedisKey.RMB1_KEY);
		for(Rmb rmb : list.getRmbList()){
			if(rmb.getId() == id)
				return rmb;
		}
		return null;
	}
	
	public RmbList getRmbConfig(String rmbKey) {
		String value = get(RedisKey.PREFIX + RedisKey.CONFIG_PREFIX + rmbKey);
		RmbList.Builder builder = RmbList.newBuilder();
		if(value != null && parseJson(value, builder))
			return builder.build();
		else{
			String xml = ReadConfig("lol_"+rmbKey+".xml");
			parseXml(xml, builder);
			for(Rmb.Builder rmb : builder.getRmbBuilderList()){
//				rmb.clearName();
				rmb.clearImg();
				rmb.clearDescription();
			}
			RmbList list = builder.build();
			set(RedisKey.PREFIX + RedisKey.CONFIG_PREFIX + rmbKey, formatJson(list));
			return list;
		}
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
		this.set(key, formatJson(rewards));
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_1HOUR);
	}
	
	public MultiReward getUserRecharge(long userId) {
		String key = RedisKey.USER_RECHARGE_PREFIX + userId;
		String value = get(key);
		if (value == null)
			return null;
		this.delete(key);//查询之后删除，防止又一次查询
		MultiReward.Builder builder = MultiReward.newBuilder();
		if(parseJson(value, builder))
			return builder.build();
		
		return null;
	}
}
