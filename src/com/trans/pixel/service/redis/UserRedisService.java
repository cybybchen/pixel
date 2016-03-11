package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.protoc.Commands.VipInfo;
import com.trans.pixel.protoc.Commands.VipList;

@Repository
public class UserRedisService extends RedisService{
	Logger logger = LoggerFactory.getLogger(UserRedisService.class);
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	public final static String VIP = RedisKey.PREFIX+"Vip";
	
	public UserBean getUser(final long userId) {
		String value = hget(RedisKey.USERDATA+userId, "UserData");
		JSONObject json = JSONObject.fromObject(value);
		return (UserBean) JSONObject.toBean(json, UserBean.class);
	}

	/**
	 * update daily data, never save
	 */
	public boolean refreshUserDailyData(UserBean user){
		if(user.getRedisTime() >= System.currentTimeMillis()/24/3600L/1000L*24*3600L*1000L+6*3600L*1000L){
			user.setRedisTime(System.currentTimeMillis());
			return false;
		}
		//每日首次登陆
		user.setRedisTime(System.currentTimeMillis());
		user.setLadderModeLeftTimes(5);
		user.setPurchaseCoinLeft(1);
		user.setPvpMineLeftTime(5);
		VipInfo vip = getVip(user.getVip());
		if(vip != null){
			user.setPurchaseCoinLeft(user.getPurchaseCoinLeft() + vip.getDianjin());
			user.setLadderModeLeftTimes(user.getLadderModeLeftTimes()+vip.getTianti());
			user.setPvpMineLeftTime(user.getPvpMineLeftTime()+vip.getPvp());
			user.setPurchaseTireLeftTime(vip.getQuyu());
			user.setRefreshExpeditionLeftTime(vip.getMohua());
			user.setBaoxiangLeftTime(vip.getBaoxiang());
			user.setZhibaoLeftTime(vip.getZhibao());
		}
		return true;
	}

	public void updateUser(final UserBean user) {
		if(user.getId() == 0)
			return;
		String key = RedisKey.USERDATA+user.getId();
		hput(key, "UserData", JSONObject.fromObject(user).toString());
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}

	public String getUserIdByAccount(final int serverId, final String account) {
		return hget(RedisKey.PREFIX+RedisKey.ACCOUNT_PREFIX+serverId, account);
	}
	
	public <T> void setUserIdByAccount(final int serverId, final String account, final T userId) {
		hput(RedisKey.PREFIX+RedisKey.ACCOUNT_PREFIX+serverId, account, userId+"");
	}

	public String getUserIdByName(final int serverId, final String userName) {
		return hget(RedisKey.PREFIX+RedisKey.USERNAME_PREFIX+serverId, userName);
	}
	
	public <T> void setUserIdByName(final int serverId, final String userName, final T userId) {
		hput(RedisKey.PREFIX+RedisKey.USERNAME_PREFIX+serverId, userName, userId+"");
	}
	
	public UserInfo getRandUser(int serverId){
		Map<String, String> map = hget(RedisKey.PREFIX+RedisKey.USERCACHE_PREFIX+serverId);
		Object[] keys = map.keySet().toArray();
		String value = map.get(keys[nextInt(keys.length)]);
		UserInfo.Builder builder = UserInfo.newBuilder();
		parseJson(value, builder);
		return builder.build();
	}
	
	public void cache(int serverId, UserInfo user){
		hput(RedisKey.PREFIX+RedisKey.USERCACHE_PREFIX+serverId, user.getId()+"", formatJson(user));
	}
	
	/**
	 * get other user(can be null)
	 */
	public <T> UserInfo getCache(int serverId, T userId){
		String value = hget(RedisKey.PREFIX+RedisKey.USERCACHE_PREFIX+serverId, userId+"");
		UserInfo.Builder builder = UserInfo.newBuilder();
		if(value != null && parseJson(value, builder))
			return builder.build();
		else
			return null;
	}
	
	/**
	 * get other user
	 */
	public <T> List<UserInfo> getCaches(int serverId, Collection<T> userIds){
		List<String> keys = new ArrayList<String>();
		for(T userId : userIds){
			keys.add(userId+"");
		}
		List<String> values  = hget(RedisKey.PREFIX+RedisKey.USERCACHE_PREFIX+serverId, keys);
		List<UserInfo> users = new ArrayList<UserInfo>();
		for(String value : values){
			UserInfo.Builder builder = UserInfo.newBuilder();
			if(value != null && parseJson(value, builder))
				users.add(builder.build());
		}
		return users;
	}

	public VipInfo getVip(int id){
		if(id <= 0)
			return null;
		VipInfo.Builder builder = VipInfo.newBuilder();
		String value = hget(VIP, id+"");
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			return buildVip(id);
		}
	}
	
	public VipInfo buildVip(int id){
		String xml = ReadConfig("lol_vip.xml");
		VipList.Builder builder = VipList.newBuilder();
		parseXml(xml, builder);
		Map<String, String> keyvalue = new HashMap<String, String>();
		for(VipInfo vip : builder.getVipList()){
			keyvalue.put(vip.getVip()+"", formatJson(vip));
		}
		hputAll(VIP, keyvalue);
		for(VipInfo vip : builder.getVipList()){
			if(id == vip.getVip())
				return vip;
		}
		return null;
	}
}
