package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserTeamBean;
import com.trans.pixel.protoc.Base.FightInfo;
import com.trans.pixel.protoc.Base.Team;
import com.trans.pixel.protoc.HeroProto.TeamUnlock;
import com.trans.pixel.protoc.HeroProto.TeamUnlockList;
import com.trans.pixel.service.cache.CacheService;

@Repository
public class FightInfoRedisService extends RedisService {
	private static Logger logger = Logger.getLogger(FightInfoRedisService.class);

	public void setFightInfo(String info, UserBean user){
		String key = RedisKey.USER_FIGHT_PREFIX+user.getId();
		lpush(key, info, user.getId());
		long size = llen(key, user.getId());
		for(; size > 10; size--){
			rpop(key, user.getId());
		}
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, user.getId());
	}

	public List<FightInfo.Builder> getFightInfoList(UserBean user){
		List<FightInfo.Builder> list = new ArrayList<FightInfo.Builder>();
		List<String> values = lrange(RedisKey.USER_FIGHT_PREFIX+user.getId(), user.getId());
		for(String value : values){
			FightInfo.Builder builder = FightInfo.newBuilder();
			if(parseJson(value, builder))
				list.add(builder);
		}
		expire(RedisKey.USER_FIGHT_PREFIX+user.getId(), RedisExpiredConst.EXPIRED_USERINFO_7DAY, user.getId());
		return list;
	}
	
	public void saveFightInfo(UserBean user, FightInfo fightinfo) {
		String key = RedisKey.USER_SAVE_FIGHT_PREFIX + user.getId();
		hput(key, fightinfo.getId() + "", RedisService.formatJson(fightinfo), user.getId());
		expire(RedisKey.USER_FIGHT_PREFIX+user.getId(), RedisExpiredConst.EXPIRED_USERINFO_7DAY, user.getId());
	}
	
	public boolean isExistFightInfoKey(UserBean user, int id) {
		String key = RedisKey.USER_SAVE_FIGHT_PREFIX + user.getId();
		return hexist(key, "" + id, user.getId());
	}
	
	public Set<String> saveFightInfoKeys(UserBean user) {
		String key = RedisKey.USER_SAVE_FIGHT_PREFIX + user.getId();
		return hkeys(key, user.getId());
	}
	
	public void deleteFightInfo(UserBean user, int fightInfoId) {
		String key = RedisKey.USER_SAVE_FIGHT_PREFIX + user.getId();
		hdelete(key, "" + fightInfoId, user.getId());
		expire(RedisKey.USER_FIGHT_PREFIX+user.getId(), RedisExpiredConst.EXPIRED_USERINFO_7DAY, user.getId());
	}
	
	public long hlenFightInfo(UserBean user) {
		String key = RedisKey.USER_SAVE_FIGHT_PREFIX + user.getId();
		return hlen(key, user.getId());
	}
	
	public List<FightInfo> getSaveFightInfoList(UserBean user){
		String key = RedisKey.USER_SAVE_FIGHT_PREFIX + user.getId();
		List<FightInfo> list = new ArrayList<FightInfo>();
		Map<String, String> map = hget(key, user.getId());
		for(String value : map.values()){
			FightInfo.Builder builder = FightInfo.newBuilder();
			if(parseJson(value, builder))
				list.add(builder.build());
		}
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, user.getId());
		return list;
	}
	
	public FightInfo getSaveFightInfo(UserBean user, int id) {
		String key = RedisKey.USER_SAVE_FIGHT_PREFIX + user.getId();
		String value = hget(key, "" + id, user.getId());
		FightInfo.Builder builder = FightInfo.newBuilder();
		if (value != null && RedisService.parseJson(value, builder))
			return builder.build();
		
		return null;
	}
}
