package com.trans.pixel.service;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.AccountBean;
import com.trans.pixel.model.mapper.AccountMapper;
import com.trans.pixel.service.redis.AccountRedisService;

@Service
public class AccountService {
	Logger log = LoggerFactory.getLogger(AccountService.class);
	
	@Resource
    private AccountRedisService accountRedis;
	@Resource
    private AccountMapper accountMapper;
	
	public long getUserId(int serverId, String account) {
    	log.debug("serverId={},The account={}", serverId, account);
    	long userId = accountRedis.getUserIdByServerIdAndAccount(serverId, account);
    	
    	if (userId == 0) {
    		try {
	    		Object object = accountMapper.queryUserId(serverId, account);
	    		if (object != null)
	    			userId = (Long)object;
	    		} catch (Exception e) {
	//				logger.error("get userId from register failed");
			}
    	}
        
        return userId;
    }
	
	public long registerAccount(int serverId, String account) {
		AccountBean accountBean = new AccountBean();
		accountBean.setAccount(account);
		accountBean.setServerId(serverId);
		accountMapper.registerAccount(accountBean);
		long userId = accountBean.getId();
		log.debug("userId is:" + userId);
		
		accountRedis.setUserIdByServerIdAndAccount(serverId, account, userId);
		
		return userId;
	}
}
