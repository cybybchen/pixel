package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.protoc.Commands.VipInfo;
import com.trans.pixel.service.redis.UserRedisService;

@Service
public class UserService {
	Logger logger = LoggerFactory.getLogger(UserService.class);
	
	@Resource
    private UserRedisService userRedisService;
	@Resource
    private UserMapper userMapper;
	
	public UserBean getUser(long userId) {
    	logger.debug("The user id is: " + userId);
    	UserBean user = userRedisService.getUser(userId);
    	if(userId < 0)
    		return user;
    	if (user == null) {
    		user = userMapper.queryById(userId);
    		if (user != null){
    			UserInfo cache = getCache(user.getServerId(), user.getId());
    			if(cache != null && cache.getZhanli() > user.getZhanli()){
    				user.setZhanli(cache.getZhanli());
    				user.setZhanliMax(cache.getZhanli());
    			}
    			userRedisService.updateUser(user);
    		}
    	}

        if(user != null && userRedisService.refreshUserDailyData(user)){
        	userRedisService.updateUser(user);
        }
        return user;
    }
	
	public UserBean getRobotUser(long userId) {
    	logger.debug("The user id is: " + userId);
    	UserBean user = userRedisService.getUser(userId);
    
    	return user;
    }
	
	public UserBean getUserByAccount(int serverId, String account) {
		logger.debug("serverId={},The account={}", serverId, account);
		String userId = userRedisService.getUserIdByAccount(serverId, account);
		UserBean user = null;

		if (userId == null) {
			try {
				user = userMapper.queryByServerAndAccount(serverId, account);
			} catch (Exception e) {
				 logger.error("login failed:"+e.getMessage());
			}
			if (user != null) {
				userRedisService.setUserIdByAccount(serverId, account, user.getId());
				UserInfo cache = getCache(user.getServerId(), user.getId());
    			if(cache != null && cache.getZhanli() > user.getZhanli()){
    				user.setZhanli(cache.getZhanli());
    				user.setZhanliMax(cache.getZhanli());
    			}else{
    				userRedisService.cache(serverId, user.buildShort());
    			}
				userRedisService.updateUser(user);
			}
		} else {
			return getUser(Long.parseLong(userId));
		}

        if(user != null && userRedisService.refreshUserDailyData(user)){
        	userRedisService.updateUser(user);
        }
		return user;
    }
	
	public UserBean getUserByName(int serverId, String userName) {
		logger.debug("serverId={},The userName={}", serverId, userName);
		String userId = userRedisService.getUserIdByName(serverId, userName);
		UserBean user = null;

		if (userId == null) {
			try {
				user = userMapper.queryByServerAndName(serverId, userName);
			} catch (Exception e) {
				 logger.error("login failed:"+e.getMessage());
			}
			if (user != null) {
				userRedisService.setUserIdByName(serverId, userName, user.getId());
				UserInfo cache = getCache(user.getServerId(), user.getId());
    			if(cache != null && cache.getZhanli() > user.getZhanli()){
    				user.setZhanli(cache.getZhanli());
    				user.setZhanliMax(cache.getZhanli());
    			}else{
    				userRedisService.cache(serverId, user.buildShort());
    			}
				userRedisService.updateUser(user);
			}
		} else {
			return getUser(Long.parseLong(userId));
		}

        if(user != null && userRedisService.refreshUserDailyData(user)){
        	userRedisService.updateUser(user);
        }
		return user;
    }
	
	public int addNewUser(UserBean user) {
		int result = userMapper.addNewUser(user);
		user.setPvpMineRefreshTime((int)userRedisService.today(6));
		userRedisService.refreshUserDailyData(user);
		userRedisService.updateUser(user);
		userRedisService.setUserIdByAccount(user.getServerId(), user.getAccount(), user.getId());
		userRedisService.cache(user.getServerId(), user.buildShort());
		return result;
	}

	public void updateUserDailyData(UserBean user) {
		userRedisService.updateUser(user);
	}
	
	public void updateUser(UserBean user) {
		userRedisService.updateUser(user);
//		userMapper.updateUser(user);
	}
	
	public void updateRobotUser(UserBean user) {
		userRedisService.updateRobotUser(user);
	}
	
	public <T> void updateToDB(T userId) {
		UserBean user = userRedisService.getUser(userId);
		if(user != null)
			userMapper.updateUser(user);
	}
	
	public String popDBKey(){
		return userRedisService.popDBKey();
	}

	public List<UserBean> getUserByUnionId(int unionId) {
    	return userMapper.queryByUnionId(unionId);
    }
	
	public UserInfo getRandUser(UserBean user){
		UserInfo userinfo = userRedisService.getRandUser(user.getServerId());
		while(userinfo.getId() == user.getId())
			userinfo = userRedisService.getRandUser(user.getServerId());
		return userinfo;
	}
	
	public void cache(int serverId, UserInfo user){
		userRedisService.cache(serverId, user);
	}
	
	/**
	 * get other user
	 */
	public UserInfo getCache(int serverId, long userId){
		UserInfo userinfo =  userRedisService.getCache(serverId, userId);
		if(userinfo == null){
			UserInfo.Builder builder = UserInfo.newBuilder();
			builder.setId(userId);
			builder.setIcon(1);
			builder.setName("7天离线玩家");
			userinfo = builder.build();
		}
		return userinfo;
	}
	
	/**
	 * get other user
	 */
	public <T> List<UserInfo> getCaches(int serverId, Collection<T> userIds){
		return userRedisService.getCaches(serverId, userIds);
	}
	
	/**
	 * get other user
	 */
	public List<UserInfo> getCaches(int serverId, Set<TypedTuple<String>> ranks){
		List<String> userIds = new ArrayList<String>();
		for(TypedTuple<String> rank : ranks){
			userIds.add(rank.getValue());
		}
		return userRedisService.getCaches(serverId, userIds);
	}
	
	public VipInfo getVip(int id){
		return userRedisService.getVip(id);
	}
}
