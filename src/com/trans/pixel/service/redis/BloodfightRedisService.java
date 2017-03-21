package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.BloodUserBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.UnionProto.TowerReward;
import com.trans.pixel.protoc.UnionProto.TowerRewardList;

@Service
public class BloodfightRedisService extends RedisService {
	private static Logger logger = Logger.getLogger(BloodfightRedisService.class);
	
	private static final int BLOODFIGHT_ZHANLI_RENSHU = 16;
	
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	private static final String TOWERREWARD1_FILE_NAME = "lol_towerreward1.xml";
	private static final String TOWERREWARD2_FILE_NAME = "lol_towerreward2.xml";
	
	public void enter(UserBean user) {
		String key = RedisKey.BLOOD_ENTER_PREFIX + user.getServerId();
		this.sadd(key, "" + user.getId());
	}
	
	public Set<String> getEnterUsers(int serverId) {
		return this.smember(RedisKey.BLOOD_ENTER_PREFIX + serverId);
	}
	
	public boolean addZhanliToBloodfight(final int serverId, final UserInfo user) {
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundZSetOperations<String, String> bzOps = redisTemplate
						.boundZSetOps(RedisKey.BLOOD_ZHANLI_PREFIX + serverId);
				
				boolean ret = bzOps.add("" + user.getId(), user.getZhanliMax());
				if (ret && bzOps.size() > BLOODFIGHT_ZHANLI_RENSHU) {
//					Set<String> ids = bzOps.range(bzOps.size() - 1, bzOps.size() - 1);
					bzOps.removeRange(0, 0);
				}
				
				return ret;
			}
		});
	}
	
	public Set<String> getBloodfightUserSet(final int serverId) {
		String key = RedisKey.BLOOD_ZHANLI_PREFIX + serverId;
		return this.zrange(key, 0, -1);
	}
	
	public void setBloodUserList(int serverId, List<BloodUserBean> buserList) {
		this.hputAll(RedisKey.BLOOD_USERINFO_PREFIX + serverId, convertBloodUserMap(buserList));
	}
	
	public List<BloodUserBean> getBloodUserList(int serverId) {
		List<BloodUserBean> buserList = new ArrayList<BloodUserBean>();
		Map<String, String> map = this.hget(RedisKey.BLOOD_USERINFO_PREFIX + serverId);
		Iterator<Entry<String, String>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			BloodUserBean buser = BloodUserBean.fromJson(entry.getValue());
			if (buser != null && buser.isSurvival()) {
				buserList.add(buser);
			}
		}
		
		return buserList;
	}
	
	public BloodUserBean getBloodUser(UserBean user) {
		String value = this.hget(RedisKey.BLOOD_USERINFO_PREFIX + user.getServerId(), "" + user.getId());
		BloodUserBean buser = BloodUserBean.fromJson(value);
		if (buser != null ) {
			return buser;
		}
		
		return null;
	}
	
	public void setBloodUser(int serverId, BloodUserBean buser) {
		this.hput(RedisKey.BLOOD_USERINFO_PREFIX + serverId, "" + buser.getUserId(), BloodUserBean.toJson(buser));
	}
	
	public void xiazhu(UserBean user, long xiazhuUserId) {
		String key = RedisKey.BLOOD_XIAZHU_PREFIX + user.getServerId();
		this.hput(key, "" + user.getId(), "" + xiazhuUserId);
	}
	
	public Map<String, String> getXiazhuMap(int serverId) {
		Map<String, String> map = this.hget(RedisKey.BLOOD_XIAZHU_PREFIX + serverId);
		this.delete(RedisKey.BLOOD_XIAZHU_PREFIX + serverId);
		return map;
	}
	
	private Map<String, String> convertBloodUserMap(List<BloodUserBean> buserList) {
		Map<String, String> map = new HashMap<String, String>();
		for (BloodUserBean buser : buserList) {
			map.put("" + buser.getUserId(), BloodUserBean.toJson(buser));
		}
		
		return map;
	}
	
	//tower reward1
	public TowerReward getTowerReward1(int floor) {
		String value = hget(RedisKey.BATTLETOWER_REWARD1_KEY, "" + floor);
		if (value == null) {
			Map<String, TowerReward> config = getTowerReward1Config();
			return config.get("" + floor);
		} else {
			TowerReward.Builder builder = TowerReward.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, TowerReward> getTowerReward1Config() {
		Map<String, String> keyvalue = hget(RedisKey.BATTLETOWER_REWARD1_KEY);
		if(keyvalue.isEmpty()){
			Map<String, TowerReward> map = buildTowerReward1Config();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, TowerReward> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.BATTLETOWER_REWARD1_KEY, redismap);
			return map;
		}else{
			Map<String, TowerReward> map = new HashMap<String, TowerReward>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				TowerReward.Builder builder = TowerReward.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, TowerReward> buildTowerReward1Config(){
		String xml = ReadConfig(TOWERREWARD1_FILE_NAME);
		TowerRewardList.Builder builder = TowerRewardList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + TOWERREWARD1_FILE_NAME);
			return null;
		}
		
		Map<String, TowerReward> map = new HashMap<String, TowerReward>();
		for(TowerReward.Builder TowerReward : builder.getIdBuilderList()){
			map.put("" + TowerReward.getFloora(), TowerReward.build());
		}
		return map;
	}
	
	//tower reward2
	public TowerReward getTowerReward2(int floor) {
		String value = hget(RedisKey.BATTLETOWER_REWARD2_KEY, "" + floor);
		if (value == null) {
			Map<String, TowerReward> config = getTowerReward2Config();
			return config.get("" + floor);
		} else {
			TowerReward.Builder builder = TowerReward.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, TowerReward> getTowerReward2Config() {
		Map<String, String> keyvalue = hget(RedisKey.BATTLETOWER_REWARD2_KEY);
		if(keyvalue.isEmpty()){
			Map<String, TowerReward> map = buildTowerReward2Config();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, TowerReward> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.BATTLETOWER_REWARD2_KEY, redismap);
			return map;
		}else{
			Map<String, TowerReward> map = new HashMap<String, TowerReward>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				TowerReward.Builder builder = TowerReward.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, TowerReward> buildTowerReward2Config(){
		String xml = ReadConfig(TOWERREWARD2_FILE_NAME);
		TowerRewardList.Builder builder = TowerRewardList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + TOWERREWARD2_FILE_NAME);
			return null;
		}
		
		Map<String, TowerReward> map = new HashMap<String, TowerReward>();
		for(TowerReward.Builder TowerReward : builder.getIdBuilderList()){
			map.put("" + TowerReward.getFloor(), TowerReward.build());
		}
		return map;
	}
}
