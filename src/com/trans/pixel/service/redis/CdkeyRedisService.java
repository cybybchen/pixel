package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.Cdkey;
import com.trans.pixel.protoc.Commands.CdkeyList;
import com.trans.pixel.protoc.Commands.RewardInfo;

@Repository
public class CdkeyRedisService extends RedisService{
	
	public String getCdkeyRewarded(UserBean user){
		return hget(RedisKey.USERDATA, "Cdkey");
	}
	
	public void saveCdkeyRewarded(UserBean user, String value){
		hput(RedisKey.USERDATA+user.getId(), "Cdkey", value);
	}
	
	public String getCdkey(String key){
		return hget(RedisKey.CDKEY, key);
	}

	public String getCdkeyOld(String key){
		return hget(RedisKey.CDKEY_OLD, key);
	}

	public void delCdkey(String key, String value){
		countCdkey(key);
		hdelete(RedisKey.CDKEY, key);
		hput(RedisKey.CDKEY_OLD, key, value);
	}
	
	public int countCdkey(String key){
		String value = hget(RedisKey.CDKEY_EXCHANGE, key);
		int time = 0;
		if(value != null)
			time = Integer.parseInt(value);
		time++;
		hput(RedisKey.CDKEY_EXCHANGE, key, time+"");
		return time;
	}

	public List<String> getAvaiCdkeys(String id){
		List<String> cdkeys = new ArrayList<String>();
		Map<String, String> map = hget(RedisKey.CDKEY);
		for(Entry<String, String> entry : map.entrySet()){
			if(entry.getValue().equals(id))
				cdkeys.add(entry.getKey());
		}
		return cdkeys;
	}
	
	public List<String> addCdkeyConfig(Cdkey.Builder cdkey, int length){
		List<String> cdkeys = new ArrayList<String>();
		if(cdkey.getCount() <= 0)
			return cdkeys;
		String type = cdkey.getId()+"";
		String value = hget(RedisKey.CDKEY_CONFIG, type);
		if(value != null){
			Cdkey.Builder builder = Cdkey.newBuilder();
			parseJson(value, builder);
			builder.setCount(builder.getCount()+cdkey.getCount());
			hput(RedisKey.CDKEY_CONFIG, type, formatJson(builder.build()));
		}else
			hput(RedisKey.CDKEY_CONFIG, type, formatJson(cdkey.build()));
		String str = cdkey.toString()+"" + System.currentTimeMillis();
		int count = 0, index = 0;
		while(count < cdkey.getCount()){
			index += nextInt(88);
			String key = DigestUtils.md5Hex(str+index).substring(0, length);
			if(hputnx(RedisKey.CDKEY, key, type)){
				cdkeys.add(key);
				count++;
			}
		}
		return cdkeys;
	}

	public void delCdkeyConfig(String id){
		Map<String, String> map = hget(RedisKey.CDKEY);
		hdelete(RedisKey.CDKEY_CONFIG, id);
		for(Entry<String, String> entry : map.entrySet()){
			if(entry.getValue().equals(id))
				hdelete(RedisKey.CDKEY, entry.getKey());
		}
	}

	public Map<Integer, String> getCdkeyConfigs(){
		Map<Integer, String> cdkeymap = new TreeMap<Integer, String>();
		Map<String, String> map = hget(RedisKey.CDKEY_CONFIG);
		Map<String, String> exchange = hget(RedisKey.CDKEY_EXCHANGE);
		for(Entry<String, String> entry : map.entrySet()){
			Cdkey.Builder builder = Cdkey.newBuilder();
			parseJson(entry.getValue(), builder);
			String count = exchange.get(builder.getId()+"");
			if(count != null)
				builder.setCurrentCount(Integer.parseInt(count));
			else
				builder.setCurrentCount(0);
			cdkeymap.put(Integer.parseInt(entry.getKey()), formatJson(builder.build()));
		}
		return cdkeymap;
//		Collections.sort(list, new Comparator<Cdkey>() {
//			public int compare(Cdkey cdkey1, Cdkey cdkey2) {
//				return cdkey1.getId() - cdkey2.getId();
//			}
//		});
//		CdkeyList.Builder listbuilder = CdkeyList.newBuilder();
//		listbuilder.addAllCdkey(list);
//		return listbuilder.build();
	}
	
	public Cdkey getCdkeyConfig(String id){
		Cdkey.Builder builder = Cdkey.newBuilder();
		String value = hget(RedisKey.CDKEY_CONFIG, id);
		if(value != null && parseJson(value, builder))
			return builder.build();
		CdkeyList.Builder listbuilder = CdkeyList.newBuilder();
		Map<String, String> map = new HashMap<String, String>();
		String xml = ReadConfig("lol_cdkey.xml");
		parseXml(xml, listbuilder);
		for(Cdkey.Builder cdkey : listbuilder.getCdkeyBuilderList()){
			RewardInfo.Builder reward = RewardInfo.newBuilder();
			reward.setItemid(cdkey.getItemid1());
			reward.setCount(cdkey.getCount1());
			cdkey.addReward(reward);
			if(cdkey.getCount2() > 0){
				reward.setItemid(cdkey.getItemid2());
				reward.setCount(cdkey.getCount2());
				cdkey.addReward(reward);
			}
			if(cdkey.getCount3() > 0){
				reward.setItemid(cdkey.getItemid3());
				reward.setCount(cdkey.getCount3());
				cdkey.addReward(reward);
			}
			cdkey.clearItemid1();
			cdkey.clearCount1();
			cdkey.clearItemid2();
			cdkey.clearCount2();
			cdkey.clearItemid3();
			cdkey.clearCount3();
			map.put(cdkey.getId()+"", formatJson(cdkey.build()));
		}
		hputAll(RedisKey.CDKEY_CONFIG, map);
		value = map.get(id);
		if(value != null && parseJson(value, builder))
			return builder.build();
		else
			return null;
	}
}
