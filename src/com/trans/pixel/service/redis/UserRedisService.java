package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserLibaoBean;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.RechargeProto.VipInfo;
import com.trans.pixel.protoc.RechargeProto.VipList;
import com.trans.pixel.protoc.ShopProto.DailyLibao;
import com.trans.pixel.protoc.ShopProto.DailyLibaoList;
import com.trans.pixel.protoc.ShopProto.Libao;
import com.trans.pixel.service.cache.CacheService;

import net.sf.json.JSONObject;

@Repository
public class UserRedisService extends RedisService{
	Logger logger = LoggerFactory.getLogger(UserRedisService.class);
	
	public UserRedisService() {
		buildVip();
		buildDailyLibao();
	}
	
	public <T> UserBean getUser(T userId) {
		String value = hget(RedisKey.USERDATA+userId, "UserData", Long.parseLong("" + userId));
		JSONObject json = JSONObject.fromObject(value);
		return (UserBean) JSONObject.toBean(json, UserBean.class);
	}

	public void updateUser(final UserBean user) {
		if(user.getId() == 0)
			return;
		String key = RedisKey.USERDATA+user.getId();
		hput(key, "UserData", JSONObject.fromObject(user).toString(), user.getId());
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, user.getId());
		sadd(RedisKey.PUSH_MYSQL_KEY+RedisKey.USERDATA_PREFIX, user.getId()+"");
	}
	
//	public void updateUserToRedis(final UserBean user) {
//		if(user.getId() == 0)
//			return;
//		String key = RedisKey.USERDATA+user.getId();
//		hput(key, "UserData", JSONObject.fromObject(user).toString());
//		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
//	}
	
	public void updateRobotUser(final UserBean user) {
		if(user.getId() == 0)
			return;
		String key = RedisKey.USERDATA+user.getId();
		hput(key, "UserData", JSONObject.fromObject(user).toString(), user.getId());
	}
	
	public String popDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY+RedisKey.USERDATA_PREFIX);
	}
	
//	public Set<String> popDBKey(){
//		return userCacheService.spop(RedisKey.PUSH_MYSQL_KEY+RedisKey.USERDATA_PREFIX);
//	}
	
	public String popLibaoDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY+RedisKey.USER_LIBAOCOUNT_PREFIX);
	}

	public boolean existLibao(long userId) {
		return exists(RedisKey.USER_LIBAOCOUNT_PREFIX+userId, userId);
	}
	
	public Libao getLibao(long userId, int rechargeid) {
		String value = hget(RedisKey.USER_LIBAOCOUNT_PREFIX+userId, rechargeid+"", userId);
		Libao.Builder builder = Libao.newBuilder();
		if(value != null && parseJson(value, builder))
			return builder.build();
		builder.setRechargeid(rechargeid);
		builder.setPurchase(0);
		saveLibao(userId, builder.build());
		return builder.build();
	}
	
	public Map<Integer, Libao> getLibaos(long userId) {
		Map<String, String> keyvalue = hget(RedisKey.USER_LIBAOCOUNT_PREFIX + userId, userId);
		expire(RedisKey.USER_LIBAOCOUNT_PREFIX + userId, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
		Map<Integer, Libao> map = new HashMap<Integer, Libao>();
		for(Entry<String, String> entry : keyvalue.entrySet()){
			Libao.Builder builder = Libao.newBuilder();
			if(parseJson(entry.getValue(), builder))
				map.put(Integer.parseInt(entry.getKey()), builder.build());
		}
		return map;
	}
	
	public void saveLibao(long userId, Libao libao) {
		hput(RedisKey.USER_LIBAOCOUNT_PREFIX+userId, libao.getRechargeid()+"", formatJson(libao), userId);
		expire(RedisKey.USER_LIBAOCOUNT_PREFIX+userId, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
		sadd(RedisKey.PUSH_MYSQL_KEY+RedisKey.USER_LIBAOCOUNT_PREFIX, userId+"#"+libao.getRechargeid());
	}
	
	public void saveLibaos(long userId, Map<Integer, Libao> libaos) {
		Map<String, String> keyvalue = new HashMap<String, String>();
		for(Libao libao : libaos.values())
			keyvalue.put(libao.getRechargeid()+"", formatJson(libao));
		
		hputAll(RedisKey.USER_LIBAOCOUNT_PREFIX+userId, keyvalue, userId);
		expire(RedisKey.USER_LIBAOCOUNT_PREFIX+userId, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
	}

	public UserLibaoBean getLibaoBean(long userId, int rechargeid) {
		String value = hget(RedisKey.USER_LIBAOCOUNT_PREFIX+userId, rechargeid+"", userId);
		Libao.Builder builder = Libao.newBuilder();
		if(value != null && parseJson(value, builder)){
			UserLibaoBean libao = new UserLibaoBean();
			libao.setUserId(userId);
			libao.setRechargeId(rechargeid);
			if(builder.hasValidtime()) libao.setValidTime(builder.getValidtime());
			libao.setPurchase(builder.getPurchase());
			return libao;
		}
		return null;
	}
	


	// public boolean existDailyLibao(long userId) {
	// 	return exists(RedisKey.USER_DAILYLIBAO_PREFIX+userId, userId);
	// }
	
	public Libao getDailyLibao(long userId, int rechargeid) {
		Map<Integer, Libao> map = getDailyLibaos(userId);
		for(Libao libao : map.values()) {
			if(libao.getRechargeid() == rechargeid)
				return libao;
		}
		return null;
	}
	
	public Map<Integer, Libao> getDailyLibaos(long userId) {
		Map<String, String> keyvalue = hget(RedisKey.USER_DAILYLIBAO_PREFIX + userId, userId);
		expire(RedisKey.USER_DAILYLIBAO_PREFIX + userId, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
		Map<Integer, Libao> map = new TreeMap<Integer, Libao>();
		for(Entry<String, String> entry : keyvalue.entrySet()){
			Libao.Builder builder = Libao.newBuilder();
			if(parseJson(entry.getValue(), builder))
				map.put(Integer.parseInt(entry.getKey()), builder.build());
		}
		return map;
	}
	
	public void saveDailyLibao(long userId, Libao libao) {
		hput(RedisKey.USER_DAILYLIBAO_PREFIX+userId, libao.getId()+"", formatJson(libao), userId);
		expire(RedisKey.USER_DAILYLIBAO_PREFIX+userId, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
	}

	public void clearDailyLibaos(long userId) {
		delete(RedisKey.USER_DAILYLIBAO_PREFIX+userId, userId);
	}
	
	public void saveDailyLibaos(long userId, Map<Integer, Libao> libaos) {
		Map<String, String> keyvalue = new HashMap<String, String>();
		for(Libao libao : libaos.values())
			keyvalue.put(libao.getId()+"", formatJson(libao));
		
		hputAll(RedisKey.USER_DAILYLIBAO_PREFIX+userId, keyvalue, userId);
		expire(RedisKey.USER_DAILYLIBAO_PREFIX+userId, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
	}

	public String getUserIdByAccount(final int serverId, final String account) {
		return hget(RedisKey.PREFIX+RedisKey.ACCOUNT_PREFIX+serverId, account);
	}
	
	public <T> void setUserIdByAccount(final int serverId, final String account, final T userId) {
		hput(RedisKey.PREFIX+RedisKey.ACCOUNT_PREFIX+serverId, account, userId+"");
	}

	public void delUserIdByAccount(final int serverId, final String account) {
		hdelete(RedisKey.PREFIX + RedisKey.ACCOUNT_PREFIX + serverId, account);
	}
	
	public long getUserIdByName(final int serverId, final String userName) {
		String value = hget(RedisKey.PREFIX+RedisKey.USERNAME_PREFIX+serverId, userName);
		if(value == null)
			return 0;
		else
			return Long.parseLong(value);
	}
	
	public <T> void setUserIdByName(final int serverId, final String userName, final T userId) {
		hput(RedisKey.PREFIX+RedisKey.USERNAME_PREFIX+serverId, userName, userId+"");
	}
	
	public void deleteUserIdByName(final int serverId, final String userName) {
		hdelete(RedisKey.PREFIX+RedisKey.USERNAME_PREFIX+serverId, userName);
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
		if(value != null && parseJson(value, builder)) {
			if (!builder.hasSignName())
				builder.setSignName("");
			return builder.build();
		}else
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
		List<String> values  = hmget(RedisKey.PREFIX+RedisKey.USERCACHE_PREFIX+serverId, keys);
		List<UserInfo> users = new ArrayList<UserInfo>();
		for(String value : values){
			UserInfo.Builder builder = UserInfo.newBuilder();
			if(value != null && parseJson(value, builder)) {
				if (!builder.hasSignName())
					builder.setSignName("");
				users.add(builder.build());
			}
		}
		return users;
	}

	public VipInfo getVip(int id){
		Map<Integer, VipInfo> map = CacheService.hgetcache(RedisKey.VIP_CONFIG);
		return map.get(id);
	}

	private void buildDailyLibao(){
		String xml = ReadConfig("ld_dailylibao.xml");
		DailyLibaoList.Builder builder = DailyLibaoList.newBuilder();
		parseXml(xml, builder);
		Map<Integer, DailyLibao> map = new HashMap<Integer, DailyLibao>();
		for(DailyLibao libao : builder.getDataList()){
			map.put(libao.getId(), libao);
		}
		CacheService.hputcacheAll(RedisKey.DAILYLIBAO_CONFIG, map);
	}
	
	private void buildVip(){
		String xml = ReadConfig("ld_vip.xml");
		VipList.Builder builder = VipList.newBuilder();
		parseXml(xml, builder);
		Map<Integer, VipInfo> map = new HashMap<Integer, VipInfo>();
		for(VipInfo vip : builder.getDataList()){
			map.put(vip.getVip(), vip);
		}
		CacheService.hputcacheAll(RedisKey.VIP_CONFIG, map);
	}
	
	public Map<String, String> getUserTypeMap() {
		Map<String, String> map = this.hget(RedisKey.USERTYPE_KEY);
		
		return map;
	}
}
