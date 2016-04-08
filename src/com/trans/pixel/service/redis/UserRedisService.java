package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	public final static String VIP = RedisKey.PREFIX+"Vip";
	
	public <T> UserBean getUser(T userId) {
		String value = hget(RedisKey.USERDATA+userId, "UserData");
		JSONObject json = JSONObject.fromObject(value);
		return (UserBean) JSONObject.toBean(json, UserBean.class);
	}

	/**
	 * update daily data, never save
	 */
	public boolean refreshUserDailyData(UserBean user){
		if(user.getRedisTime() >= today(0)){
			user.setRedisTime(now());
			return false;
		}
		//每日首次登陆
		user.setRedisTime(now());
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
		sadd(RedisKey.PUSH_MYSQL_KEY+RedisKey.USERDATA_PREFIX, user.getId()+"");
	}
	
	public void updateRobotUser(final UserBean user) {
		if(user.getId() == 0)
			return;
		String key = RedisKey.USERDATA+user.getId();
		hput(key, "UserData", JSONObject.fromObject(user).toString());
	}
	
	public String popDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY+RedisKey.USERDATA_PREFIX);
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
		String key = RedisKey.PREFIX+RedisKey.USERCACHE_PREFIX+serverId;
		Collection<String> set = hkeys(key);
		Object[] keys = set.toArray();
		String value = hget(key, (String)keys[nextInt(keys.length)]);
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
