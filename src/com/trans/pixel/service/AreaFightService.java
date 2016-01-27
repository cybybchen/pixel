package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.AreaBoss;
import com.trans.pixel.protoc.Commands.AreaInfo;
import com.trans.pixel.protoc.Commands.AreaMonster;
import com.trans.pixel.protoc.Commands.AreaResource;
import com.trans.pixel.protoc.Commands.AreaResourceMine;
import com.trans.pixel.service.redis.AreaRedisService;

/**
 * 1.1.3.6区域争夺战
 */
@Service
public class AreaFightService {
	Logger logger = Logger.getLogger(AreaFightService.class);
	@Resource
    private AreaRedisService redis;
	private UserBean user = null;

	public List<AreaInfo> Area(){
		List<AreaInfo> areas = redis.getAreas().getRegionList();
		return areas;
	}

	public boolean AttackMonster(int id){
		AreaMonster.Builder builder = AreaMonster.newBuilder();
		builder.mergeFrom(redis.getMonster(id));
		redis.saveMonster(builder.build());
		return true;
	}
	
	public boolean AttackBoss(int id){
		AreaBoss.Builder builder = AreaBoss.newBuilder();
		builder.mergeFrom(redis.getBoss(id));
		builder.setOwner(user.account);
		redis.saveBoss(builder.build());
		return true;
	}
	
	public boolean AttackResource(int id){
		AreaResource.Builder builder = AreaResource.newBuilder();
		builder.mergeFrom(redis.getResource(id));
		builder.setOwner(user.account);
		redis.saveResource(builder.build());
		return true;
	}
	
	public boolean AttackResourceMine(int id){
		AreaResourceMine.Builder builder = AreaResourceMine.newBuilder();
		builder.mergeFrom(redis.getResourceMine(id));
		builder.setOwner(user.account);
		redis.saveResourceMine(builder.build());
		return true;
	}
	public void setUserNX(UserBean user) {
		if(this.user != null)
			return;
		this.user = user;
		redis.setUser(user);
	}
}
